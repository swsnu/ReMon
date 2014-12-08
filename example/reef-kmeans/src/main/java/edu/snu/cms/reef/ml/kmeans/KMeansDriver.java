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

import com.microsoft.reef.io.network.nggroup.api.driver.CommunicationGroupDriver;
import com.microsoft.reef.io.network.nggroup.api.driver.GroupCommDriver;
import com.microsoft.reef.io.network.nggroup.impl.config.BroadcastOperatorSpec;
import com.microsoft.reef.io.network.nggroup.impl.config.ReduceOperatorSpec;
import edu.snu.cms.remon.collector.evaluator.RemonLogger;
import edu.snu.cms.remon.collector.evaluator.RemonStartTaskHandler;
import edu.snu.cms.remon.collector.evaluator.RemonStopTaskHandler;
import org.apache.reef.driver.context.ActiveContext;
import org.apache.reef.driver.evaluator.EvaluatorRequestor;
import org.apache.reef.driver.task.FailedTask;
import org.apache.reef.driver.task.TaskConfiguration;
import org.apache.reef.evaluator.context.parameters.ContextIdentifier;
import org.apache.reef.io.data.loading.api.DataLoadingService;
import org.apache.reef.io.serialization.SerializableCodec;
import edu.snu.cms.reef.ml.kmeans.groupcomm.names.*;
import edu.snu.cms.reef.ml.kmeans.groupcomm.subs.*;
import edu.snu.cms.reef.ml.kmeans.parameters.KMeansParameters;
import edu.snu.cms.reef.ml.kmeans.data.KMeansDataParser;
import edu.snu.cms.reef.ml.kmeans.utils.DataParseService;
import org.apache.reef.tang.Configuration;
import org.apache.reef.tang.Configurations;
import org.apache.reef.tang.Injector;
import org.apache.reef.tang.Tang;
import org.apache.reef.tang.annotations.Unit;
import org.apache.reef.tang.exceptions.InjectionException;
import org.apache.reef.task.events.TaskStart;
import org.apache.reef.wake.EventHandler;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Driver code for the K-means clustering REEF application.
 * This class is appropriate for setting up event handlers as well as configuring
 * Broadcast and Reduce operations for Group Communication.
 */
@Unit
public final class KMeansDriver {
  private final static Logger LOG = Logger.getLogger(KMeansDriver.class.getName());

  /**
   * Sub-id for Compute Tasks.
   * This object grants different IDs to each task
   * e.g. ComputeTask-0, ComputeTask-1, and so on.
   */
  private final AtomicInteger taskId = new AtomicInteger(0);

  /**
   * ID of the Context that goes under Controller Task.
   * This string is used to distinguish the Context that represents the Controller Task
   * from Contexts that go under Compute Tasks.
   */
  private String ctrlTaskContextId;

  /**
   * Object that sends requests to the resource manager
   */
  private final EvaluatorRequestor requestor;

  /**
   * Driver that manages Group Communication settings
   */
  private final GroupCommDriver groupCommDriver;

  /**
   *  Communication Group to work on
   */
  private final CommunicationGroupDriver commGroup;

  /**
   * Accessor for data loading service
   * Can check whether a evaluator is configured with the service or not.
   */
  private final DataLoadingService dataLoadingService;

  /**
   * Class that manages parameters specific to the k-means algorithm
   */
  private final KMeansParameters kMeansParameters;

  /**
   * This class is instantiated by TANG
   *
   * Constructor for Driver of k-means job.
   * Store various objects as well as configuring Group Communication with
   * Broadcast and Reduce operations to use.
   *
   * @param requestor object used to request for new evaluators to the resource manager
   * @param groupCommDriver manager for Group Communication configurations
   * @param dataLoadingService manager for Data Loading configurations
   * @param kMeansParameters parameter manager related specifically to the k-means algorithm
   */
  @Inject
  private KMeansDriver(final EvaluatorRequestor requestor,
                       final GroupCommDriver groupCommDriver,
                       final DataLoadingService dataLoadingService,
                       final KMeansParameters kMeansParameters) {
    this.requestor = requestor;
    this.groupCommDriver = groupCommDriver;
    this.dataLoadingService = dataLoadingService;
    this.kMeansParameters = kMeansParameters;

    this.commGroup = groupCommDriver.newCommunicationGroup(
      CommunicationGroup.class,
      dataLoadingService.getNumberOfPartitions() + 1);

    this.commGroup
      .addBroadcast(CtrlMsgBroadcast.class,
        BroadcastOperatorSpec.newBuilder()
          .setSenderId(KMeansControllerTask.TASK_ID)
          .setDataCodecClass(SerializableCodec.class)
          .build())
      .addBroadcast(CentroidBroadcast.class,
        BroadcastOperatorSpec.newBuilder()
          .setSenderId(KMeansControllerTask.TASK_ID)
          .setDataCodecClass(CentroidListCodec.class)
          .build())
      .addReduce(NewCentroidReduce.class,
        ReduceOperatorSpec.newBuilder()
          .setReceiverId(KMeansControllerTask.TASK_ID)
          .setDataCodecClass(MapOfIntVSumCodec.class)
          .setReduceFunctionClass(MapOfIntVSumReduceFunction.class)
          .build())
      .addReduce(InitialCetroidReduce.class,
        ReduceOperatorSpec.newBuilder()
          .setReceiverId(KMeansControllerTask.TASK_ID)
          .setDataCodecClass(VectorListCodec.class)
          .setReduceFunctionClass(VectorListReduceFunction.class)
          .build())
      .finalise();

  }

  final class ActiveContextHandler implements EventHandler<ActiveContext> {
    @Override
    public void onNext(final ActiveContext activeContext) {

      // Case 1: Evaluator configured with a Data Loading context has been given
      // We need to add a Group Communication context above this context.
      //
      // It would be better if the two services could go into the same context, but
      // the Data Loading API is currently constructed to add its own context before
      // allowing any other ones.
      if (!groupCommDriver.isConfigured(activeContext)) {
        Configuration groupCommContextConf = groupCommDriver.getContextConfiguration();
        Configuration groupCommServiceConf = groupCommDriver.getServiceConfiguration();
        Configuration finalServiceConf;

        if (dataLoadingService.isComputeContext(activeContext)) {
          LOG.log(Level.INFO, "Submitting GroupCommContext for ControllerTask to underlying context");
          ctrlTaskContextId = getContextId(groupCommContextConf);
          finalServiceConf = groupCommServiceConf;

        } else {
          LOG.log(Level.INFO, "Submitting GroupCommContext for ComputeTask to underlying context");

          // Add a Data Parse service with the Group Communication service
          final Configuration dataParseConf = DataParseService.getServiceConfiguration(KMeansDataParser.class);
          finalServiceConf = Configurations.merge(groupCommServiceConf, dataParseConf);

        }

        activeContext.submitContextAndService(groupCommContextConf, finalServiceConf);

      } else {
        final Configuration partialTaskConf;

        // Case 2: Evaluator configured with a Group Communication context has been given,
        //         representing a Controller Task
        // We can now place a Controller Task on top of the contexts.
        if (activeContext.getId().equals(ctrlTaskContextId)) {
          LOG.log(Level.INFO, "Submit ControllerTask");
          partialTaskConf = Configurations.merge(
            TaskConfiguration.CONF
              .set(TaskConfiguration.IDENTIFIER, KMeansControllerTask.TASK_ID)
              .set(TaskConfiguration.TASK, KMeansControllerTask.class)
              .set(TaskConfiguration.ON_SEND_MESSAGE, RemonLogger.class)
              .set(TaskConfiguration.ON_TASK_STARTED, RemonStartTaskHandler.class)
              .set(TaskConfiguration.ON_TASK_STOP, RemonStopTaskHandler.class)
              .build(),
            kMeansParameters.getCtrlTaskConfiguration());

          // Case 3: Evaluator configured with a Group Communication context has been given,
          //         representing a Compute Task
          // We can now place a Compute Task on top of the contexts.
        } else {
          LOG.log(Level.INFO, "Submit ComputeTask");
          partialTaskConf = Configurations.merge(
            TaskConfiguration.CONF
              .set(TaskConfiguration.IDENTIFIER, "CmpTask-" + taskId.getAndIncrement())
              .set(TaskConfiguration.TASK, KMeansComputeTask.class)
              .set(TaskConfiguration.ON_SEND_MESSAGE, RemonLogger.class)
              .set(TaskConfiguration.ON_TASK_STARTED, RemonStartTaskHandler.class)
              .set(TaskConfiguration.ON_TASK_STOP, RemonStopTaskHandler.class)
              .build(),
            kMeansParameters.getCompTaskConfiguration());
        }

        // add the Task to our communication group
        commGroup.addTask(partialTaskConf);
        final Configuration finalTaskConf = groupCommDriver.getTaskConfiguration(partialTaskConf);
        activeContext.submitTask(finalTaskConf);
      }
    }
  }

  /**
   * When a certain Compute Task fails, we add the Task back and let it participate in
   * Group Communication again. However if the failed Task is the Controller Task,
   * we just shut down the whole job because it's hard to recover the cluster centroid info.
   */
  final class FailedTaskHandler implements EventHandler<FailedTask> {
    @Override
    public void onNext(FailedTask failedTask) {
      LOG.info(failedTask.getId() + " has failed.");

      // Stop the whole job if the failed Task is the Compute Task
      if (failedTask.getActiveContext().get().getId().equals(ctrlTaskContextId)) {
        throw new RuntimeException("Controller Task failed; aborting job");
      }

      final Configuration partialTaskConf = Tang.Factory.getTang()
        .newConfigurationBuilder(
          TaskConfiguration.CONF
            .set(TaskConfiguration.IDENTIFIER, failedTask.getId() + "-R")
            .set(TaskConfiguration.TASK, KMeansComputeTask.class)
            .set(TaskConfiguration.ON_SEND_MESSAGE, RemonLogger.class)
            .set(TaskConfiguration.ON_TASK_STARTED, RemonStartTaskHandler.class)
            .set(TaskConfiguration.ON_TASK_STOP, RemonStopTaskHandler.class)
            .build())
        .build();

      // Re-add the failed Compute Task
      commGroup.addTask(partialTaskConf);

      final Configuration taskConf = groupCommDriver.getTaskConfiguration(partialTaskConf);

      failedTask.getActiveContext().get().submitTask(taskConf);

    }
  }

  /**
   * Return the ID of the given Context
   */
  private String getContextId(final Configuration contextConf) {
    try {
      final Injector injector = Tang.Factory.getTang().newInjector(contextConf);
      return injector.getNamedInstance(ContextIdentifier.class);
    } catch (final InjectionException e) {
      throw new RuntimeException("Unable to inject context identifier from context conf", e);
    }
  }
}
