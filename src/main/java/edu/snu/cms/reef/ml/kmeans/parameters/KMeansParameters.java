package edu.snu.cms.reef.ml.kmeans.parameters;

import edu.snu.cms.reef.ml.kmeans.utils.Parameters;
import org.apache.reef.tang.Configuration;
import org.apache.reef.tang.Tang;
import org.apache.reef.tang.annotations.Parameter;
import org.apache.reef.tang.formats.CommandLine;

import javax.inject.Inject;

/**
 * Class that manages parameters specific to the k-means clustering algorithm
 */
public final class KMeansParameters implements Parameters {

  private final double convThreshold;
  private final int maxIterations;

  @Inject
  private KMeansParameters(@Parameter(ConvergenceThreshold.class) final double convThreshold,
                           @Parameter(MaxIterations.class) final int maxIterations) {
    this.convThreshold = convThreshold;
    this.maxIterations = maxIterations;
  }

  public static void registerShortNameOfClass(CommandLine cl) {
    cl.registerShortNameOfClass(ConvergenceThreshold.class);
    cl.registerShortNameOfClass(MaxIterations.class);
  }

  @Override
  public Configuration getDriverConfiguration() {
    return Tang.Factory.getTang().newConfigurationBuilder()
        .bindNamedParameter(ConvergenceThreshold.class, String.valueOf(convThreshold))
        .bindNamedParameter(MaxIterations.class, String.valueOf(maxIterations))
        .build();
  }

  @Override
  public Configuration getCompTaskConfiguration() {
    return Tang.Factory.getTang().newConfigurationBuilder()
        .build();
  }

  @Override
  public Configuration getCtrlTaskConfiguration() {
    return Tang.Factory.getTang().newConfigurationBuilder()
        .bindNamedParameter(ConvergenceThreshold.class, String.valueOf(convThreshold))
        .bindNamedParameter(MaxIterations.class, String.valueOf(maxIterations))
        .build();
  }
}
