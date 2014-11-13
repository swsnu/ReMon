/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package edu.snu.cms.remon.collector.examples.counter;

import edu.snu.cms.remon.collector.Collector;
import org.apache.reef.task.Task;
import javax.inject.Inject;

/**
 * DummyCounterTask. It sleeps 0.1 second for each iteration
 * and count up to 1000
 */
public final class CounterTask implements Task {
  @Inject
  CounterTask() {
  }

  @Override
  public final byte[] call(final byte[] memento) {
    for (int i = 0; i < 1000; i++) {
      // Register to Collector to track the changes of a variable.
      Collector.putData("getEvalId", "CounterTask#call#i", i);
      if (i == 999) {
        i = 0;
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    return null;
  }
}
