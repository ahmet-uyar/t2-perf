package plainpods;

import t2.Delays;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GeneratePodYamls {
  public static void main(String[] args) {

    if (args.length != 4) {
      System.out.println("Usage java GeneratePodYamls sourceFile count name(podName and fileName) targetDir");
      return;
    }

    String source = args[0];
    int count = Integer.parseInt(args[1]);
    String name = args[2];
    String targetDir = args[3];

    Path sourceFile = Paths.get(source);

    List<String> lines = Delays.readFileLines(source);

    // get name line and its index
    String nameLine = null;
    int nameLineIndex = -1;
    int i = 0;
    for (String line: lines) {
      if (line.contains("  name:")) {
        nameLine = line.substring(0, line.indexOf("name:") + 6);
        nameLineIndex = i;
        break;
      }
      i++;
    }

    for (int j = 0; j < count; j++) {
      String pName = name + "-" + (1000 + j);
      String newNameLine = nameLine + pName;
      System.out.println("newNameLine: " + newNameLine);
      lines.set(nameLineIndex, newNameLine);
      Path newFile = Paths.get(targetDir + "/" + pName + ".yaml");
      try {
        Files.write(newFile, lines, Charset.defaultCharset());
        System.out.println("Written yaml file: " + newFile);
      } catch (IOException e) {
        e.printStackTrace();
      }

    }

  }

}
