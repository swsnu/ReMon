#!/usr/bin/env python
import tornado.ioloop
from tornado import gen
from tornado.escape import json_encode, json_decode
from tornado.websocket import websocket_connect
from tornado.testing import AsyncHTTPTestCase, gen_test
from datetime import timedelta

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
        message = {
            'app_id': 'younhaholic',
            'metrics': [{
                'source_id': 'younhapia',
                'tag': 'younha',
                'time': 1415895132,
                'value': 486.0,
            }]
        }
        ws.write_message(json_encode(message))
        response = yield ws.read_message()
        metric = message['metrics'][0]
        output = json_decode(response)
        self.assertEqual(metric.keys(), output.keys())
        for key in metric.keys():
            self.assertEqual(metric[key], output[key])
        ws.close()

    @gen_test
    def test_echo_without_register(self):
        ws = yield self.ws_connect()
        message = {
            'app_id': 'younhaholic',
            'metrics': [{
                'source_id': 'younhapia',
                'tag': 'younha',
                'time': 1415895132,
                'value': 486.0,
            }]
        }
        ws.write_message(json_encode(message))
        with self.assertRaises(gen.TimeoutError) as context:
            wait_time = timedelta(seconds=0.5)
            response = yield gen.with_timeout(wait_time, ws.read_message())
        ws.close()
