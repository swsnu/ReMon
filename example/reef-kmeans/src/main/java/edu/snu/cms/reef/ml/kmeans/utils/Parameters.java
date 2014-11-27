/**
 * Copyright (C) 2014 Seoul National University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.snu.cms.reef.ml.kmeans.utils;

import org.apache.reef.tang.Configuration;

/**
 * Interface for managing command line parameters defined by user.
 * (Excluding default parameters such as timeout and inputDir.)
 */
public interface Parameters {

  /**
   * @return configuration containing parameter information for Driver
   */
  public Configuration getDriverConfiguration();

  /**
   * @return configuration containing parameter information for Compute Task
   */
  public Configuration getCompTaskConfiguration();

  /**
   * @return configuration containing parameter information for Controller Task
   */
  public Configuration getCtrlTaskConfiguration();
}
