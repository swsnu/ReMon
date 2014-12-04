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
package edu.snu.cms.reef.ml.kmeans.converge;

import edu.snu.cms.remon.collector.evaluator.RemonLogger;
import org.apache.mahout.math.Vector;
import edu.snu.cms.reef.ml.kmeans.data.Centroid;
import edu.snu.cms.reef.ml.kmeans.parameters.ConvergenceThreshold;
import edu.snu.cms.reef.ml.kmeans.data.EuclideanDistance;
import org.apache.reef.tang.annotations.Parameter;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation of KMeansConvergenceCondition
 * Algorithm converges when every centroid has moved less than
 * a certain threshold after an iteration
 */
public final class KMeansConvergenceEuclidean implements KMeansConvergenceCondition {
  private static final Logger LOG = Logger.getLogger(KMeansConvergenceCondition.class.getName());

  Map<Integer, Centroid> oldCentroids;
  final double convergenceThreshold;
  final EuclideanDistance euclideanDistance;
  final RemonLogger rLogger;

  @Inject
  public KMeansConvergenceEuclidean(
      final EuclideanDistance euclideanDistance,
      @Parameter(ConvergenceThreshold.class) final double convergenceThreshold,
      final RemonLogger rLogger
      ) {
    this.euclideanDistance = euclideanDistance;
    this.convergenceThreshold = convergenceThreshold;
    this.rLogger = rLogger;
  }

  @Override
  public final boolean checkConvergence(Iterable<Centroid> centroids) {
    if (oldCentroids == null) {
      oldCentroids = new HashMap<>();

      for (final Centroid centroid : centroids) {
        oldCentroids.put(centroid.getClusterId(), centroid);
      }

      return false;
    }

    boolean hasConverged = true;
    for (final Centroid centroid : centroids) {
      Centroid oldCentroid = oldCentroids.get(centroid.getClusterId());
      final double dist = distance(centroid.vector, oldCentroid.vector);
      rLogger.value("distance", dist);
      LOG.log(Level.SEVERE, "Dist : " + dist);

      if (hasConverged
          && dist > convergenceThreshold) {
        hasConverged = false;
      }

      oldCentroids.put(centroid.getClusterId(), centroid);
    }

    return hasConverged;
  }

  public final double distance(Vector v1, Vector v2) {
    return euclideanDistance.distance(v1, v2);
  }
}
