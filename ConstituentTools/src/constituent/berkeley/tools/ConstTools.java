package constituent.berkeley.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.input.BOMInputStream;

import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.syntax.Trees;

public class ConstTools {
  public static List<String> readConstSentenceString(String fileName) throws IOException {
    return IOTools.readStringList(fileName);
  }
  
  public static void writeConstSentenceString(List<String> sentences, String fileName) throws IOException {
    IOTools.writeStringList(sentences, fileName);
  }
  
  public static void writeConstTreeSentence(List<Tree<String>> sentences, String fileName) throws IOException {
    List<String> stringList = new ArrayList<String>();
    for (Tree<String> tree : sentences) {
      stringList.add(tree.toString());
    }
    IOTools.writeStringList(stringList, fileName);
  }
  
  public static List<Tree<String>> readConstTree(String fileName) throws IOException{
    BufferedReader bfReader = new BufferedReader(new InputStreamReader(new BOMInputStream(new FileInputStream(fileName))));
    
    List<Tree<String>> result  = new ArrayList<Tree<String>>();
    
    Trees.PennTreeReader reader = new Trees.PennTreeReader(bfReader);
    
    while (reader.hasNext()) {
      try {
      result.add(reader.next());
      } catch (Exception ex) {
        System.out.println("HIBA");
      }
    }
    
    bfReader.close();
    
    return result;
  }
}
