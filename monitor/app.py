#!/usr/bin/env python
import logging
import motor
import os.path
import random
import time
import tornado.escape
import tornado.gen
import tornado.ioloop
import tornado.httpserver
import tornado.web
import tornado.websocket
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
        tornado.web.Application.__init__(self, handlers, **settings)


class MainHandler(tornado.web.RequestHandler):

    def get(self):
        self.render('index.html')


class MessageQueue:

    def __init__(self):
        self.subscribers = set()

    def register(self, client):
        self.subscribers.add(client)

    def unregister(self, client):
        if client in self.subscribers:
            self.subscribers.remove(client)

    def publish(self, message):
        logging.info('Publishing "%s"' % message)
        for client in self.subscribers:
            client.write_message(message)


class WebsocketHandler(tornado.websocket.WebSocketHandler):
    mq = MessageQueue()

    @property
    def db(self):
        return self.application.db

    def open(self):
        logging.info('Websocket opened')

    def on_close(self):
        logging.info('Websocket closed')
        WebsocketHandler.mq.unregister(self)

    @tornado.gen.coroutine
    def on_message(self, message):
        data = tornado.escape.json_decode(message)

        if data.get('op') is None:
            WebsocketHandler.mq.publish(message)
            yield self.db.values.insert(data)

        elif data.get('op') == u'register':
            WebsocketHandler.mq.register(self)
            cursor = self.db.values.find()
            while (yield cursor.fetch_next):
                item = cursor.next_object()
                item.pop('_id')
                WebsocketHandler.mq.publish(tornado.escape.json_encode(item))

        elif data.get('op') == u'clear':
            yield self.db.values.remove()


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO,
                        format="%(asctime)s %(levelname)s %(message)s")
    tornado.options.parse_command_line()
    httpserver = tornado.httpserver.HTTPServer(Application())
    httpserver.listen(options.port)
    tornado.ioloop.IOLoop().instance().start()
