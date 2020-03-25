package standalone;

import t2.Delays;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class MPIDelays {
  public static void main(String[] args) {

    String jobID;
    String jobLogsFile;
    String summaryFile = null;
    boolean outToConsole;

    if (args.length == 2) {
      jobID = args[0];
      jobLogsFile = args[1];
      outToConsole = true;
    } else if (args.length == 3) {
      jobID = args[0];
      jobLogsFile = args[1];
      summaryFile = args[2];
      outToConsole = false;
    } else {
      System.out.println("Please provide jobID jobLogsFile summaryFile (optional) as parameters");
      return;
    }

    String summary = calculateDelays(jobID, jobLogsFile, outToConsole);

    if (summaryFile != null) {
      Path summaryPath = Paths.get(summaryFile);
      try {
        Files.write(summaryPath, Collections.singleton(summary), Charset.defaultCharset(), StandardOpenOption.APPEND);
        System.out.println("Summary: " + summary);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  public static String calculateDelays(String jobID, String jobLogsFile, boolean outToConsole) {

    List<String> lines = Delays.readFileLines(jobLogsFile);
    Map<Integer, Long> mpiWorkerDelays = readDelays(lines, "mpiWorkerStartDelay");
    Map<Integer, Long> workerStartDelays = readDelays(lines, "workerStartDelay");
    Map<Integer, Long> allJoinedDelays = readDelays(lines, "allJoinedDelay");

//    System.out.println("mpiWorkerDelays");
//    System.out.println(mpiWorkerDelays);
//    System.out.println("workerStartDelays");
//    System.out.println(workerStartDelays);
//    System.out.println("allJoinedDelays");
//    System.out.println(allJoinedDelays);

    // check number of delays in each map
    if (mpiWorkerDelays.size() == workerStartDelays.size() && workerStartDelays.size() == allJoinedDelays.size()) {
//      System.out.println("Number of workers: " + mpiWorkerDelays.size());
    } else {
      System.out.println("Not all maps have the same size: ");
      System.out.println("size of mpiWorkerDelays: " + mpiWorkerDelays.size());
      System.out.println("size of workerStartDelays: " + workerStartDelays.size());
      System.out.println("size of allJoinedDelays: " + allJoinedDelays.size());
    }

    if (outToConsole) {
      // print delays,
      // first print header line: jobID   numberOfWorkers
      System.out.println(jobID + "\t" + mpiWorkerDelays.size());
      for (Map.Entry<Integer, Long> entry : mpiWorkerDelays.entrySet()) {
        System.out.println(entry.getKey() + "\t"
            + entry.getValue() + "\t"
            + workerStartDelays.get(entry.getKey()) + "\t"
            + allJoinedDelays.get(entry.getKey())
        );
      }
    }

    long maxMpiWorkerStart = mpiWorkerDelays.values().stream().mapToLong(d -> d).max().orElseThrow(NoSuchElementException::new);
    long maxWorkerStart = workerStartDelays.values().stream().mapToLong(d -> d).max().orElseThrow(NoSuchElementException::new);
    double avgAllJoined = allJoinedDelays.values().stream().mapToLong(d -> d).average().orElseThrow(NoSuchElementException::new);

    if (outToConsole) {
      String line = String.format(
          "summary\t%d\t%d\t%.1f", maxMpiWorkerStart, maxWorkerStart, avgAllJoined);
      System.out.println(line);
    }

    String summary = String.format(
        "%s\t%d\t%d\t%.1f", jobID, maxMpiWorkerStart, maxWorkerStart, avgAllJoined);

    return summary;
  }

  public static Map<Integer, Long> readDelays(List<String> lines, String delayStr) {

    TreeMap<Integer, Long> delays = new TreeMap<>();

    for (String line : lines) {
      if (line.contains(delayStr)) {
        String trimmedLine = line.trim();
        String words[] = trimmedLine.split(" ");
        long delay = Long.parseLong(words[words.length - 1]);
        int wID = Integer.parseInt(words[words.length - 2]);
        delays.put(wID, delay);
      }
    }
    return delays;
  }

}
