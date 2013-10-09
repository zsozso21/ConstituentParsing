package constituent.berkeley;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import constituent.berkeley.tools.ConstTools;
import edu.berkeley.nlp.syntax.Tree;

public class Ptb2Raw {

  public static void main(String[] args) {
    if (args.length < 2) {
      System.out
          .println("Usage: java constituent.berkeley.Ptb2Raw ptbFile1 rawFile");
      return;
    }
    
    try {
      List<Tree<String>> sentences = ConstTools.readConstTree(args[0]);
      BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]));
      
      for (Tree<String> sentence : sentences) {
        List<String> words = sentence.getTerminalYield();
        if (words.size() > 0) {
          writer.write(words.get(0));
        }
        for (int i = 1; i < words.size(); i++) {
          writer.write(" " + words.get(i));
        }
        writer.write("\n");
      }
      
      writer.flush();
      writer.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
   
  }
}
