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
import edu.snu.cms.reef.ml.kmeans.data.VectorSum;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for VectorSum
 */
public final class VectorSumTest {

  @Test
  public final void testIsCopyConstructor() {
    final VectorSum vectorSumA = new VectorSum(new DenseVector(0), 0);
    final VectorSum vectorSumB = new VectorSum(vectorSumA);
    assertFalse(vectorSumA.sum == vectorSumB.sum);
    assertEquals(vectorSumA.count, vectorSumB.count);
  }

  /**
   * Add two random VectorSums with VectorSum.add(),
   * and then check if the result really is the addition of the two VectorSums.
   */
  @Test
  public final void testAddSum() {
    final int vectorSize = (int)(Math.random() * 1000);
    final Vector vectorA = new DenseVector(vectorSize);
    for (int i = 0; i < vectorSize; i++) {
      vectorA.set(i, Math.random());
    }
    final VectorSum vectorSumA = new VectorSum(vectorA, (int)(Math.random() * 1000));

    final Vector vectorB = new DenseVector(vectorSize);
    for (int i = 0; i < vectorSize; i++) {
      vectorB.set(i, Math.random());
    }
    final VectorSum vectorSumB = new VectorSum(vectorB, (int)(Math.random() * 1000));


    final Vector expectedVector = new DenseVector(vectorSize);
    for (int i = 0; i < vectorSize; i++) {
      expectedVector.set(i, vectorA.get(i) + vectorB.get(i));
    }
    final VectorSum expectedSum = new VectorSum(expectedVector, vectorSumA.count + vectorSumB.count);

    vectorSumA.add(vectorSumB);

    assertEquals(expectedSum.sum.size(), vectorSumA.sum.size());
    for (int i = 0; i < vectorSize; i++) {
      assertEquals(expectedSum.sum.get(i), vectorSumA.sum.get(i), 0.001);
    }
    assertEquals(expectedSum.count, vectorSumA.count);
  }

  /**
   * Add a random VectorSum and random Vector with VectorSum.add(),
   * and then check if the result really is the addition of the VectorSum and Vector.
   */
  @Test
  public final void testAddVector() {
    final int vectorSize = (int)(Math.random() * 1000);
    final Vector vectorA = new DenseVector(vectorSize);
    for (int i = 0; i < vectorSize; i++) {
      vectorA.set(i, Math.random());
    }
    final VectorSum vectorSumA = new VectorSum(vectorA, (int)(Math.random() * 1000));

    final Vector vectorB = new DenseVector(vectorSize);
    for (int i = 0; i < vectorSize; i++) {
      vectorB.set(i, Math.random());
    }


    final Vector expectedVector = new DenseVector(vectorSize);
    for (int i = 0; i < vectorSize; i++) {
      expectedVector.set(i, vectorA.get(i) + vectorB.get(i));
    }
    final VectorSum expectedSum = new VectorSum(expectedVector, vectorSumA.count + 1);

    vectorSumA.add(vectorB);

    assertEquals(expectedSum.sum.size(), vectorSumA.sum.size());
    for (int i = 0; i < vectorSize; i++) {
      assertEquals(expectedSum.sum.get(i), vectorSumA.sum.get(i), 0.001);
    }
    assertEquals(expectedSum.count, vectorSumA.count);
  }
}