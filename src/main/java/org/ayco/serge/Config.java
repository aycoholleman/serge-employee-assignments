package org.ayco.serge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import static java.lang.Integer.parseInt;

final class Config {


  static Config getInstance() {
    String path = System.getProperty("user.home") + "/config.properties";
    File file = new File(path);
    Properties props = new Properties();
    try {
      props.load(new FileInputStream(file));
    } catch (FileNotFoundException e0) {
      throw new RuntimeException("Missing configuration file: " + path);
    } catch (IOException e1) {
      throw new RuntimeException(e1);
    }
    if (!props.containsKey("FTE_MAX_DIFF_MEAN")) {
      throw new RuntimeException("Missing configuration setting FTE_MAX_DIFF_MEAN");
    }
    int fteMaxDiffMean = parseInt(props.getProperty("FTE_MAX_DIFF_MEAN"));
    if (!props.containsKey("AGE_MAX_DIFF_MEAN")) {
      throw new RuntimeException("Missing configuration setting AGE_MAX_DIFF_MEAN");
    }
    int ageMaxDiffMean = parseInt(props.getProperty("AGE_MAX_DIFF_MEAN"));
    return new Config(fteMaxDiffMean, ageMaxDiffMean);
  }

  final int fteMaxDiffMean;
  final int ageMaxDiffMean;

  private Config(int fteMaxDiffMean, int ageMaxDiffMean) {
    this.fteMaxDiffMean = fteMaxDiffMean;
    this.ageMaxDiffMean = ageMaxDiffMean;
  }

  public String toString() {
    return new StringBuilder(50)
          .append("{fteMaxDiffMean=")
          .append(fteMaxDiffMean)
          .append(";ageMaxDiffMean=")
          .append(ageMaxDiffMean)
          .append("}")
          .toString();
  }

}
