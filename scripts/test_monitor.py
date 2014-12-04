import os
import subprocess
import sys
from optparse import OptionParser

usage = "usage : %prog [options] "
parser = OptionParser(usage=usage)
parser.add_option("-n","--no-coverage",dest="coverage",action="store_false",help="don't run coverage test",default=True)

(option,args) = parser.parse_args()

if len(args) > 1:
  parser.print_help()
  sys.exit(2)

os.chdir('../monitor')

ret = 0

if option.coverage:
  ret = subprocess.call(['coverage', 'run', '-m', 'tornado.testing', '--verbose', 'tests.runtests'])
  if ret == 0:
    subprocess.call(['coverage', 'report', '-m', '--include=app*'])

else :
  ret = subprocess.call(['python', '-m', 'tornado.testing', '--verbose', 'tests.runtests'])

sys.exit(ret)
