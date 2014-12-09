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

import com.microsoft.reef.io.network.group.operators.*;
import com.microsoft.reef.io.network.nggroup.api.task.CommunicationGroupClient;
import com.microsoft.reef.io.network.nggroup.api.task.GroupCommClient;
import edu.snu.cms.remon.collector.EventType;
import edu.snu.cms.remon.collector.evaluator.RemonLogger;
import org.apache.mahout.math.Vector;
import edu.snu.cms.reef.ml.kmeans.data.Centroid;
import edu.snu.cms.reef.ml.kmeans.groupcomm.names.*;
import edu.snu.cms.reef.ml.kmeans.parameters.ControlMessage;
import edu.snu.cms.reef.ml.kmeans.parameters.MaxIterations;
import edu.snu.cms.reef.ml.kmeans.converge.KMeansConvergenceCondition;
import edu.snu.cms.reef.ml.kmeans.data.VectorSum;
import org.apache.reef.tang.annotations.Parameter;
import org.apache.reef.task.Task;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is in charge of the control flow of the k-means clustering
 * and also performs a little computing to determine when to stop
 * iteration.
 */
public final class KMeansControllerTask implements Task {
  private static final Logger LOG = Logger.getLogger(KMeansControllerTask.class.getName());


  /**
   * Task ID used for configuring Group Communication
   */
  public static final String TASK_ID = "ControllerTask";

  /**
   * Accessor for retrieving communicator members for Group Communication
   */
  private final CommunicationGroupClient communicationGroupClient;

  /**
   * Receive initial cluster centroids from Compute Tasks
   * since no info on centroids are available to Controller Task at job start
   * (initial centroids are read as data input, which are directed to Compute Tasks)
   */
  private final Reduce.Receiver<List<Vector>> initialCentroidReduce;

  /**
   * Send control messages to Compute Tasks on what to do
   * e.g. TERMINATE, COMPUTE
   */
  private final Broadcast.Sender<ControlMessage> ctrlMsgBroadcast;

  /**
   * Send centroid information to Compute Tasks at the start of each iteration
   */
  private final Broadcast.Sender<List<Centroid>> centroidBroadcast;

  /**
   * Receive computation results from Compute Tasks and
   * aggregate them to compute new centroids for next iteration
   */
  private final Reduce.Receiver<Map<Integer, VectorSum>> newCentroidReduce;

  /**
   * Check function to determine whether algorithm has converged or not.
   * This is separate from the default stop condition,
   * which is based on the number of iterations made.
   */
  private final KMeansConvergenceCondition convergenceCondition;

  /**
   * List of cluster centroids to distribute to Compute Tasks
   * Will be updated for each iteration
   */
  private final List<Centroid> centroids = new ArrayList<>();

  /**
   * Maximum number of iterations allowed before job stops
   */
  private final int maxIterations;
  private final RemonLogger logger;

  /**
   * This class is instantiated by TANG
   *
   * Constructs the Controller Task for k-means
   *
   * @param groupCommClient accessor for retrieving communicator members for Group Communication
   * @param convergenceCondition conditions for checking convergence of algorithm
   * @param maxIter maximum number of iterations allowed before job stops
   */
  @Inject
  public KMeansControllerTask(final GroupCommClient groupCommClient,
                              final KMeansConvergenceCondition convergenceCondition,
                              @Parameter(MaxIterations.class) final int maxIter,
                              final RemonLogger logger) {
    super();

    this.communicationGroupClient = groupCommClient.getCommunicationGroup(CommunicationGroup.class);
    this.initialCentroidReduce = communicationGroupClient.getReduceReceiver(InitialCetroidReduce.class);
    this.ctrlMsgBroadcast = communicationGroupClient.getBroadcastSender(CtrlMsgBroadcast.class);
    this.centroidBroadcast = communicationGroupClient.getBroadcastSender(CentroidBroadcast.class);
    this.newCentroidReduce = communicationGroupClient.getReduceReceiver(NewCentroidReduce.class);

    this.convergenceCondition = convergenceCondition;
    this.maxIterations = maxIter;
    this.logger = logger;
  }


  /**
   * Execute the k-means clustering algorithm
   */
  @Override
  public final byte[] call(final byte[] memento) throws Exception {
    LOG.log(Level.INFO, "ControllerTask.call() commencing....");

    // 1. Gather the initial centroids from Compute Tasks
    ctrlMsgBroadcast.send(ControlMessage.INITIATE);
    final List<Vector> listOfClustersNoId = initialCentroidReduce.reduce();

    // 2. Give IDs to centroids
    int clusterID = 0;
    for (final Vector vector : listOfClustersNoId) {
      centroids.add(new Centroid(clusterID++, vector));
    }

    for (int iteration = 0; iteration < maxIterations; iteration++) {
      logger.value("iteration", iteration);
      if (iterateKMeansClustering(iteration) == false) break;
    }

    ctrlMsgBroadcast.send(ControlMessage.TERMINATE);

    return null;
  }

  private final boolean iterateKMeansClustering(final int iteration) throws Exception {

    Map<Integer, VectorSum> map;
    long iterCount = 0;
    do {
      logger.event("iter"+iterCount, EventType.END);

      topologyChanged();

      ctrlMsgBroadcast.send(ControlMessage.COMPUTE);

      // 3. Broadcast the centroids for computing
      centroidBroadcast.send(centroids);

      // 4. Receive vector sums and compute new cluster centroids
      map = newCentroidReduce.reduce();

      logger.event("iter"+(iterCount++), EventType.END);
    } while (map == null);

    for (final Integer id : map.keySet()) {
      final VectorSum vectorSum = map.get(id);
      final Centroid newCentroid = new Centroid(id, vectorSum.computeVectorMean());

      centroids.set(id, newCentroid);
    }

    LOG.log(Level.INFO, "********* Centroids after {0} iterations*********", iteration + 1);
    LOG.log(Level.INFO, "" + centroids);
    LOG.log(Level.INFO, "********* Centroids after {0} iterations*********", iteration + 1);

    // 5. If threshold is violated, then repeat from 3
    return !convergenceCondition.checkConvergence(centroids);
  }

  /**
   * Check if group communication topology has changed, and updates it if it has.
   * @return true if topology has changed, false if not
   */
  private final boolean topologyChanged() {
    if (communicationGroupClient.getTopologyChanges().exist()) {
      communicationGroupClient.updateTopology();
      return true;

    } else {
      return false;
    }
  }
}

