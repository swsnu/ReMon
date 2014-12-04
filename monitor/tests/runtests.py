#!/usr/bin/env python
from tornado import gen
from tornado.escape import json_encode, json_decode
from tornado.ioloop import IOLoop
from tornado.testing import AsyncHTTPTestCase, gen_test
from tornado.websocket import websocket_connect
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
    'op': 'metrics',
    'metrics': [TEST_METRIC],
}


class MainHandlerTestCase(AsyncHTTPTestCase):

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


class WebsocketHandlerTestCase(AsyncHTTPTestCase):

    def get_app(self):
        return Application()

    def get_new_ioloop(self):
        return IOLoop.instance()

    @gen.coroutine
    def ws_connect(self):
        address = 'ws://localhost:%d/websocket' % self.get_http_port()
        ws = yield websocket_connect(address, io_loop=self.io_loop)
        raise gen.Return(ws)

    @gen.coroutine
    def _make_request(self, ws, payload):
        payload['app_id'] = TEST_APP_ID
        ws.write_message(json_encode(payload))
        wait_time = timedelta(seconds=0.5)
        output = yield gen.with_timeout(wait_time, ws.read_message())
        result = json_decode(output)
        raise gen.Return(result)

    @gen_test
    def test_echo_with_subscribe(self):
        ws = yield self.ws_connect()
        resp = yield self._make_request(ws, {'op': 'subscribe'})
        self.assertDictEqual({'op': 'subscribe'}, resp)
        resp = yield self._make_request(ws, TEST_MESSAGE)
        self.assertDictEqual(TEST_METRIC, resp)
        ws.close()

    @gen_test
    def test_echo_without_subscribe(self):
        ws = yield self.ws_connect()
        with self.assertRaises(gen.TimeoutError) as context:
            resp = yield self._make_request(ws, TEST_MESSAGE)
        ws.close()

    @gen_test
    def test_unsubscribe(self):
        ws = yield self.ws_connect()
        resp = yield self._make_request(ws, {'op': 'subscribe'})
        resp = yield self._make_request(ws, {'op': 'unsubscribe'})
        self.assertDictEqual({'op': 'unsubscribe'}, resp)
        with self.assertRaises(gen.TimeoutError) as context:
            resp = yield self._make_request(ws, TEST_MESSAGE)
        ws.close()

    @gen_test
    def test_history(self):
        ws = yield self.ws_connect()
        resp = yield self._make_request(ws, {'op': 'clear'})
        resp = yield self._make_request(ws, {'op': 'subscribe'})
        n_times = 5
        for _ in xrange(n_times):
            resp = yield self._make_request(ws, TEST_MESSAGE)
        resp = yield self._make_request(ws, {'op': 'history'})
        for _ in xrange(n_times):
            resp = yield self._make_request(ws, TEST_MESSAGE)
            self.assertDictEqual(TEST_METRIC, resp)
        ws.close()

    @gen_test
    def test_list(self):
        ws = yield self.ws_connect()
        resp = yield self._make_request(ws, {'op': 'list'})
        self.assertEqual('list', resp['op'])
        self.assertIn(TEST_APP_ID, resp['app_list'])

    @gen_test
    def test_undefined(self):
        ws = yield self.ws_connect()
        with self.assertRaises(gen.TimeoutError) as context:
            resp = yield self._make_request(ws, {'op': 'undefined'})
        ws.close()
