package constituent.berkeley;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import constituent.berkeley.tools.ConstTools;
import edu.berkeley.nlp.syntax.Tree;

public class Feat2MergedFeatKnowedTags {

  private Map<String, String> posMergedPosPairs = new HashMap<String, String>();
  
  public void addRulesByFile(String originalFileName, String mergedFileName) {
    try {
      List<Tree<String>> originalSentences = ConstTools.readConstTree(originalFileName);
      List<Tree<String>> mergedSentences = ConstTools.readConstTree(mergedFileName);
      
      for (int i = 0; i < originalSentences.size(); i++) {
        List<String> originaltPreTerms = originalSentences.get(i).getPreTerminalYield();
        List<String> mergedPreTerms = mergedSentences.get(i).getPreTerminalYield();
        for (int j = 0; j < originaltPreTerms.size(); j++) {
          posMergedPosPairs.put(originaltPreTerms.get(j), mergedPreTerms.get(j));
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public String getMergedPos(String originalPos) {
    if (posMergedPosPairs != null && posMergedPosPairs.containsKey(originalPos)) {
      return posMergedPosPairs.get(originalPos);
    }
    return null;
  }
}
