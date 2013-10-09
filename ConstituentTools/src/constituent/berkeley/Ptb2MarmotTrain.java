package constituent.berkeley;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import constituent.berkeley.tools.ConstTools;
import edu.berkeley.nlp.syntax.Tree;

public class Ptb2MarmotTrain {
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      System.out
          .println("Usage: java constituent.berkeley.Ptb2MarmotTrain ptbFile1 marmotTrainFile");
      return;
    }
    
    try {
      List<Tree<String>> sentences = ConstTools.readConstTree(args[0]);
      BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]));
      
      for (Tree<String> sentence : sentences) {
        List<Tree<String>> preTerms = sentence.getPreTerminals();
        
        for (Tree<String> preTerm : preTerms) {
          writer.write(gerMarmotTrainLine(preTerm));
          writer.write("\n");
        }
        writer.write("\n");
      }
      
      writer.flush();
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public static String gerMarmotTrainLine(Tree<String> preTerm) {
    String tag = preTerm.getLabel();
    if (tag.contains("#")) {
      tag = tag.replaceFirst("([^#]*)##([^#]*).*", "$1\t$2");
    } else {
      tag = tag + "\t_";
    }
    
    return preTerm.getChild(0).getLabel()+ "\t" + tag;
  }
}
