#!/usr/bin/env python
import os.path
import random
import time
import tornado.escape
import tornado.ioloop
import tornado.httpserver
import tornado.web
from tornado.options import define, options

define('port', default=8000, help='run on the given port', type=int)


class Application(tornado.web.Application):
    def __init__(self):
        settings = {
            'static_path': os.path.join(os.path.dirname(__file__), 'static'),
        }
        handlers = [
            (r'/', MainHandler),
        ]
        tornado.web.Application.__init__(self, handlers, **settings)


class BaseHandler(tornado.web.RequestHandler):
    pass


class MainHandler(BaseHandler):
    def get(self):
        self.render('index.html')


if __name__ == '__main__':
    tornado.options.parse_command_line()
    httpserver = tornado.httpserver.HTTPServer(Application())
    httpserver.listen(options.port)
    tornado.ioloop.IOLoop().instance().start()

