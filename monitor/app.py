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
        settings = {
            'static_path': os.path.join(os.path.dirname(__file__), 'static'),
            'metric_table_prefix': 'metric_',
            'message_table_prefix': 'message_',
        }
        handlers = [
            (r'/', MainHandler),
            (r'/websocket', WebsocketHandler),
        ]
        db_name = options.mongo_uri.rsplit('/', 1)[-1]
        self.db = motor.MotorClient(options.mongo_uri)[db_name]
        self.mq = MessageQueue()
        tornado.web.Application.__init__(self, handlers, **settings)


class MainHandler(tornado.web.RequestHandler):

    def get(self):
        self.render('index.html')


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
        metric_table_prefix = self.settings['metric_table_prefix']
        message_table_prefix = self.settings['message_table_prefix']

        data = json_decode(message)
        op = data.get('op')
        app_id = data.get('app_id')

        if op == 'list':
            prefix = metric_table_prefix
            table_names = yield self.db.collection_names()
            app_names = filter(lambda n: n.startswith(prefix), table_names)
            app_list = map(lambda n: {'app_id': n[len(prefix):]}, app_names)
            self.write_message(json_encode({'op': op, 'app_list': app_list}))

        elif op == 'subscribe':
            self.mq.register(self, app_id)
            self.write_message(json_encode({'op': op}))

        elif op == 'metrics':
            metrics = data['metrics']
            assert(isinstance(metrics, list))
            for item in metrics:
                item['op'] = 'metrics'
                self.mq.publish(app_id, json_encode(item))
            table_name = metric_table_prefix + app_id
            yield self.db[table_name].insert(metrics)

        elif op == 'messages':
            messages = data['messages']
            assert(isinstance(messages, list))
            for item in messages:
                item['op'] = 'messages'
                self.mq.publish(app_id, json_encode(item))
            table_name = message_table_prefix + app_id
            yield self.db[table_name].insert(messages)

        elif op == 'history':
            for prefix in [metric_table_prefix, message_table_prefix]:
                cursor = self.db[prefix + app_id].find()
                while (yield cursor.fetch_next):
                    item = cursor.next_object()
                    if prefix == metric_table_prefix:
                        item['op'] = 'metrics'
                    else:
                        item['op'] = 'messages'
                    item.pop('_id', None)
                    self.write_message(json_encode(item))

        elif op == 'clear':
            for prefix in [metric_table_prefix, message_table_prefix]:
                yield self.db[prefix + app_id].remove()
            self.write_message(json_encode({'op': op}))

        else:
            raise NotImplementedError('Undefined opcode: %s' % op)


if __name__ == '__main__':
    tornado.options.parse_command_line()
    httpserver = tornado.httpserver.HTTPServer(Application())
    httpserver.listen(options.port)
    tornado.ioloop.IOLoop().instance().start()
