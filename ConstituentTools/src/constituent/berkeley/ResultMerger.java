package constituent.berkeley;

import ims.hypergraph.EvalB;
import ims.hypergraph.LanguagePack;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import constituent.berkeley.tools.ConstTools;
import edu.berkeley.nlp.syntax.Tree;

public class ResultMerger {

  private Tree<String> originalSentence;

  private Tree<String> predSentence1;

  private Tree<String> predSentence2;

  private int length;

  private double pred1Recall;

  private double pred1Precision;

  private double pred1F1;

  private double pred2Recall;

  private double pred2Precision;

  private double pred2F1;

  private EvalB evalB;

  public int getLength() {
    return length;
  }

  private void getDifferences() {
    replaceWordToOriginal(originalSentence, predSentence1);
    replaceWordToOriginal(originalSentence, predSentence2);

    String gs = originalSentence.toString();
    String pred1 = predSentence1.toString();
    String pred2 = predSentence2.toString();

    if (evalB.evaluateStrings(gs, pred1)) {
      pred1Recall = evalB.evalb.getLastRecall();
      pred1Precision = evalB.evalb.getLastPrecision();
      pred1F1 = 2 * pred1Recall * pred1Precision
          / (pred1Recall + pred1Precision);

    } else {
      System.err.println("ERROR");
    }

    if (evalB.evaluateStrings(gs, pred2)) {
      pred2Recall = evalB.evalb.getLastRecall();
      pred2Precision = evalB.evalb.getLastPrecision();
      pred2F1 = 2 * pred2Recall * pred2Precision
          / (pred2Recall + pred2Precision);

    } else {
      System.err.println("ERROR");
    }
  }

  public static void replaceWordToOriginal(Tree<String> originalSentence,
      Tree<String> predSentence) {
    List<Tree<String>> originalTerms = originalSentence.getTerminals();
    List<Tree<String>> predTerms = predSentence.getTerminals();

    for (int i = 0; i < originalTerms.size(); i++) {
      predTerms.get(i).setLabel(originalTerms.get(i).getLabel());
    }
  }

  public ResultMerger(Tree<String> originalSentence,
      Tree<String> predSentence1, Tree<String> predSentence2) {
    this.originalSentence = originalSentence;
    this.predSentence1 = predSentence1;
    this.predSentence2 = predSentence2;

    this.evalB = new EvalB();
    this.length = originalSentence.getTerminals().size();
    getDifferences();
  }

  public String toString() {
    if (Math.abs((pred1F1 - pred2F1)) < 0.2) {
      return "";
    }
    
    String result = "Length: " + getLength() + " |  F1 diff: "
        + String.format(Locale.US, "%6.2f", (pred1F1 - pred2F1) * 100) + " | ";
    result += "(P/R/F) pred1: "
        + String.format(Locale.US, "%6.2f", pred1Precision * 100) + " /"
        + String.format(Locale.US, "%6.2f", pred1Recall * 100) + " /"
        + String.format(Locale.US, "%6.2f", pred1F1 * 100) + " | ";
    result += "pred2: "
        + String.format(Locale.US, "%6.2f", pred2Precision * 100) + " /"
        + String.format(Locale.US, "%6.2f", pred2Recall * 100) + " /"
        + String.format(Locale.US, "%6.2f", pred2F1 * 100);
    result += "\n";
    result += "Original:\t" + originalSentence + "\n";
    result += "Predicated1:\t" + predSentence1 + "\n";
    result += "Predicated2:\t" + predSentence2 + "\n\n";
    
    
    return result;
  }

  public static void main(String[] args) {
    try {
      if (args.length < 3) {
        System.out
            .println("Usage: java constituent.berkeley.ResultMerger originalConstFile predConstFile1 predConstFile2 [outFile]");
        return;
      }

      Writer writer;
      if (args.length > 3) {
        writer = new FileWriter(args[3]);
      } else {
        writer = new PrintWriter(System.out);
      }
      BufferedWriter bfWriter = new BufferedWriter(writer);

      List<Tree<String>> originalSentences = ConstTools.readConstTree(args[0]);
      List<Tree<String>> predSentences1 = ConstTools.readConstTree(args[1]);
      List<Tree<String>> predSentences2 = ConstTools.readConstTree(args[2]);

      LanguagePack.setLanguage("hu");

      List<ResultMerger> resultMergerList = new ArrayList<ResultMerger>();
      for (int i = 0; i < originalSentences.size(); i++) {
        resultMergerList.add(new ResultMerger(originalSentences.get(i),
            predSentences1.get(i), predSentences2.get(i)));
      }

      Collections.sort(resultMergerList, new Comparator<ResultMerger>() {
        @Override
        public int compare(ResultMerger rm1, ResultMerger rm2) {
          return rm1.getLength() > rm2.getLength() ? 1 : (rm1.getLength() < rm2
              .getLength() ? -1 : 0);
        }
      });

      for (ResultMerger rm : resultMergerList) {
        bfWriter.write(rm.toString());
      }
      
      bfWriter.flush();
      bfWriter.close();

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
