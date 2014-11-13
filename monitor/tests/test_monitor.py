#!/usr/bin/env python
from functools import partial
from tornado.testing import gen_test
from tornado import testing
import tornado.websocket
import tornado.web
import tornado.ioloop
import app


class EchoWebSocketHandler(tornado.websocket.WebSocketHandler):
    def on_message(self, message):
      self.write_message(message)


class WebSocketTest(testing.AsyncHTTPTestCase):

    def get_app(self):
      app = tornado.web.Application([('/', EchoWebSocketHandler)])
      return app
    
    @gen_test
    def test_echo(self):
      ws = yield tornado.websocket.websocket_connect('ws://localhost:%d/' % self.get_http_port(),io_loop=self.io_loop)
      ws.write_message(str({'register': 'cpu'}))
      response = yield ws.read_message()
      self.assertIn(str({'register': 'cpu'}), response, "Server didn't send source not a_response. Instead sent: %s" % response)
