#!/usr/bin/env python
import os.path
import random
import time
import tornado.escape
import tornado.ioloop
import tornado.httpserver
import tornado.web
from tornado.options import define, options

define('port', default=8080, help='run on the given port', type=int)

class Application(tornado.web.Application):
    def __init__(self):
        settings = {
            'static_path': os.path.join(os.path.dirname(__file__), 'static'),
        }
        handlers = [
            (r'/', MainHandler),
            (r'/api/', ApplicationHandler),
            (r'/api/evaluators', EvaluatorListHandler),
            (r'/api/evaluators/(\w+)/status', EvaluatorStatusHandler),
            (r'/api/evaluators/(\w+)/resources/(\w+)', EvaluatorResourceHandler),
            (r'/api/evaluators/(\w+)/timeseries/(\w+)', EvaluatorTimeseriesHandler),
            (r'/api/tasks', TaskListHandler),
            (r'/api/tasks/(\w+)/status', TaskStatusHandler),
            (r'/api/tasks/(\w+)/logs', TaskLogsHandler),
            (r'/api/control', ControlHandler),
        ]
        tornado.web.Application.__init__(self, handlers, **settings)


class BaseHandler(tornado.web.RequestHandler):
    pass


class MainHandler(BaseHandler):
    def get(self):
        self.render('index.html')


class ApplicationHandler(BaseHandler):
    def get(self):
        data = {
            'application_id': 'app_0',
            'application_name': 'HelloREEF',
            'support_aggregation': True,
            'resource_names': [{'resource': 'memory'}, {'resource': 'cpu'}],
        }
        json = tornado.escape.json_encode(data)
        self.write(json)


class EvaluatorListHandler(BaseHandler):
    def get(self):
        data = {'evaluators': [{'evaluator_id': 'ev_%d' % i} for i in xrange(5)]}
        json = tornado.escape.json_encode(data)
        self.write(json)


class EvaluatorStatusHandler(BaseHandler):
    def get(self, evaluator_id):
        task_id = 'task_' + evaluator_id
        data = {'evaluator_id': evaluator_id, 'status': 'AVAILABLE', 'assigned_task_id': task_id}
        json = tornado.escape.json_encode(data)
        self.write(json)


class EvaluatorResourceHandler(BaseHandler):
    def get(self, evaluator_id, resource_name):
        current_time = int(time.time())
        point = {'time': current_time, 'value': random.random()}
        data = {
            'evaluator_id': evaluator_id,
            'resource': resource_name,
            'point': point,
        }
        json = tornado.escape.json_encode(data)
        self.write(json)


class EvaluatorTimeseriesHandler(BaseHandler):
    def get(self, evaluator_id, resource_name):
        current_time = int(time.time())
        points = [{'time': current_time + 1000 * t, 'value': random.random()} \
                    for t in xrange(-19, 0)]
        data = {
            'evaluator_id': evaluator_id,
            'resource': resource_name,
            'points': points,
        }
        json = tornado.escape.json_encode(data)
        self.write(json)


class TaskListHandler(BaseHandler):
    def get(self):
        data = {'tasks': [{'id': 'task_ev_%d' % i} for i in xrange(5)]}
        json = tornado.escape.json_encode(data)
        self.write(json)


class TaskStatusHandler(BaseHandler):
    def get(self, task_id):
        data = {'task_id': task_id, 'status': 'RUNNING'}
        json = tornado.escape.json_encode(data)
        self.write(json)


class TaskLogsHandler(BaseHandler):
    def get(self, task_id):
        current_time = int(time.time())
        logs = [{
            'time': current_time - 20000,
            'message': 'Hello, world!',
            'source': task_id[5:],
            'level': 'DEBUG',
            'tags': [{'tag_name': 'Hello'}, {'tag_name': 'Test'}],
        }, {
            'time': current_time - 10000,
            'message': 'Nice to meet you.',
            'source': task_id[5:],
            'level': 'DEBUG',
            'tags': [{'tag_name': 'Test'}],
        }]
        data = {
            'task_id': task_id,
            'logs': logs,
        }
        json = tornado.escape.json_encode(data)
        self.write(json)


class ControlHandler(BaseHandler):
    def post(self):
        command = self.get_argument('command')
        data = {'result': 'helloworld'}
        json = tornado.escape.json_encode(data)
        self.write(json)


if __name__ == '__main__':
    tornado.options.parse_command_line()
    httpserver = tornado.httpserver.HTTPServer(Application())
    httpserver.listen(options.port)
    tornado.ioloop.IOLoop().instance().start()

