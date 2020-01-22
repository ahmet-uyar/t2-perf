package t2;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * ready many delay files and construct a summary file
 * currently not used
 */
public final class ManyJobDelays {

  private ManyJobDelays() { }

  public static void main(String[] args) {

    String delaysDir;

    if (args.length == 1) {
      delaysDir = args[0];
    } else {
      throw new RuntimeException("You must provide delaysDir as parameter");
    }

    File[] delayFiles = new File(delaysDir).listFiles();
    List<String> outList = new LinkedList<>();
    for (File f: delayFiles) {
      if (f.toString().contains("summary")) {
        continue;
      }
      outList.add(processFile(f));
    }

    Path summaryPath = Paths.get(delaysDir + "/summary.txt");
    try {
      Files.write(summaryPath, outList, Charset.defaultCharset());
      System.out.println("Summary of delays written to: " + summaryPath);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public static String processFile(File f) {
    List<String> lines = readFileLines(f);
//    System.out.println("lines in file: " + lines.size());
    String firstLine = lines.remove(0);
    String[] words = firstLine.split("\t");
    String jobID = words[0];
    String launchDelay = words[words.length - 1];
    double ld = Integer.parseInt(launchDelay) / 1000.0;

    List<Integer> podStarts = new LinkedList<>();
    List<Integer> allJoins = new LinkedList<>();

    for (String line: lines) {
//      System.out.println("words size: " + words.length);
      words = line.split("\t");
      podStarts.add(Integer.parseInt(words[1]));
      allJoins.add(Integer.parseInt(words[3]));
    }

    double maxPodStart =
        podStarts.stream().mapToInt(v -> v).max().orElseThrow(NoSuchElementException::new) / 1000.0;

    double minPodStart =
        podStarts.stream().mapToInt(v -> v).min().orElseThrow(NoSuchElementException::new) / 1000.0;

    double avgAllJoins = allJoins.stream()
        .mapToInt(v -> v).average().orElseThrow(NoSuchElementException::new) / 1000;

    return String.format(
        "%s\t%.1f\t%.1f\t%.1f\t%.1f", jobID, ld, minPodStart, maxPodStart, avgAllJoins);
  }

  public static List<String> readFileLines(File file) {
    Path path = file.toPath();
    try {
      List<String> lines = Files.readAllLines(path);
      return lines;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
