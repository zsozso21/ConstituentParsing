package constituent.berkeley;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LexiconPosMerger {

  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
      if (args.length < 2) {
        System.out
            .println("Usage: java constituent.berkeley.LexiconPosMerger originalConstFile1 mergedConstFile1 [originalConstFile2 mergedConstFile2 [originalConstFile3 mergedConstFile3 ...]] originalLex outLex posColumnNumber");
        System.out.println("posColumnNumber: The index of columns starts with 1.");
        return;
      }

      int columnNumber = Integer.parseInt(args[args.length -1]) - 1;
      Feat2MergedFeatKnowedTags posMerger = new Feat2MergedFeatKnowedTags();
      for (int i = 0; i < args.length - 3; i += 2) {
        posMerger.addRulesByFile(args[i], args[i + 1]);
      }

      BufferedReader reader = new BufferedReader(new FileReader(
          args[args.length - 3]));
      BufferedWriter writer = new BufferedWriter(new FileWriter(
          args[args.length - 2]));

      String line;
      while ((line = reader.readLine()) != null) {
        String[] lineArr = line.split("\\t");
        lineArr[columnNumber] = posMerger.getMergedPos(lineArr[columnNumber]);
        writer.write(lineArr[0]);
        for (int i = 1 ; i < lineArr.length; i++) {
          writer.write("\t" + lineArr[i]);
        }
        writer.write("\n");
      }

      reader.close();
      writer.flush();
      writer.close();

    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
