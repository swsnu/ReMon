#!/usr/bin/env python
import tornado.ioloop
from tornado import gen
from tornado.escape import json_encode, json_decode
from tornado.websocket import websocket_connect
from tornado.testing import AsyncHTTPTestCase, gen_test
from datetime import timedelta

from app import Application


TEST_APP_ID = 'TEST_APP_ID'
TEST_SOURCE_ID = 'TEST_SOURCE_ID'
TEST_TAG = 'TEST_TAG'
TEST_TIME = 1234567890
TEST_VALUE = 486.
TEST_METRIC = {
    'source_id': TEST_SOURCE_ID,
    'tag': TEST_TAG,
    'time': TEST_TIME,
    'value': TEST_VALUE,
}
TEST_MESSAGE = {
    'app_id': TEST_APP_ID,
    'metrics': [TEST_METRIC],
}


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

    def get_new_ioloop(self):
        return tornado.ioloop.IOLoop.instance()

    @gen.coroutine
    def ws_connect(self):
        address = 'ws://localhost:%d/websocket' % self.get_http_port()
        ws = yield websocket_connect(address, io_loop=self.io_loop)
        raise gen.Return(ws)

    @gen_test
    def test_echo_with_register(self):
        ws = yield self.ws_connect()
        ws.write_message(json_encode({'op': 'register'}))
        ws.write_message(json_encode(TEST_MESSAGE))
        output = json_decode((yield ws.read_message()))
        self.assertDictEqual(TEST_METRIC, output)
        ws.close()

    @gen_test
    def test_echo_without_register(self):
        ws = yield self.ws_connect()
        ws.write_message(json_encode(TEST_MESSAGE))
        with self.assertRaises(gen.TimeoutError) as context:
            wait_time = timedelta(seconds=0.5)
            response = yield gen.with_timeout(wait_time, ws.read_message())
        ws.close()

    @gen_test
    def test_history(self):
        ws = yield self.ws_connect()
        ws.write_message(json_encode({'op': 'clear'}))
        ws.write_message(json_encode({'op': 'register'}))
        ws.write_message(json_encode(TEST_MESSAGE))
        output = json_decode((yield ws.read_message()))
        self.assertDictEqual(TEST_METRIC, output)
        ws.write_message(json_encode({'op': 'history', 'app_id': TEST_APP_ID}))
        output = json_decode((yield ws.read_message()))
        self.assertDictEqual(TEST_METRIC, output)
        ws.close()
