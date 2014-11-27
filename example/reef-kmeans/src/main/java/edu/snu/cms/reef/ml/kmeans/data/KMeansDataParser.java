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
package edu.snu.cms.reef.ml.kmeans.data;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;
import org.apache.reef.io.data.loading.api.DataSet;
import org.apache.reef.io.network.util.Pair;
import edu.snu.cms.reef.ml.kmeans.utils.DataParser;
import edu.snu.cms.reef.ml.kmeans.utils.ParseException;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Parser class for parsing data to be used with the K-means Clustering REEF job.
 * Returns a pair of two lists: a list of initial centroids and a list of
 */
public final class KMeansDataParser implements DataParser<Pair<List<Vector>, List<Vector>>> {
  private final static Logger LOG = Logger.getLogger(KMeansDataParser.class.getName());

  private final DataSet<LongWritable, Text> dataSet;
  private Pair<List<Vector>, List<Vector>> result;
  private ParseException parseException;

  @Inject
  public KMeansDataParser(final DataSet<LongWritable, Text> dataSet) {
    this.dataSet = dataSet;
  }

  @Override
  public final Pair<List<Vector>, List<Vector>> get() throws ParseException {
    if (result == null) {
      parse();
    }

    if (parseException != null) {
      throw parseException;
    }

    return result;
  }

  @Override
  public final void parse() {
    List<Vector> centroids = new ArrayList<>();
    List<Vector> points = new ArrayList<>();

    for (final Pair<LongWritable, Text> keyValue : dataSet) {
      String[] split = keyValue.second.toString().trim().split("\\s+");
      if (split.length == 0) {
        continue;
      }

      if (split[0].equals("*")) {
        final Vector centroid = new DenseVector(split.length - 1);
        try {
          for (int i = 1; i < split.length; i++) {
            centroid.set(i - 1, Double.valueOf(split[i]));
          }
          centroids.add(centroid);

        } catch (final NumberFormatException e) {
          parseException = new ParseException("Parse failed: numbers should be DOUBLE");
          return;
        }

      } else {
        final Vector data = new DenseVector(split.length);
        try {
          for (int i = 0; i < split.length; i++) {
            data.set(i, Double.valueOf(split[i]));
          }
          points.add(data);

        } catch (final NumberFormatException e) {
          parseException = new ParseException("Parse failed: numbers should be DOUBLE");
          return;
        }
      }

      result = new Pair<>(centroids, points);
    }
  }
}
