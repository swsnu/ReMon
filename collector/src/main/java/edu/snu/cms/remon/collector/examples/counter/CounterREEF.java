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
import org.apache.commons.cli.ParseException;
import org.apache.reef.client.DriverConfiguration;
import org.apache.reef.client.DriverLauncher;
import org.apache.reef.client.LauncherStatus;
import org.apache.reef.runtime.local.client.LocalRuntimeConfiguration;
import org.apache.reef.tang.Configuration;
import org.apache.reef.tang.Configurations;
import org.apache.reef.tang.Tang;
import org.apache.reef.tang.exceptions.BindException;
import org.apache.reef.tang.exceptions.InjectionException;
import org.apache.reef.tang.formats.CommandLine;
import org.apache.reef.util.EnvironmentUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.reef.tang.formats.CommandLine.parseToConfiguration;

/**
 * The Client for Hello REEF example.
 */
public final class CounterREEF {

  private static final Logger LOG = Logger.getLogger(CounterREEF.class.getName());

  /**
   * Number of milliseconds to wait for the job to complete.
   */
  private static final int JOB_TIMEOUT = 10000; // 10 sec.

  /**
   * @return the configuration of the HelloREEF driver.
   * @param commandLineConf
   */
  public static Configuration getDriverConfiguration(Configuration commandLineConf) {
    return Configurations.merge(commandLineConf, DriverConfiguration.CONF
      .set(DriverConfiguration.GLOBAL_LIBRARIES, EnvironmentUtils.getClassLocation(CounterDriver.class))
      .set(DriverConfiguration.DRIVER_IDENTIFIER, "CounterREEF")
      .set(DriverConfiguration.ON_DRIVER_STARTED, CounterDriver.StartHandler.class)
      .set(DriverConfiguration.ON_EVALUATOR_ALLOCATED, CounterDriver.EvaluatorAllocatedHandler.class)
      .set(DriverConfiguration.ON_TASK_MESSAGE, Collector.heartbeatBypassHandler.class)
      .build());
  }

  public static LauncherStatus runCounterReef(final Configuration runtimeConf, Configuration commandLineConf, final int timeOut)
    throws BindException, InjectionException {
    final Configuration driverConf = getDriverConfiguration(commandLineConf);
    return DriverLauncher.getLauncher(runtimeConf).run(driverConf);
  }

  /**
   * Start Hello REEF job. Runs method runCounterReef().
   *
   * @param args command line parameters.
   * @throws BindException      configuration error.
   * @throws InjectionException configuration error.
   */
  public static void main(final String[] args) throws BindException, InjectionException, ParseException {
    final Configuration commandLineConf = parseToConfiguration(args, Collector.MonitorAddress.class);

    final Configuration runtimeConfiguration = LocalRuntimeConfiguration.CONF
      .set(LocalRuntimeConfiguration.NUMBER_OF_THREADS, 2)
      .build();
    final LauncherStatus status = runCounterReef(runtimeConfiguration, commandLineConf, JOB_TIMEOUT);
    LOG.log(Level.INFO, "REEF job completed: {0}", status);
  }
}
