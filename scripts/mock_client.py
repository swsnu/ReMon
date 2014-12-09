#!/usr/bin/env python
import random
import time
from tornado.escape import json_encode, json_decode
from websocket import create_connection


if __name__ == '__main__':
    address = 'ws://localhost:8000/websocket'
    ws = create_connection(address)
    ws.send(json_encode({
        'op': 'clear',
        'auth_key': '__REMON_ADMIN_PASSWORD__',
    }))
    time.sleep(1.)
    n_times = 10
    for t in xrange(n_times):
        message = {
            'op': 'insert',
            'app_id': 'TEST_APP_ID',
            'metrics': [{
                'tag': 'TEST_TAG',
                'value': random.random() + t,
                'source_id': 'TEST_SOURCE_ID',
                'time': int(time.time()) + t,
            }],
            'messages': [{
                'level': 'INFO',
                'message': 'TEST_INFO_MESSAGE',
                'source_id': 'TEST_SOURCE_ID',
                'time': int(time.time()) + t,
            }],
            'events': [{
                'type': 'START',
                'time': int(time.time()) + 2 * t,
                'tag': 'TEST_TAG_%d' % t,
                'source_id': 'TEST_SOURCE_ID',
            }, {
                'type': 'END',
                'time': int(time.time()) + 2 * t + 1,
                'tag': 'TEST_TAG_%d' % t,
                'source_id': 'TEST_SOURCE_ID',
            }],
        }
        ws.send(json_encode(message))
    ws.close()
