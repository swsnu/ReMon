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
import edu.snu.cms.reef.ml.kmeans.groupcomm.subs.MapOfIntVSumReduceFunction;
import edu.snu.cms.reef.ml.kmeans.data.VectorSum;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test class for testing reduce function
 */
public final class MapOfIntVSumReduceTest {

  final MapOfIntVSumReduceFunction reducer = new MapOfIntVSumReduceFunction();
  final List<Map<Integer, VectorSum>> list = new ArrayList<>();
  final static int LIST_SIZE = 100;
  final static int MAP_SIZE = 100;
  final static int VEC_SIZE = 100;

  /**
   * Create a random List of Maps
   */
  @Before
  public final void setUp() throws Exception {
    for (int i = 0; i < LIST_SIZE; i++) {
      final Map<Integer, VectorSum> map = new HashMap<>();

      for (int j = 0; j < MAP_SIZE; j++) {
        final Vector vector = new DenseVector(VEC_SIZE);
        for (int k = 0; k < VEC_SIZE; k++) {
          vector.set(k, Math.random());
        }

        map.put(j, new VectorSum(vector, (int)(Math.random() * 100)));

      }

      list.add(map);
    }
  }

  /**
   * Reduce a random List of Maps and check if the result equals the expected Map.
   */
  @Test
  public final void testReduce() {
    final Map<Integer, VectorSum> expectedMap = new HashMap<>();
    for (int i = 0; i < MAP_SIZE; i++) {
      final List<VectorSum> vecSumList = new ArrayList<>();

      for (final Map<Integer, VectorSum> map : list) {
        vecSumList.add(map.get(i));
      }

      expectedMap.put(i, VectorSum.addAllSums(vecSumList));
    }

    final Map<Integer, VectorSum> resultMap = reducer.apply(list);
    assertEquals(resultMap.size(), expectedMap.size());
    for (final Integer i : expectedMap.keySet()) {
      for (int j = 0; j < VEC_SIZE; j++) {
        assertEquals(resultMap.get(i).sum.get(j), expectedMap.get(i).sum.get(j), 0.001);
      }
      assertEquals(resultMap.get(i).count, expectedMap.get(i).count);
    }

  }
}