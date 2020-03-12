package standalone;

import t2.Delays;

import java.util.*;

public class MPIDelays {
  public static void main(String[] args) {

    String jobLogsFile;
    String jobID;

    if (args.length == 2) {
      jobLogsFile = args[0];
      jobID = args[0];
    } else {
      System.out.println("Please provide jobID and jobLogsFile as parameters");
      return;
    }

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

    // print delays,
    // first print header line: jobID   numberOfWorkers
    System.out.println(jobID + "\t" + mpiWorkerDelays.size());
    for (Map.Entry<Integer, Long> entry: mpiWorkerDelays.entrySet()) {
      System.out.println(entry.getKey() + "\t"
          + entry.getValue() + "\t"
          + workerStartDelays.get(entry.getKey()) + "\t"
          + allJoinedDelays.get(entry.getKey())
      );
    }

    long maxMpiWorkerStart = mpiWorkerDelays.values().stream().mapToLong(d -> d).max().orElseThrow(NoSuchElementException::new);
    long maxWorkerStart = workerStartDelays.values().stream().mapToLong(d -> d).max().orElseThrow(NoSuchElementException::new);
    double avgAllJoined = allJoinedDelays.values().stream().mapToLong(d -> d).average().orElseThrow(NoSuchElementException::new);
    String line = String.format(
        "summary\t%d\t%d\t%.1f", maxMpiWorkerStart, maxWorkerStart, avgAllJoined);
    System.out.println(line);
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
