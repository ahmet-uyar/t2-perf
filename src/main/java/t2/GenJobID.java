package t2;

import edu.iu.dsc.tws.api.util.JobIDUtils;

public final class GenJobID {

  private GenJobID() { }

  public static void main(String[] args) {
    if (args.length != 1) {
      throw new RuntimeException("You must provide jobName as a parameter");
    }

    String userName = "au";
    String jobName = args[0];
    String jobID = JobIDUtils.generateJobID(jobName, userName);
    System.out.println(jobID);
  }
}

