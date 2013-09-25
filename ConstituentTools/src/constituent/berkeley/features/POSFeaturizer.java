package constituent.berkeley.features;

public interface POSFeaturizer{
	/*
	 * [Number of feats][2 (feat-value pair)]
	 */
	public String[][] getFeats(String preTerm);
	public String getPos(String preTerm);
}
