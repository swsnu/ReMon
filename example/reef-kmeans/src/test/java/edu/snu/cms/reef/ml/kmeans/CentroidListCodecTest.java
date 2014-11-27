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
import org.apache.mahout.math.Vector;
import edu.snu.cms.reef.ml.kmeans.data.Centroid;
import edu.snu.cms.reef.ml.kmeans.groupcomm.subs.CentroidListCodec;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


/**
 * Test class for testing codec
 */
public final class CentroidListCodecTest {

  final CentroidListCodec codec = new CentroidListCodec();
  final List<Centroid> list = new ArrayList<>();

  @Before
  public final void setUp() {
    for (int j = 0; j < (int)(Math.random() * 1000); j++) {
      final Vector vector = new DenseVector((int)(Math.random() * 1000));
      for (int i = 0; i < vector.size(); i++) {
         vector.set(i, Math.random());
      }
      final Centroid centroid = new Centroid((int)(Math.random() * 1000000), vector);
      list.add(centroid);
    }
  }

  /**
   * Encode a random List of Centroid, decode it right away,
   * then compare it with the original List.
   */
  @Test
  public final void testEncodeDecode() throws Exception {
    final List<Centroid> newList = codec.decode(codec.encode(list));
    assertEquals(list.size(), newList.size());

    for (int i = 0; i < list.size(); i++) {
      assertEquals(list.get(i).getClusterId(), newList.get(i).getClusterId());

      for (int j = 0; j < list.get(i).vector.size(); j++) {
        assertEquals(list.get(i).vector.get(j), newList.get(i).vector.get(j), 0.001);
      }
    }
  }

}