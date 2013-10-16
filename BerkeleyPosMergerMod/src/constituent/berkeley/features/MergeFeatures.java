package constituent.berkeley.features;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constituent.berkeley.PCFLGA.FeatureNumberer;
import constituent.berkeley.tools.ConstTools;
import constituent.berkeley.tools.Parameters;
import edu.berkeley.nlp.PCFGLA.Grammar;
import edu.berkeley.nlp.syntax.Tree;

public class MergeFeatures {

	public static void main(String[] args) {
		Map<String, Map<String, String>> mergeMap = loadMergePairs(args[0]);
		try {
			List<Tree<String>> trainBase = ConstTools.readConstTree(args[1]);
			trainBase = convertTags(trainBase, mergeMap);
			ConstTools.writeConstTreeSentence(trainBase, args[2]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Map<String, Map<String, String>> loadMergePairs(
			String fileName) {
		Map<String, Map<String, String>> mergeMap = new HashMap<String, Map<String, String>>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] mergeArr = line.split("\t");
				if (mergeArr.length >= 3) {
					Map<String, String> mergePair;
					if (mergeMap.containsKey(mergeArr[0])) {
						mergePair = mergeMap.get(mergeArr[0]);
					} else {
						mergePair = new HashMap<String, String>();
					}
					mergePair.put(mergeArr[1], mergeArr[1]
							+ Parameters.FeatValueSeparator + mergeArr[2]);
					mergePair.put(mergeArr[2], mergeArr[1]
							+ Parameters.FeatValueSeparator + mergeArr[2]);
					mergeMap.put(mergeArr[0], mergePair);
				}
			}

			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return mergeMap;
	}

	public static List<Tree<String>> convertTags(List<Tree<String>> trainBase,
			Map<String, Map<String, String>> mergeMap) {
		return convertTags(trainBase, mergeMap, 0, true);
	}

	public static List<Tree<String>> convertTags(List<Tree<String>> trainBase,
			Map<String, Map<String, String>> mergeMap, int featId,
			boolean newMainPoses) {
		HungarianPOSFeaturizer featurizer = new HungarianPOSFeaturizer();
		for (Tree<String> tree : trainBase) {
			for (Tree<String> preTerm : tree.getPreTerminals()) {
				String label = preTerm.getLabel();
				String pos = featurizer.getPos(label);
				String[][] feats = featurizer.getFeats(label);

				if (feats.length > featId) {
					String newFeat;
					if (mergeMap.containsKey(pos)
							&& mergeMap.get(pos).containsKey(feats[featId][1])) {
						newFeat = mergeMap.get(pos).get(feats[featId][1]);
					} else {
						newFeat = feats[featId][1];
					}
					String featStr = "";
					if (newMainPoses) {
						pos += Parameters.FeatSeparatorBase + feats[featId][0]
								+ Parameters.FeatValueSeparator + newFeat;
					} else {
						featStr += feats[featId][0]
								+ Parameters.FeatValueSeparator + newFeat
								+ Parameters.FeatSeparatorBase;
					}
					if (feats.length >= 2) {
						featStr += feats[1][0] + Parameters.FeatValueSeparator
								+ feats[1][1];
						for (int i = 2; i < feats.length; i++) {
							featStr += Parameters.FeatSeparatorBase
									+ feats[i][0]
									+ Parameters.FeatValueSeparator
									+ feats[i][1];
						}
					}
					preTerm.setLabel(pos + "##" + featStr + "##");
				}
			}
		}

		return trainBase;
	}

	public static List<Tree<String>> convertTags(List<Tree<String>> trainBase,
			Map<String, Map<String, String>> mergeMap, int featId,
			FeatureNumberer numberer) {
		HungarianPOSFeaturizer featurizer = new HungarianPOSFeaturizer();
		for (Tree<String> tree : trainBase) {
			for (Tree<String> preTerm : tree.getPreTerminals()) {
				String label = preTerm.getLabel();
				String pos = featurizer.getPos(label);
				String[][] feats = featurizer.getFeats(label);
				int posId = numberer.number(pos);
				if (numberer.getFeatIdToName().get(posId) == null) {
					System.out.println(pos);
					continue;
				}
				String currentFeat = numberer.getFeatIdToName().get(posId)
						.get(featId);
				int featArrId = -1;
				for (int i = 0; i < feats.length; i++) {
					if (feats[i][0].equals(currentFeat)) {
						featArrId = i;
					}
				}

				if (featArrId >= 0) {
					if (mergeMap.containsKey(pos)
							&& mergeMap.get(pos).containsKey(
									feats[featArrId][1])) {
						feats[featArrId][1] = mergeMap.get(pos).get(
								feats[featArrId][1]);
					}
					String featStr = "";

					featStr += feats[0][0] + Parameters.FeatValueSeparator
							+ feats[0][1];

					for (int i = 1; i < feats.length; i++) {
						featStr += Parameters.FeatSeparatorBase + feats[i][0]
								+ Parameters.FeatValueSeparator + feats[i][1];
					}
					preTerm.setLabel(pos + "##" + featStr + "##");
				}
			}
		}

		return trainBase;
	}
}
