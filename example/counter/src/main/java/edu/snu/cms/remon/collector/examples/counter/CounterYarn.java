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
import edu.snu.cms.remon.collector.driver.RemonMessenger;
import org.apache.commons.cli.ParseException;
import org.apache.reef.client.DriverConfiguration;
import org.apache.reef.client.DriverLauncher;
import org.apache.reef.client.LauncherStatus;
import org.apache.reef.runtime.yarn.client.YarnClientConfiguration;
import org.apache.reef.tang.Configuration;
import org.apache.reef.tang.Configurations;
import org.apache.reef.tang.exceptions.InjectionException;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.reef.tang.formats.CommandLine.parseToConfiguration;

/**
 * The Client for Dummy counter example running on YARN.
 */
public final class CounterYarn {

  private static final Logger LOG = Logger.getLogger(CounterYarn.class.getName());

  /**
   * Number of milliseconds to wait for the job to complete.
   */
  private static final int JOB_TIMEOUT = 30000; // 30 sec.


  /**
   * @return the configuration of the CounterREEF driver.
   * @param commandLineConf
   */
  private static Configuration getDriverConfiguration(Configuration commandLineConf) {
    return Configurations.merge(commandLineConf, DriverConfiguration.CONF
      .set(DriverConfiguration.GLOBAL_LIBRARIES, CounterYarn.class.getProtectionDomain().getCodeSource().getLocation().getFile())
      .set(DriverConfiguration.DRIVER_IDENTIFIER, "CounterREEF")
      .set(DriverConfiguration.ON_DRIVER_STARTED, CounterDriver.StartHandler.class)
      .set(DriverConfiguration.ON_EVALUATOR_ALLOCATED, CounterDriver.EvaluatorAllocatedHandler.class)
      .set(DriverConfiguration.ON_TASK_MESSAGE, RemonMessenger.class)
      .build());
  }

  /**
   * Start CounterREEF job. Runs method runCounterREEF().
   *
   * @param args command line parameters.
   * @throws org.apache.reef.tang.exceptions.BindException      configuration error.
   * @throws org.apache.reef.tang.exceptions.InjectionException configuration error.
   */
  public static void main(final String[] args) throws InjectionException, ParseException {
    final Configuration commandLineConf = parseToConfiguration(args, Collector.MonitorAddress.class);

    final LauncherStatus status = DriverLauncher
      .getLauncher(YarnClientConfiguration.CONF.build())
      .run(getDriverConfiguration(commandLineConf), JOB_TIMEOUT);
    LOG.log(Level.INFO, "REEF job completed: {0}", status);
  }
}
