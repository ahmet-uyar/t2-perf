package t2;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public final class Delays {

  private Delays() { }

  public static void main(String[] args) {

    String jobLogsDir;
    String delaysDir;

    if (args.length == 2) {
      jobLogsDir = args[0];
      delaysDir = args[1];
    } else {
      System.out.println("Please provide jobLogsDir and delaysDir as parameters");
      return;
    }

    File logsDir = new File(jobLogsDir);
    String jobID = logsDir.getName();
    Path delayFile = Paths.get(delaysDir + "/" + jobID + ".txt");
    int workers = logsDir.listFiles().length - 2;
    long jst = jobSubmitTime(jobLogsDir);
    long ld = launchDelay(jobLogsDir);

    List<WorkerDelays> delaysList = new LinkedList<>();

    for (int i = 0; i < workers; i++) {
      String wlog = jobLogsDir + "/worker-" + i + ".log";
      delaysList.add(workerDelays(i, jst, wlog));
    }

    // write to delays file
    List<String> outList = new LinkedList<>();
    outList.add(jobID + "\t" + workers + "\tld\t" + ld);
    delaysList.forEach(wd -> outList.add(wd.toString()));
    try {
      Files.write(delayFile, outList, Charset.defaultCharset());
      System.out.println("Written delays to file: " + delayFile);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // write to summary file
    Path summaryFile = Paths.get(delaysDir + "/summary.txt");
    double ldSec = ld / 1000.0;
    double maxPodStart =
        delaysList.stream().mapToLong(v -> v.podStart).max().orElseThrow(NoSuchElementException::new) / 1000.0;

    double minPodStart =
        delaysList.stream().mapToLong(v -> v.podStart).min().orElseThrow(NoSuchElementException::new) / 1000.0;

    double avgAllJoins = delaysList.stream()
        .mapToLong(v -> v.allJoined).average().orElseThrow(NoSuchElementException::new) / 1000;

    String line = String.format(
        "%s\t%.1f\t%.1f\t%.1f\t%.1f\n", jobID, ldSec, minPodStart, maxPodStart, avgAllJoins);

    try {
      Files.write(summaryFile, line.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public static WorkerDelays workerDelays(int index, long jst, String wFile) {
    List<String> lines = readFileLines(wFile);

    long podStart = -1;
    long workerStart = -1;
    long allJoined = -1;

    for (String line: lines) {
      if (line.contains("timestamp")) {
        String trimmedLine = line.trim();
        String ts = trimmedLine.substring(trimmedLine.lastIndexOf(" ") + 1);
        long delay = Long.parseLong(ts) - jst;
        if (line.contains("PodStartTime")){
          podStart = delay;
        } else if (line.contains("workerStart")){
          workerStart = delay;
        } else if (line.contains("allWorkersJoined")){
          allJoined = delay;
        }
      }
    }

    return new WorkerDelays(index, podStart, workerStart, allJoined);
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
