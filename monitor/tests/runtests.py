#!/usr/bin/env python
from tornado import gen
from tornado.escape import json_encode, json_decode
from tornado.ioloop import IOLoop
from tornado.testing import AsyncHTTPTestCase, gen_test
from tornado.websocket import websocket_connect
from datetime import timedelta

from app import Application


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
        response = self.fetch(r'/static/js/remon-main.js')
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
    def wait_response(self, ws):
        timeout = timedelta(seconds=0.5)
        message = yield gen.with_timeout(timeout, ws.read_message())
        response = json_decode(message)
        raise gen.Return(response)

    @gen_test
    def test_insert_with_subscribe(self):
        ws = yield self.ws_connect()
        payload = {
            'op': 'subscribe',
            'app_id': 'TEST_APP_ID',
        }
        ws.write_message(json_encode(payload))
        response = yield self.wait_response(ws)
        payload = {
            'op': 'insert',
            'app_id': 'TEST_APP_ID',
            'metrics': [{
                'source_id': 'TEST_SOURCE_ID',
                'tag': 'TEST_TAG',
                'time': 1234567890,
                'value': 486.,
            }],
            'messages': [{
                'source_id': 'TEST_SOURCE_ID',
                'level': 'INFO',
                'time': 1234567890,
                'message': 'TEST_MESSAGE',
            }],
            'events': [{
                'source_id': 'TEST_SOURCE_ID',
                'tag': 'TEST_TAG',
                'time': 1234567890,
                'type': 'START'
            }],
        }
        ws.write_message(json_encode(payload))
        response = yield self.wait_response(ws)
        self.assertDictEqual(payload, response)
        ws.close()

    @gen_test
    def test_insert_without_subscribe(self):
        ws = yield self.ws_connect()
        payload = {
            'op': 'insert',
            'app_id': 'TEST_APP_ID',
            'metrics': [{
                'source_id': 'TEST_SOURCE_ID',
                'tag': 'TEST_TAG',
                'time': 1234567890,
                'value': 486.,
            }],
            'messages': [{
                'source_id': 'TEST_SOURCE_ID',
                'level': 'INFO',
                'time': 1234567890,
                'message': 'TEST_MESSAGE',
            }],
            'events': [{
                'source_id': 'TEST_SOURCE_ID',
                'tag': 'TEST_TAG',
                'time': 1234567890,
                'type': 'START'
            }],
        }
        ws.write_message(json_encode(payload))
        with self.assertRaises(gen.TimeoutError) as context:
            response = yield self.wait_response(ws)
        ws.close()

    @gen_test
    def test_list(self):
        ws = yield self.ws_connect()
        payload = {
            'op': 'list',
        }
        ws.write_message(json_encode(payload))
        response = yield self.wait_response(ws)
        self.assertIn('op', response)
        self.assertIn('app_list', response)
        ws.close()

    @gen_test
    def test_subscribe(self):
        ws = yield self.ws_connect()
        payload = {
            'op': 'subscribe',
            'app_id': 'TEST_APP_ID',
        }
        ws.write_message(json_encode(payload))
        response = yield self.wait_response(ws)
        self.assertDictEqual(payload, response)
        ws.close()

    @gen_test
    def test_history(self):
        ws = yield self.ws_connect()
        ws.write_message(json_encode({
            'op': 'clear',
        }))
        response = yield self.wait_response(ws)

        n_times = 5
        payload = {
            'op': 'insert',
            'app_id': 'TEST_APP_ID',
            'metrics': [{
                'source_id': 'TEST_SOURCE_ID',
                'tag': 'TEST_TAG',
                'time': 1234567890,
                'value': 486.,
            }],
            'messages': [{
                'source_id': 'TEST_SOURCE_ID',
                'level': 'INFO',
                'time': 1234567890,
                'message': 'TEST_MESSAGE',
            }],
            'events': [{
                'source_id': 'TEST_SOURCE_ID',
                'tag': 'TEST_TAG',
                'time': 1234567890,
                'type': 'START'
            }],
        }
        for _ in xrange(n_times):
            ws.write_message(json_encode(payload))

        ws.write_message(json_encode({
            'op': 'history',
            'app_id': 'TEST_APP_ID',
        }))
        response = yield self.wait_response(ws)
        expected_response = {
            'op': 'history',
            'app_id': payload['app_id'],
            'metrics': [payload['metrics'][0] for _ in xrange(n_times)],
            'messages': [payload['messages'][0] for _ in xrange(n_times)],
            'events': [payload['events'][0] for _ in xrange(n_times)],
        }

        self.assertDictEqual(expected_response, response)
        ws.close()

    @gen_test
    def test_clear(self):
        ws = yield self.ws_connect()
        payload = {
            'op': 'clear',
        }
        ws.write_message(json_encode(payload))
        response = yield self.wait_response(ws)
        self.assertDictEqual(payload, response)
        ws.close()

    @gen_test
    def test_undefined(self):
        ws = yield self.ws_connect()
        payload = {
            'op': 'undefined',
        }
        ws.write_message(json_encode(payload))
        response = yield self.wait_response(ws)
        self.assertIn('op', response)
        self.assertIn('error', response)
        self.assertEqual(payload['op'], response['op'])
        self.assertTrue(response['error'])
        ws.close()
