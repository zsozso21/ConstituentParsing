package constituent.berkeley.PCFLGA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import constituent.berkeley.features.HungarianPOSFeaturizer;
import constituent.berkeley.features.POSFeaturizer;
import constituent.berkeley.tools.Parameters;

import edu.berkeley.nlp.util.MutableInteger;
import edu.berkeley.nlp.util.Numberer;

public class FeatureNumberer extends Numberer {

	private static final long serialVersionUID = 1L;

	/*
	 * <PosId, <FeatName, FeatId>>
	 */
	private Map<Integer, Map<String, Integer>> featNameToId = new HashMap<Integer, Map<String, Integer>>();

	/*
	 * <PosId, <FeatId, FeatName>>
	 */
	private Map<Integer, Map<Integer, String>> featIdToName = new HashMap<Integer, Map<Integer, String>>();

	/*
	 * <PosId, <FeatId, <ValueName, ValueId>>>
	 */
	private Map<Integer, Map<Integer, Map<String, Integer>>> featValueNameToId = new HashMap<Integer, Map<Integer, Map<String, Integer>>>();

	/*
	 * <PosId, <FeatId, <ValueId, ValueName>>>
	 */
	private Map<Integer, Map<Integer, Map<Integer, String>>> featValueIdToName = new HashMap<Integer, Map<Integer, Map<Integer, String>>>();

	private POSFeaturizer featurizer;

	public FeatureNumberer(POSFeaturizer featurizer) {
		super();
		this.featurizer = featurizer;
	}

	/*
	 * Overrided methodes.
	 */
	public Map<Integer, Integer> numberFeats(String preTerm) {
		int posId = number(featurizer.getPos(preTerm));
		String[][] featPairs = featurizer.getFeats(preTerm);
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();

		if (!featNameToId.containsKey(posId)) {
			featNameToId.put(posId, new HashMap<String, Integer>());
			featIdToName.put(posId, new HashMap<Integer, String>());
			featValueNameToId.put(posId,
					new HashMap<Integer, Map<String, Integer>>());
			featValueIdToName.put(posId,
					new HashMap<Integer, Map<Integer, String>>());
		}

		Map<String, Integer> nameToId = featNameToId.get(posId);
		Map<Integer, String> idToName = featIdToName.get(posId);
		Map<Integer, Map<String, Integer>> valueNameToIdByFeatId = featValueNameToId
				.get(posId);
		Map<Integer, Map<Integer, String>> valueIdToNameByFeatId = featValueIdToName
				.get(posId);

		String feat;
		String value;
		int featId;
		int valueId;
		for (String[] featPair : featPairs) {
			feat = featPair[0];
			value = featPair[1];
			if (!nameToId.containsKey(feat)) {
				featId = nameToId.size();
				nameToId.put(feat, featId);
				idToName.put(featId, feat);
				valueNameToIdByFeatId.put(featId,
						new HashMap<String, Integer>());
				valueIdToNameByFeatId.put(featId,
						new HashMap<Integer, String>());
			}

			featId = nameToId.get(feat);
			Map<String, Integer> valueNameToId = valueNameToIdByFeatId
					.get(featId);
			Map<Integer, String> valueIdToName = valueIdToNameByFeatId
					.get(featId);
			if (!valueNameToId.containsKey(value)) {
				valueId = valueNameToId.size();
				valueNameToId.put(value, valueId);
				valueIdToName.put(valueId, value);
			}
			valueId = valueNameToId.get(value);

			result.put(featId, valueId);
		}

		return result;
	}

	public static Numberer getGlobalNumberer(String type) {
		return getGlobalNumberer(type, new HungarianPOSFeaturizer());
	}

	public static Numberer getGlobalNumberer(String type,
			POSFeaturizer featurizer) {
		Map numbererMap = Numberer.getNumberers();
		Numberer n = (Numberer) numbererMap.get(type);
		if (n == null) {
			n = new FeatureNumberer(featurizer);
			numbererMap.put(type, n);
		}
		return n;
	}

	/*
	 * Getters and setters.
	 */
	public Map<Integer, Map<String, Integer>> getFeatNameToId() {
		return featNameToId;
	}

	public void setFeatNameToId(Map<Integer, Map<String, Integer>> featNameToId) {
		this.featNameToId = featNameToId;
	}

	public Map<Integer, Map<Integer, String>> getFeatIdToName() {
		return featIdToName;
	}

	public void setFeatIdToName(Map<Integer, Map<Integer, String>> featIdToName) {
		this.featIdToName = featIdToName;
	}

	public Map<Integer, Map<Integer, Map<String, Integer>>> getFeatValueNameToId() {
		return featValueNameToId;
	}

	public void setFeatValueNameToId(
			Map<Integer, Map<Integer, Map<String, Integer>>> featValueNameToId) {
		this.featValueNameToId = featValueNameToId;
	}

	public Map<Integer, Map<Integer, Map<Integer, String>>> getFeatValueIdToName() {
		return featValueIdToName;
	}

	public void setFeatValueIdToName(
			Map<Integer, Map<Integer, Map<Integer, String>>> featValueIdToName) {
		this.featValueIdToName = featValueIdToName;
	}

	public POSFeaturizer getFeaturizer() {
		return featurizer;
	}

	public void setFeaturizer(POSFeaturizer featurizer) {
		this.featurizer = featurizer;
	}

	public void removeFeatValue(int posId, int featId, int valueId) {
		String valueName = featValueIdToName.get(posId).get(featId)
				.get(valueId);
		featValueIdToName.get(posId).get(featId).remove(valueId);
		featValueNameToId.get(posId).get(featId).remove(valueName);
	}

	public void changeFeatValueId(int posId, int featId, int originalValueId,
			int newValueId) {
		if (featValueIdToName.get(posId).get(featId).containsKey(newValueId)) {
			System.err.println("featValueIdToName contains new valueId:"
					+ newValueId);
		}
		String valueName = featValueIdToName.get(posId).get(featId)
				.get(originalValueId);
		featValueIdToName.get(posId).get(featId).remove(originalValueId);
		featValueIdToName.get(posId).get(featId).put(newValueId, valueName);
		featValueNameToId.get(posId).get(featId).put(valueName, newValueId);
	}

	public void changeFeatValueName(int posId, int featId,
			String originalValueName, String newValueName) {
		int valueId = featValueNameToId.get(posId).get(featId)
				.get(originalValueName);
		featValueNameToId.get(posId).get(featId).remove(originalValueName);
		featValueNameToId.get(posId).get(featId).put(newValueName, valueId);
		featValueIdToName.get(posId).get(featId).put(valueId, newValueName);
	}

	public void mergeFeatValues(int posId, int featId, int valueId1,
			int valueId2) {
		/*
		 * if (valueId1 > valueId2) { int temp = valueId1; valueId1 = valueId2;
		 * valueId2 = temp; }
		 */
		String valueName1 = featValueIdToName.get(posId).get(featId)
				.get(valueId1);
		String valueName2 = featValueIdToName.get(posId).get(featId)
				.get(valueId2);
		removeFeatValue(posId, featId, valueId2);
		changeFeatValueName(posId, featId, valueName1, valueName1
				+ Parameters.MergeSeparator + valueName2);
	}

	public void reorderFeatValues(int posId, int featId) {
		if (featValueIdToName.get(posId).size() > featId) {
			List<Integer> valueIds = new ArrayList<Integer>(featValueIdToName
					.get(posId).get(featId).keySet());
			Collections.sort(valueIds);
			Map<Integer, String> valueIdToName = new HashMap<Integer, String>();
			Map<String, Integer> valueNameToId = new HashMap<String, Integer>();
			for (int i = 0; i < valueIds.size(); i++) {
				String valueName = featValueIdToName.get(posId).get(featId)
						.get(valueIds.get(i));
				valueIdToName.put(i, valueName);
				valueNameToId.put(valueName, i);
			}
			featValueIdToName.get(posId).put(featId, valueIdToName);
			featValueNameToId.get(posId).put(featId, valueNameToId);
		}
	}
	public int maxNumberOfFeats(){
		int maxNumOfFeats = 0;
		for (Map<Integer, String> feats : featIdToName.values()) {
			if (feats.size() > maxNumOfFeats) {
				maxNumOfFeats = feats.size();
			}
		}
		return maxNumOfFeats;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
