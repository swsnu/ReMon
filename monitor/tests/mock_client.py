#!/usr/bin/env python
import random
import time
from tornado.escape import json_encode, json_decode
from websocket import create_connection


if __name__ == '__main__':
    address = 'ws://localhost:8000/websocket'
    ws = create_connection(address)
    n_times = 10
    for _ in xrange(n_times):
        message = {
            'app_id': 'TEST_APP_ID',
            'metrics': {
                'tag': 'TEST_TAG',
                'value': random.random(),
                'source_id': 'TEST_SOURCE_ID',
                'time': int(time.time()),
            }
        }
        ws.send(json_encode(message))
