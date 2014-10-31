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
            (r'/evaluators', EvaluatorListHandler),
            (r'/evaluators/(\w+)/status', EvaluatorStatusHandler),
            (r'/evaluators/(\w+)/timeseries/(\w+)', EvaluatorTimeseriesHandler),
            (r'/tasks', TaskListHandler),
            (r'/tasks/(\w+)/status', TaskStatusHandler),
            (r'/tasks/(\w+)/run', TaskRunHandler),
        ]
        tornado.web.Application.__init__(self, handlers, **settings)


class BaseHandler(tornado.web.RequestHandler):
    pass


class MainHandler(BaseHandler):
    def get(self):
        self.render('index.html')


class EvaluatorListHandler(BaseHandler):
    def get(self):
        data = {'evaluators': [{'id': 'ev_%d' % i} for i in xrange(5)]}
        json = tornado.escape.json_encode(data)
        self.write(json)


class EvaluatorStatusHandler(BaseHandler):
    def get(self, evaluator_id):
        task_id = 'task_' + evaluator_id
        data = {'evaluator_id': evaluator_id, 'status': 'AVAILABLE', 'assigned_task_id': task_id}
        json = tornado.escape.json_encode(data)
        self.write(json)


class EvaluatorTimeseriesHandler(BaseHandler):
    def get(self, evaluator_id, resource_name):
        current_time = int(time.time())
        series = [{'time': current_time + 1000*t, 'value': random.random()} for t in xrange(-19, 0)]
        data = {'metric': resource_name, 'points': series}
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


class TaskRunHandler(BaseHandler):
    def post(self, task_id):
        command = self.get_argument('command')
        data = {'result': 'helloworld'}
        json = tornado.escape.json_encode(data)
        self.write(json)


if __name__ == '__main__':
    tornado.options.parse_command_line()
    httpserver = tornado.httpserver.HTTPServer(Application())
    httpserver.listen(options.port)
    tornado.ioloop.IOLoop().instance().start()

