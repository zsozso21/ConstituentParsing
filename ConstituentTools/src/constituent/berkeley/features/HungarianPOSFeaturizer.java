package constituent.berkeley.features;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import constituent.berkeley.tools.Parameters;

public class HungarianPOSFeaturizer implements POSFeaturizer, Serializable {
	private static final long serialVersionUID = 9101696667207962648L;

	/**
	 * @param preTerm Input POS tag in POS##feat1_value1|feat2_value2...## format
	 * @return Array of feat-value pairs [[feat1,value1],[feat2,value2]...]
	 */
	public String[][] getFeats(String preTerm) {
		List<String[]> result = new ArrayList<String[]>();
		if (preTerm.equalsIgnoreCase(Parameters.Punction)) {
			return new String[0][];
		}
		
		int sepInd = preTerm.indexOf("#");
		String featsStr = preTerm.substring(sepInd >= 0 ? sepInd + 2 : 3, preTerm.length() - 2);
		String[] featsArr = featsStr.split(Parameters.FeatSeparator);
		for (String feat : featsArr) {
			String[] featPair = feat.split(Parameters.FeatValueSeparator);
			if (featPair.length < 2 || featPair[0].equalsIgnoreCase("lemma")) {
				continue;
			}
			result.add(featPair);
		}

		String[][] resultArr = new String[result.size()][];
		return result.toArray(resultArr);
	}
	
	public String getPos(String preTerm) {
		if (preTerm.equalsIgnoreCase(Parameters.Punction)) {
			return preTerm;
		}
		int sepInd = preTerm.indexOf("#");
		return preTerm.substring(0, sepInd > 0 ? sepInd : 1);
	}
}
