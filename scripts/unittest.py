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
parser.add_option(
    "-j",
    "--jenkins",
    dest="jenkins",
    action="store_true",
    help="run test as jenkins mode",
    default=False)

(options, args) = parser.parse_args()

if options.jenkins:
    options.alltest = True
    options.coverage = False

if options.alltest:
    options.collector = True
    options.monitor = True

if not (options.collector or options.monitor):
    parser.print_help()
    sys.exit(2)

monitor_test = 0
collector_test = 0

if options.collector:
    os.chdir('../collector')
    print "##### Collector Unit Test #####"
    if options.coverage:
        subprocess.call(['mvn', 'clean', 'install'])
        collector_test = subprocess.call(['mvn', 'cobertura:cobertura'])
        webbrowser.open('target/site/cobertura/index.html')
    else:
        collector_test = subprocess.call(['mvn', 'clean', 'install'])
        if collector_test != 0 and options.jenkins:
            # Rebuild REEF and test again
            os.chdir('../../../../reef')
            subprocess.call(['git', 'pull'])
            subprocess.call(['mvn', 'clean', 'install', '-DskipTests'])
            os.chdir('../jobs/ReMon Build/workspace/collector')
            collector_test = subprocess.call(['mvn', 'clean', 'install'])
    if collector_test == 0:
        print "Collector unit test passed"
    else:
        print "Collector unit test failed"

if options.monitor:
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
    if monitor_test == 0:
        print "Monitor unit test passed"
    else:
        print "Monitor unit test failed"


if monitor_test|collector_test == 0:
    print "Unit test result : passed"
else:
    print "Unit test result : failed"
sys.exit(monitor_test | collector_test)
