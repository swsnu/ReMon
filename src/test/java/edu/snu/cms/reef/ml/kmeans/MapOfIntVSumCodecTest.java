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
import edu.snu.cms.reef.ml.kmeans.groupcomm.subs.MapOfIntVSumCodec;
import edu.snu.cms.reef.ml.kmeans.data.VectorSum;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test class for testing codec.
 */
public final class MapOfIntVSumCodecTest {

  final MapOfIntVSumCodec codec = new MapOfIntVSumCodec();
  final Map<Integer, VectorSum> map = new HashMap<>();

  @Before
  public final void setUp() throws Exception {
    for (int i = 0; i < 1000; i++) {
      final Vector vector = new DenseVector((int)(Math.random() * 1000));
      for (int j = 0; j < vector.size(); j++) {
        vector.set(j, Math.random());
      }

      map.put(i, new VectorSum(vector, (int)(Math.random() * 1000)));
    }
  }

  /**
   * Encode a random Map of Integer and VectorSum, decode it right away,
   * then compare it with the original Map.
   */
  @Test
  public final void testEncodeDecode() {
    final Map<Integer, VectorSum> newMap = codec.decode(codec.encode(map));
    assertEquals(map.size(), newMap.size());

    for (final Integer id : map.keySet()) {
      final VectorSum vectorSum = map.get(id);
      final VectorSum newVectorSum = newMap.get(id);

      assertEquals(vectorSum.sum.size(), newVectorSum.sum.size());

      for (int i = 0; i < vectorSum.sum.size(); i++) {
        assertEquals(vectorSum.sum.get(i), newVectorSum.sum.get(i), 0.001);
      }

      assertEquals(vectorSum.count, newVectorSum.count);
    }
  }
}