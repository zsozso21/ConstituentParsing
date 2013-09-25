package constituent.berkeley;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constituent.berkeley.tools.CollectionTool;
import constituent.berkeley.tools.ConstTools;
import edu.berkeley.nlp.syntax.Tree;

public class PosCounter {

  public static void main(String[] args) {
    try {
      Map<String, Integer> numberOfTags = new HashMap<String, Integer>();
      if (args.length < 2) {
        System.out
            .println("Usage: java constituent.berkeley.PosCounter constFile1 [constFile2 [constFile3 ...]] outFile");
        return;
      }
      for (int i = 0; i < args.length - 1; i++) {
        List<Tree<String>> sentences = ConstTools.readConstTree(args[i]);
        for (Tree<String> tree : sentences) {
          for (String preTerm : tree.getPreTerminalYield()) {
            CollectionTool.incValueInMap(numberOfTags, preTerm);
          }
        }
      }
      CollectionTool.WriteMap(numberOfTags, new FileOutputStream(args[args.length - 1]));
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
