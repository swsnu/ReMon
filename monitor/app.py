#!/usr/bin/env python
import logging
import os.path
import pymongo
import random
import time
import tornado.escape
import tornado.ioloop
import tornado.httpserver
import tornado.web
import tornado.websocket
from tornado.options import define, options

define('port', default=8000, help='run on the given port', type=int)


class Application(tornado.web.Application):
    def __init__(self):
        settings = {
            'static_path': os.path.join(os.path.dirname(__file__), 'static'),
        }
        handlers = [
            (r'/', MainHandler),
            (r'/websocket', WebsocketHandler),
        ]
        self.db = pymongo.Connection().remon
        tornado.web.Application.__init__(self, handlers, **settings)


class BaseHandler(tornado.web.RequestHandler):
    @property
    def db(self):
        return self.application.db


class MainHandler(BaseHandler):
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


class WebsocketHandler(BaseHandler, tornado.websocket.WebSocketHandler):
    mq = MessageQueue()

    def open(self):
        logging.info('Websocket opened')

    def on_close(self):
        logging.info('Websocket closed')
        WebsocketHandler.mq.unregister(self)

    def on_message(self, message):
        data = tornado.escape.json_decode(message)
        if data.get('op') == u'register':
            WebsocketHandler.mq.register(self)
            history = self.db.values.find()
            for item in history:
                item.pop('_id')
                WebsocketHandler.mq.publish(tornado.escape.json_encode(item))
        else:
            WebsocketHandler.mq.publish(message)
            self.db.values.insert(data)


if __name__ == '__main__':
    logging.basicConfig(format="%(asctime)s %(levelname)s %(message)s", level=logging.INFO)
    tornado.options.parse_command_line()
    httpserver = tornado.httpserver.HTTPServer(Application())
    httpserver.listen(options.port)
    tornado.ioloop.IOLoop().instance().start()

