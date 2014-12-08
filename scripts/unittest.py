import os
import subprocess
import sys
import webbrowser
from optparse import OptionParser

usage = "usage : %prog [options] "
parser = OptionParser(usage=usage)
parser.add_option(
    "-v",
    "--no-coverage",
    dest="coverage",
    action="store_false",
    help="don't run coverage test",
    default=True)
parser.add_option(
    "-c",
    "--collector",
    dest="collector",
    action="store_true",
    help="run collector test",
    default=False)
parser.add_option(
    "-m",
    "--monitor",
    dest="monitor",
    action="store_true",
    help="run monitor test",
    default=False)
parser.add_option(
    "-a",
    "--all",
    dest="alltest",
    action="store_true",
    help="run collector and monitor test",
    default=False)

(options, args) = parser.parse_args()

if not (options.collector or options.monitor or options.alltest):
    parser.print_help()
    sys.exit(2)


monitor_test = 0
collector_test = 0

if options.collector or options.alltest:
    os.chdir('../../../../reef')
    subprocess.call(['git', 'pull'])
    subprocess.call(['mvn', 'clean', 'install', '-DskipTests'])
    os.chdir('../jobs/ReMon Build/workspace/collector')
    print "##### Collector Unit Test #####"
    if options.coverage:
        subprocess.call(['mvn', 'clean', 'install'])
        collector_test = subprocess.call(['mvn', 'cobertura:cobertura'])
        webbrowser.open('target/site/cobertura/index.html')
    else:
        collector_test = subprocess.call(['mvn', 'clean', 'install'])

if options.monitor or options.alltest:
    os.chdir('../monitor')
    print "##### Monitor Unit Test #####"
    if options.coverage:
        monitor_test = subprocess.call(
            ['coverage', 'run', '-m', 'tornado.testing', '--verbose', 'tests.runtests'])
        if monitor_test == 0:
            subprocess.call(['coverage', 'report', '-m', '--include=app*'])

    else:
        monitor_test = subprocess.call(
            ['python', '-m', 'tornado.testing', '--verbose', 'tests.runtests'])

sys.exit(monitor_test | collector_test)
