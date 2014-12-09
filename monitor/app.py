#!/usr/bin/env python
import motor
import os.path
import tornado.gen
import tornado.httpserver
import tornado.ioloop
import tornado.web
import tornado.websocket
from tornado.escape import json_decode, json_encode
from tornado.options import define, options

define('port', default=8000, help='run on the given port', type=int)
define('mongo_uri', default='mongodb://localhost:27017/remon', type=str)


class Application(tornado.web.Application):

    def __init__(self):
        base_dir = os.path.dirname(__file__)
        settings = {
            'static_path': os.path.join(base_dir, 'static'),
            'template_path': os.path.join(base_dir, 'templates'),
            'table_metrics_prefix': 'table_metrics_',
            'table_messages_prefix': 'table_messages_',
            'table_analytics': 'analytics',
        }
        handlers = [
            (r'/', MainHandler),
            (r'/analytics', AnalyticsHandler),
            (r'/websocket', WebsocketHandler),
        ]
        db_name = options.mongo_uri.rsplit('/', 1)[-1]
        self.db = motor.MotorClient(options.mongo_uri)[db_name]
        self.mq = MessageQueue()
        tornado.web.Application.__init__(self, handlers, **settings)


class MainHandler(tornado.web.RequestHandler):

    def get(self):
        self.render('index.html')


class AnalyticsHandler(tornado.web.RequestHandler):

    @tornado.gen.coroutine
    def get(self):
        db = self.application.db
        table_name = self.settings['table_analytics']
        rows = []
        cursor = db[table_name].find().sort('_id', -1)
        while (yield cursor.fetch_next):
            item = cursor.next_object()
            rows.append(item)
        self.render('analytics.html', rows=rows)


class MessageQueue(object):

    def __init__(self):
        self.clients = set()
        self.channels = {}  # client to app_id

    def subscribe(self, client):
        self.clients.add(client)

    def unsubscribe(self, client):
        self.clients.remove(client)

    def register(self, client, app_id):
        self.channels[client] = app_id

    def unregister(self, client):
        self.channels.pop(client)

    def publish(self, app_id, message):
        for client in self.clients:
            if app_id == self.channels.get(client):
                client.write_message(message)


class WebsocketHandler(tornado.websocket.WebSocketHandler):

    @property
    def db(self):
        return self.application.db

    @property
    def mq(self):
        return self.application.mq

    def open(self):
        self.mq.subscribe(self)

    def on_close(self):
        self.mq.unsubscribe(self)

    @tornado.gen.coroutine
    def on_message(self, message):
        data = json_decode(message)
        op = data.get('op')
        app_id = data.get('app_id')

        # Monitor user behavior using an analytics framework.
        # Access time is logged automatically.
        yield self.db[self.settings['table_analytics']].insert({
            'ip': self.request.remote_ip,
            'op': op,
        })

        if op == 'insert':
            self.mq.publish(app_id, message)

            metrics = data['metrics']
            assert(isinstance(metrics, list))
            prefix = self.settings['table_metrics_prefix']
            yield self.db[prefix + app_id].insert(metrics)

            messages = data['messages']
            assert(isinstance(messages, list))
            prefix = self.settings['table_messages_prefix']
            yield self.db[prefix + app_id].insert(messages)

        elif op == 'list':
            table_names = yield self.db.collection_names()
            table_names.sort()

            candidates = set()
            for name in table_names:
                prefix = self.settings['table_metrics_prefix']
                if name.startswith(prefix):
                    candidates.add(name[len(prefix):])

                prefix = self.settings['table_messages_prefix']
                if name.startswith(prefix):
                    candidates.add(name[len(prefix):])

            app_list = [{'app_id': app_id} for app_id in candidates]

            self.write_message(json_encode({
                'op': 'list',
                'app_list': app_list,
            }))

        elif op == 'subscribe':
            self.mq.register(self, app_id)
            self.write_message(message)

        elif op == 'history':
            response = {
                'op': 'history',
                'app_id': app_id,
                'metrics': [],
                'messages': [],
            }

            prefix = self.settings['table_metrics_prefix']
            cursor = self.db[prefix + app_id].find().sort('time', 1)
            while (yield cursor.fetch_next):
                item = cursor.next_object()
                item.pop('_id', None)
                response['metrics'].append(item)

            prefix = self.settings['table_messages_prefix']
            cursor = self.db[prefix + app_id].find().sort('time', 1)
            while (yield cursor.fetch_next):
                item = cursor.next_object()
                item.pop('_id', None)
                response['messages'].append(item)

            self.write_message(json_encode(response))

        elif op == 'clear':
            table_names = yield self.db.collection_names()
            for name in table_names:
                if name.startswith(self.settings['table_metrics_prefix']) or\
                   name.startswith(self.settings['table_messages_prefix']):
                    yield self.db[name].remove()
            self.write_message(message)

        else:
            self.write_message(json_encode({
                'op': op,
                'error': True,
                'error_reason': 'Undefined opcode: %s' % op,
            }))


if __name__ == '__main__':
    tornado.options.parse_command_line()
    httpserver = tornado.httpserver.HTTPServer(Application())
    httpserver.listen(options.port)
    tornado.ioloop.IOLoop().instance().start()
