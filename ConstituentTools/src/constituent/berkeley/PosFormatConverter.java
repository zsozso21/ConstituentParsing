package constituent.berkeley;

import java.io.IOException;
import java.util.List;

import constituent.berkeley.tools.ConstTools;
import edu.berkeley.nlp.syntax.Tree;

public class PosFormatConverter {
  public static void main(String[] args) {
    if (args.length < 2) {
      System.out
          .println("Usage: java constituent.berkeley.PosFormatConverter originalPtbFile convertedPtbFile");
      return;
    }
    
    try {
      List<Tree<String>> sentences = ConstTools.readConstTree(args[0]);
      
      for (Tree<String> sentence : sentences) {
        for (Tree<String> preTerm : sentence.getPreTerminals()) {
          preTerm.setLabel(preTerm.getLabel().replaceAll("-", "*").replaceAll("_", "~").replaceAll("=", "_"));
        }
      }
      
      ConstTools.writeConstTreeSentence(sentences, args[1]);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
