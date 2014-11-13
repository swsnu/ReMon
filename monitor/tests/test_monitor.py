#!/usr/bin/env python
import tornado.escape
import tornado.ioloop
import tornado.web
import tornado.websocket
from tornado.testing import AsyncHTTPTestCase, gen_test

import app


class EchoWebSocketHandler(tornado.websocket.WebSocketHandler):

    def on_message(self, message):
        self.write_message(message)


class WebSocketTest(AsyncHTTPTestCase):

    def get_app(self):
        app = tornado.web.Application([('/', EchoWebSocketHandler)])
        return app

    @gen_test
    def test_echo(self):
        address = 'ws://localhost:%d/' % self.get_http_port()
        ws = yield tornado.websocket.websocket_connect(address, io_loop=self.io_loop)
        ws.write_message(tornado.escape.json_encode({'register': 'cpu'}))
        response = yield ws.read_message()
        self.assertIn(tornado.escape.json_encode({'register': 'cpu'}), response,
                      "Server didn't send source not a_response. Instead sent: %s" % response)
