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

import com.microsoft.reef.io.network.group.operators.Broadcast;
import com.microsoft.reef.io.network.group.operators.Reduce;
import com.microsoft.reef.io.network.nggroup.api.task.CommunicationGroupClient;
import com.microsoft.reef.io.network.nggroup.api.task.GroupCommClient;
import edu.snu.cms.remon.collector.EventType;
import edu.snu.cms.remon.collector.evaluator.RemonLogger;
import org.apache.mahout.math.Vector;
import org.apache.reef.io.network.util.Pair;
import edu.snu.cms.reef.ml.kmeans.data.Centroid;
import edu.snu.cms.reef.ml.kmeans.groupcomm.names.*;
import edu.snu.cms.reef.ml.kmeans.parameters.ControlMessage;
import edu.snu.cms.reef.ml.kmeans.data.VectorDistanceMeasure;
import edu.snu.cms.reef.ml.kmeans.data.VectorSum;
import edu.snu.cms.reef.ml.kmeans.utils.DataParser;
import org.apache.reef.task.Task;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is in charge of assigning the closest cluster to each point
 * and returning a vector sum of points for each cluster to KMeansControllerTask
 */
public final class KMeansComputeTask implements Task {
  private static final Logger LOG = Logger.getLogger(KMeansComputeTask.class.getName());

  /**
   * Parser object for input data that returns data assigned to this Task
   */
  private final DataParser<Pair<List<Vector>, List<Vector>>> dataParser;

  /**
   * Send the initial cluster centroids to Controller Task
   * since Controller Task has no information on centroids at job start
   * (initial centroids are read as data input, which are directed to Compute Tasks)
   */
  private final Reduce.Sender<List<Vector>> initialCentroidReduce;

  /**
   * Receive centroid information at the start of each iteration
   */
  private final Broadcast.Receiver<List<Centroid>> centroidBroadcast;

  /**
   * Receive control messages from Controller Task on what to do
   * e.g. TERMINATE, COMPUTE
   */
  private final Broadcast.Receiver<ControlMessage> ctrlMsgBroadcast;

  /**
   * Send computation results to Controller Task
   */
  private final Reduce.Sender<Map<Integer, VectorSum>> newCentroidReduce;

  /**
   * Definition of 'distance between points' for this job
   * Default measure is Euclidean distance
   */
  private final VectorDistanceMeasure distanceMeasure;

  /**
   * Points read from input data to work on
   */
  private List<Vector> points = new ArrayList<>();

  /**
   * Initial centroids read from input data to pass to Controller Task
   */
  private List<Vector> initialCentroids = new ArrayList<>();

  private RemonLogger logger;
  /**
   * This class is instantiated by TANG
   *
   * Constructs a single Compute Task for k-means
   *
   * @param dataParser object that parses input data and returns it by the appropriate data structure
   * @param groupCommClient accessor for retrieving communicator members for Group Communication
   * @param distanceMeasure distance measure to use to compute distances between points
   */
  @Inject
  KMeansComputeTask(final DataParser<Pair<List<Vector>, List<Vector>>> dataParser,
                    final GroupCommClient groupCommClient,
                    final VectorDistanceMeasure distanceMeasure,
                    final RemonLogger logger) {
    super();
    this.dataParser = dataParser;
    this.logger = logger;

    CommunicationGroupClient commGroupClient = groupCommClient.getCommunicationGroup(CommunicationGroup.class);
    this.initialCentroidReduce = commGroupClient.getReduceSender(InitialCetroidReduce.class);
    this.ctrlMsgBroadcast = commGroupClient.getBroadcastReceiver(CtrlMsgBroadcast.class);
    this.centroidBroadcast = commGroupClient.getBroadcastReceiver(CentroidBroadcast.class);
    this.newCentroidReduce = commGroupClient.getReduceSender(NewCentroidReduce.class);

    this.distanceMeasure = distanceMeasure;
  }

  /**
   * Perform the k-means clustering algorithm with KMeansControllerTask
   */
  @Override
  public final byte[] call(final byte[] memento) throws Exception {
    LOG.log(Level.INFO, "ComputeTask.call() commencing....");

    // 0. Read the points and initial centroids from input data
    initialCentroids = dataParser.get().first;
    points = dataParser.get().second;

    // 1. Start iteration
    boolean terminate = false;
    long iterCount = 0;
    while (!terminate) {
      logger.event("iter"+iterCount, EventType.START);
      switch (ctrlMsgBroadcast.receive()) {
        case TERMINATE:
          terminate = true;
          break;

        case INITIATE:
          // 2. Send the initial centroids to the Controller Task
          initialCentroidReduce.send(initialCentroids);
          break;

        case COMPUTE:
          iterateKMeansClustering();
          break;

        default:
          break;
      }
      logger.event("iter"+(iterCount++), EventType.END);
    }

    return null;
  }

  private final void iterateKMeansClustering() throws Exception {
    // 3. Receive the actual cluster centroids
    List<Centroid> clusters = centroidBroadcast.receive();


    // 4. Compute the nearest cluster centroid for each point
    final Map<Integer, VectorSum> map = new HashMap<>();
    for (final Vector vector : points) {
      double nearestClusterDist = Double.MAX_VALUE;
      int nearestClusterId = -1;

      for (int i = 0; i < clusters.size(); i++) {
        final double distance = distanceMeasure.distance(clusters.get(i).vector, vector);
        logger.value("distance", distance);
        if (nearestClusterDist > distance) {
          nearestClusterDist = distance;
          nearestClusterId = i;
        }
      }

      // 5. Compute vector sums for each cluster centroid
      if (map.containsKey(nearestClusterId) == false) {
        map.put(nearestClusterId, new VectorSum(vector, 1, true));
      } else {
        map.get(nearestClusterId).add(vector);
      }
    }

    // 6. Return the vector sums for each centroids to KMeansControllerTask
    newCentroidReduce.send(map);

    // 7. Rinse and repeat until termination of KMeansControllerTask
    return;
  }
}

