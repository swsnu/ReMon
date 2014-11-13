#!/usr/bin/env python
from datetime import timedelta
from tornado import gen
from tornado.escape import json_encode
from tornado.websocket import websocket_connect
from tornado.testing import AsyncHTTPTestCase, gen_test

from app import Application


class MainHandlerTest(AsyncHTTPTestCase):

    def get_app(self):
        return Application()

    def test_main_page(self):
        response = self.fetch(r'/')
        self.assertEqual(response.code, 200)

    def test_not_found(self):
        response = self.fetch(r'/i-am-lost')
        self.assertEqual(response.code, 404)

    def test_static_file(self):
        response = self.fetch(r'/static/js/remon.js')
        self.assertEqual(response.code, 200)


class WebsocketHandlerTest(AsyncHTTPTestCase):

    def get_app(self):
        return Application()

    @gen.coroutine
    def ws_connect(self):
        address = 'ws://localhost:%d/websocket' % self.get_http_port()
        ws = yield websocket_connect(address, io_loop=self.io_loop)
        raise gen.Return(ws)

    @gen_test
    def test_echo_with_register(self):
        ws = yield self.ws_connect()
        ws.write_message(json_encode({'op': 'register'}))
        for value in ['younha', 486, u'unicode']:
            message = json_encode({'key': value})
            ws.write_message(message)
            response = yield ws.read_message()
            self.assertEqual(response, message)
        ws.close()

    @gen_test
    def test_echo_without_register(self):
        ws = yield self.ws_connect()
        message = json_encode({'key': 'younha'})
        ws.write_message(message)
        with self.assertRaises(gen.TimeoutError) as context:
            wait_time = timedelta(seconds=1.0)
            response = yield gen.with_timeout(wait_time, ws.read_message())
        ws.close()
