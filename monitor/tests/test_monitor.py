#!/usr/bin/env python
from tornado import gen
from tornado.escape import json_encode
from tornado.ioloop import TimeoutError
from tornado.websocket import websocket_connect
from tornado.testing import AsyncHTTPTestCase, gen_test

from app import Application


class MainHandlerTest(AsyncHTTPTestCase):
    
    def get_app(self):
        return Application()

    def test_main_page(self):
        response = self.fetch(r'/')
        assert(response.code == 200)

    def test_not_found(self):
        response = self.fetch(r'/i-am-lost')
        assert(response.code == 404)

    def test_static_file(self):
        response = self.fetch(r'/static/js/remon.js')
        assert(response.code == 200)

