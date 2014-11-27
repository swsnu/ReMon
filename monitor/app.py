#!/usr/bin/env python
import logging
import motor
import os.path
import random
import time
import tornado.gen
import tornado.httpserver
import tornado.ioloop
import tornado.web
import tornado.websocket
from tornado.escape import json_decode, json_encode
from tornado.options import define, options

define('port', default=8000, help='run on the given port', type=int)
define('mongo_uri', default='mongodb://localhost:27017/remon', type=str)


class Application(tornado.web.Application):

    def __init__(self):
        settings = {
            'static_path': os.path.join(os.path.dirname(__file__), 'static'),
        }
        handlers = [
            (r'/', MainHandler),
            (r'/websocket', WebsocketHandler),
        ]
        db_name = options.mongo_uri.rsplit('/', 1)[-1]
        self.db = motor.MotorClient(options.mongo_uri)[db_name]
        self.mq = MessageQueue()
        tornado.web.Application.__init__(self, handlers, **settings)


class MainHandler(tornado.web.RequestHandler):

    def get(self):
        self.render('index.html')


class MessageQueue(object):

    def __init__(self):
        self.channels = {}

    def subscribe(self, app_id, client):
        if app_id not in self.channels:
            self.channels[app_id] = set()
        self.channels[app_id].add(client)

    def unsubscribe(self, app_id, client):
        if app_id in self.channels:
            if client in self.channels[app_id]:
                self.channels[app_id].remove(client)

    def unsubscribe_all(self, client):
        for app_id in self.channels:
            self.unsubscribe(app_id, client)

    def publish(self, app_id, message):
        if app_id in self.channels:
            n_clients = len(self.channels[app_id])
            logging.info('[%s] (%d): "%s"' % (app_id, n_clients, message))
            for client in self.channels[app_id]:
                client.write_message(message)


class WebsocketHandler(tornado.websocket.WebSocketHandler):

    @property
    def db(self):
        return self.application.db

    @property
    def mq(self):
        return self.application.mq

    def open(self):
        logging.info('Websocket opened')

    def on_close(self):
        logging.info('Websocket closed')
        self.mq.unsubscribe_all(self)

    @tornado.gen.coroutine
    def on_message(self, message):
        data = json_decode(message)
        op = data.get('op', 'insert')
        app_id = data.get('app_id')

        if op == 'insert':
            metrics = data['metrics']
            yield self.db[app_id].insert(metrics)
            for item in metrics:
                item.pop('_id', None)
                self.mq.publish(app_id, json_encode(item))

        elif op == 'subscribe':
            self.mq.subscribe(app_id, self)
            self.write_message(json_encode({'op': op}))

        elif op == 'unsubscribe':
            self.mq.unsubscribe(app_id, self)
            self.write_message(json_encode({'op': op}))

        elif op == 'history':
            cursor = self.db[app_id].find()
            while (yield cursor.fetch_next):
                item = cursor.next_object()
                item.pop('_id', None)
                self.write_message(json_encode(item))

        elif op == 'clear':
            yield self.db[app_id].remove()
            self.write_message(json_encode({'op': op}))

        elif op == 'list':
            app_list = yield self.db.collection_names()
            self.write_message(json_encode({'op': op, 'app_list': app_list}))

        else:
            raise NotImplementedError('Undefined opcode: %s' % op)


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO,
                        format="%(asctime)s %(levelname)s %(message)s")
    tornado.options.parse_command_line()
    httpserver = tornado.httpserver.HTTPServer(Application())
    httpserver.listen(options.port)
    tornado.ioloop.IOLoop().instance().start()
