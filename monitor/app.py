#!/usr/bin/env python
import logging
import os.path
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
        tornado.web.Application.__init__(self, handlers, **settings)


class BaseHandler(tornado.web.RequestHandler):
    pass


class MainHandler(BaseHandler):
    def get(self):
        self.render('index.html')


class MessageQueue:
    def __init__(self):
        self.subscribers = set()

    def register(self, client):
        self.subscribers.add(client)

    def unregister(self, client):
        self.subscribers.remove(client)

    def publish(self, message):
        logging.info('Publishing "%s"' % message)
        for client in self.subscribers:
            client.write_message(message)


class WebsocketHandler(tornado.websocket.WebSocketHandler):
    def __init__(self, *args, **kwargs):
        self.mq = MessageQueue()
        tornado.websocket.WebSocketHandler.__init__(self, *args, **kwargs)

    def open(self):
        logging.info('Websocket opened')
        self.mq.register(self)

    def on_close(self):
        logging.info('Websocket closed')
        self.mq.unregister(self)

    def on_message(self, message):
        self.mq.publish('Echo: %s' % message)


if __name__ == '__main__':
    logging.basicConfig(format="%(asctime)s %(levelname)s %(message)s", level=logging.INFO)
    tornado.options.parse_command_line()
    httpserver = tornado.httpserver.HTTPServer(Application())
    httpserver.listen(options.port)
    tornado.ioloop.IOLoop().instance().start()

