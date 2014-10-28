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
package edu.snu.cms.reef.ml.kmeans.groupcomm.subs;

import com.microsoft.reef.io.network.group.operators.Reduce;
import org.apache.mahout.math.Vector;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Reduce function for simply gathering all Vector Lists sent into one single List
 */
public final class VectorListReduceFunction implements Reduce.ReduceFunction<List<Vector>> {

  @Inject
  public VectorListReduceFunction() {
  }

  @Override
  public List<Vector> apply(Iterable<List<Vector>> elements) {
    final List<Vector> resultList = new ArrayList<>();
    for (final List<Vector> list : elements) {
      resultList.addAll(list);
    }

    return resultList;
  }
}
