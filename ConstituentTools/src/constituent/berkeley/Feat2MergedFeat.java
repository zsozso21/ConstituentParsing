package constituent.berkeley;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import constituent.berkeley.features.HungarianPOSFeaturizer;
import constituent.berkeley.features.POSFeaturizer;
import constituent.berkeley.tools.Parameters;

public class Feat2MergedFeat {

  /*
   * <MainPos, <FeatId, <FeatValue, MergedFeatValue>>>
   * example: <"M", <"Num", <"s", "s/p">>> 
   */
  private Map<String, Map<String, Map<String, String>>> mergedFeatRules;

  private POSFeaturizer posFeaturizer;

  public Feat2MergedFeat(POSFeaturizer posFeaturizer, String fileName) {
    this(posFeaturizer, getMergedFeatList(fileName));
  }
  
  public Feat2MergedFeat(POSFeaturizer posFeaturizer, List<String> mergedTags) {
    this.posFeaturizer = posFeaturizer;
    getMergedFeatRules(mergedTags);
  }

  public Feat2MergedFeat(String fileName) {
    this(new HungarianPOSFeaturizer(), fileName);
  }

  public static List<String> getMergedFeatList(String fileName) {
    List<String> mergedFeats = new ArrayList<String>();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(fileName));

      String line;
      while ((line = reader.readLine()) != null) {
        String[] lineArr = line.split("\t");
        mergedFeats.add(lineArr[0]);
      }
      reader.close();
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return mergedFeats;
  }

  private void getMergedFeatRules(List<String> mergedTags) {
    for (String mergedTag : mergedTags) {
      String pos = posFeaturizer.getPos(mergedTag);
      String[][] feats = posFeaturizer.getFeats(mergedTag);
      if (!mergedFeatRules.containsKey(pos)) {
        mergedFeatRules.put(pos, new HashMap<String, Map<String, String>>());
      }
      Map<String, Map<String, String>> posFeatRules = mergedFeatRules.get(pos);
      for(String[] featPair : feats) {
        String featId = featPair[0];
        String[] featValues = featPair[1].split(Parameters.MergeSeparator);
        if (!posFeatRules.containsKey(featId)) {
          posFeatRules.put(featId, new HashMap<String, String>());
        }
        Map<String, String> featRules =  posFeatRules.get(featId);
        for (String featValue : featValues) {
          featRules.put(featValue, featPair[1]);
        }
      }
    }
  }
  /*
   * TODO: Cache megírása
   */
  public String getMergedTag(String originalTag) {
    String mergedPos = "";
    String originalPos = posFeaturizer.getPos(originalTag);
    String[][] originalFeats = posFeaturizer.getFeats(originalTag);
    if (!mergedFeatRules.containsKey(originalPos) || originalPos.equals(Parameters.Punction)) {
      return originalTag;
    }
    mergedPos += originalPos + "##";
    Map<String, Map<String, String>> posFeatRules = mergedFeatRules.get(originalPos);
    if (originalFeats.length > 0) {
      mergedPos += originalFeats[0][0] + Parameters.FeatValueSeparator + getMergedFeatPair(posFeatRules, originalFeats[0][0], originalFeats[0][1]);
    }
    for (int i = 1; i < originalFeats.length; i++) {
      mergedPos += Parameters.FeatSeparatorBase + originalFeats[i][0] + Parameters.FeatValueSeparator + getMergedFeatPair(posFeatRules, originalFeats[i][0], originalFeats[i][1]);
    }
    mergedPos += "##";
    return mergedPos;
  }
  
  private String getMergedFeatPair(Map<String, Map<String, String>> posFeatRules, String featId, String featValue) {
    if (posFeatRules.containsKey(featId) && posFeatRules.get(featId).containsKey(featValue)) {
      return posFeatRules.get(featId).get(featValue);
    }
    return featValue;
  }
}
