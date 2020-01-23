package plainpods;

import t2.WorkerDelays;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

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
    String jobName = logsDir.getName();
    List<File> logFiles = new ArrayList<>(Arrays.asList(logsDir.listFiles()));
    // delete job-submit-time.txt file
    logFiles.removeIf(file -> file.toString().contains("job-submit-time"));
    int pods = logFiles.size();
    long jst = jobSubmitTime(jobLogsDir);

    Map<String, Long> delaysMap = new HashMap<>();

    for (File lf : logFiles) {
      String podName = lf.getName().substring(0, lf.getName().length() - 4);
      delaysMap.put(podName, workerDelay(lf, jst));
    }

    // write to delays file
    Path delayFile = Paths.get(delaysDir + "/delays.txt");
    List<String> outList = new LinkedList<>();
    outList.add(jobName + "\t" + pods);
    delaysMap.forEach((k, v) -> outList.add(k + "\t" + v));

    double maxPodStart =
        delaysMap.entrySet().stream().mapToLong(v -> v.getValue()).max().orElseThrow(NoSuchElementException::new) / 1000.0;

    outList.add("maxPodStart\t" + maxPodStart);
    try {
      Files.write(delayFile, outList, Charset.defaultCharset());
      System.out.println("Written delays to file: " + delayFile);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public static long workerDelay(File wFile, long jst) {
    List<String> lines = readFileLines(wFile);

    for (String line: lines) {
      if (line.contains("timestamp")) {
        String trimmedLine = line.trim();
        String ts = trimmedLine.substring(trimmedLine.lastIndexOf(" ") + 1);
        long delay = Long.parseLong(ts) - jst;
        return delay;
      }
    }

    return -1;
  }

  public static String readJobID() {
    String jobIDFile = System.getProperty("user.home") + "/.twister2/last-job-id.txt";
    return readFileLines(jobIDFile).get(0);
  }

  public static long jobSubmitTime(String jobLogsDir) {
    String jstFile = jobLogsDir + "/job-submit-time.txt";
    return Long.parseLong(readFileLines(jstFile).get(0));
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

  public static List<String> readFileLines(File file) {
    try {
      List<String> lines = Files.readAllLines(file.toPath());
      return lines;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
