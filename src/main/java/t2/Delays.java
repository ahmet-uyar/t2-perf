package t2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class Delays {

  private Delays() { }

  public static void main(String[] args) {

    String jobLogsDir;

    if (args.length == 1) {
      jobLogsDir = args[0];
    } else {
      System.out.println("Please provide jobLogsDir as parameter");
      return;
    }

    File logsDir = new File(jobLogsDir);
    String jobID = logsDir.getName();
    int workers = logsDir.listFiles().length - 2;
    long jst = jobSubmitTime(jobLogsDir);
    long ld = launchDelay(jobLogsDir);

    System.out.println(jobID + "\t" + workers + "\tld\t" + ld);

    for (int i = 0; i < workers; i++) {
      String wlog = jobLogsDir + "/worker-" + i + ".log";
      String delays = workerDelays(jst, wlog);
      System.out.println(i + "\t" + delays);
    }

  }

  public static String workerDelays(long jst, String wFile) {
    List<String> lines = readFileLines(wFile);
    String delays = "";

    for (String line: lines) {
      if (line.contains("timestamp")) {
        String trimmedLine = line.trim();
        String ts = trimmedLine.substring(trimmedLine.lastIndexOf(" ") + 1);
        long delay = Long.parseLong(ts) - jst;
        delays += delay + "\t";
      }
    }

    return delays.trim();
  }

  public static String readJobID() {
    String jobIDFile = System.getProperty("user.home") + "/.twister2/last-job-id.txt";
    return readFileLines(jobIDFile).get(0);
  }

  public static long jobSubmitTime(String jobDir) {
    String jstFile = jobDir + "/jobSubmitTime.txt";
    return Long.parseLong(readFileLines(jstFile).get(0));
  }

  public static long launchDelay(String jobDir) {
    String ldFile = jobDir + "/launch-delay.txt";
    return Long.parseLong(readFileLines(ldFile).get(0));
  }

  public static List<String> readFileLines(String filename) {
    Path path = new File(filename).toPath();
    try {
      List<String> lines = Files.readAllLines(path);
      return lines;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
