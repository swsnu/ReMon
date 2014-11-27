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
package edu.snu.cms.reef.ml.kmeans;

import org.apache.mahout.math.DenseVector;
import edu.snu.cms.reef.ml.kmeans.data.Centroid;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for Centroid
 */
public final class CentroidTest {
  @Test
  public final void testIsDeepCopyConstructor() {
    final Centroid centroidA = new Centroid(0, new DenseVector(0));
    final Centroid centroidB = new Centroid(centroidA);
    assertTrue(centroidA.vector != centroidB.vector);
    assertEquals(centroidA.getClusterId(), centroidB.getClusterId());
  }
}