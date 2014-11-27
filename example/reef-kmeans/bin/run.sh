#!/bin/sh
#
# Copyright (C) 2014 Seoul National University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# EXAMPLE USAGE 
# ./run.sh -input /user/username/filename -timeout 100000 -convThr 0.01 -maxIter 20 -splitNum 4

# RUNTIME
SELF_JAR=../target/cms-reef-tutorial-1.0-shaded.jar

LOGGING_CONFIG='-Djava.util.logging.config.class=org.apache.reef.util.logging.Config'

CLASSPATH=$YARN_HOME/share/hadoop/common/*:$YARN_HOME/share/hadoop/common/lib/*:$YARN_HOME/share/hadoop/yarn/*:$YARN_HOME/share/hadoop/hdfs/*:$YARN_HOME/share/hadoop/mapreduce/lib/*:$YARN_HOME/share/hadoop/mapreduce/*

YARN_CONF_DIR=$YARN_HOME/etc/hadoop

KMEANS=edu.snu.cms.reef.ml.kmeans.KMeansREEF

CMD="java -cp $YARN_CONF_DIR:$SELF_JAR:$CLASSPATH $LOCAL_RUNTIME_TMP $LOGGING_CONFIG $KMEANS $*"
echo $CMD
$CMD # 2> /dev/null
