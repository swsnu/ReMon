import os
import subprocess
import sys

os.chdir('../example/reef-kmeans/bin')

SELF_JAR = '../target/cms-reef-tutorial-1.0-shaded.jar'

LOGGING_CONFIG = '-Djava.util.logging.config.class=org.apache.reef.util.logging.Config'

YARN_HOME = os.environ['YARN_HOME']

CLASSPATH = YARN_HOME + '/share/hadoop/common/*:' + YARN_HOME + '/share/hadoop/common/lib/*:' + YARN_HOME + '/share/hadoop/yarn/*:' + \
    YARN_HOME + '/share/hadoop/hdfs/*:' + YARN_HOME + '/share/hadoop/mapreduce/lib/*:' + YARN_HOME + '/share/hadoop/mapreduce/*'

YARN_CONF_DIR = YARN_HOME + '/etc/hadoop'

KMEANS = 'edu.snu.cms.reef.ml.kmeans.KMeansREEF'


callarg = [
    "java",
    "-cp",
    YARN_CONF_DIR +
    ':' +
    SELF_JAR +
    ':' +
    CLASSPATH,
    LOGGING_CONFIG,
    KMEANS]
callarg.extend(sys.argv[1:])

print callarg

subprocess.call(callarg)
