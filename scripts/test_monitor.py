import os
import subprocess
import sys

os.chdir('../monitor')
ret = subprocess.call(['python', '-m', 'tornado.testing', '--verbose', 'tests.runtests'])

if ret == 0:
  subprocess.call(['coverage', 'run', '-m', 'tornado.testing', '--verbose', 'tests.runtests'], stdout=open(os.devnull, 'wb'), stderr=open(os.devnull, 'wb'))
  subprocess.call(['coverage', 'report', '-m', '--include=app*'])

sys.exit(ret)
