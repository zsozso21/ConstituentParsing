package edu.berkeley.nlp.PCFGLA;

import java.util.Map;

import edu.berkeley.nlp.syntax.StateSet;

public class PosMergerStateSet extends StateSet {
	
	private Map<Integer, Integer> feats;
	private boolean preTerminal = false;

	public PosMergerStateSet(short s, short nSubStates, String word, short from,
			short to) {
		super(s, nSubStates, word, from, to);
		// TODO Auto-generated constructor stub
	}
	
	public PosMergerStateSet(short s, short nSubStates, String word, short from,
			short to, Map<Integer, Integer> feats) {
		super(s, nSubStates, word, from, to);
		this.feats = feats;
		this.preTerminal = feats != null;
	}

	public PosMergerStateSet(short state, short nSubStates) {
		super(state, nSubStates);
		// TODO Auto-generated constructor stub
	}

	public PosMergerStateSet(StateSet oldS, short nSubStates) {
		super(oldS, nSubStates);
		this.feats = ((PosMergerStateSet)oldS).getFeats();
		this.preTerminal = feats != null;
	}

	public Map<Integer, Integer> getFeats() {
		return feats;
	}

	public void setFeats(Map<Integer, Integer> feats) {
		this.feats = feats;
	}
	
	public boolean isCorrectFeat(int feat, int value){
		return feats.containsKey(feat) && feats.get(feat) == value;
	}
	
	public double weightForFeat(int feat, int value){
		return isCorrectFeat(feat, value) ? 1 : 0;
	}
	
	public double weightForFeat(int value){
		return !preTerminal || isCorrectFeat(0, value) ? 1 : 0.00000001;
	}

	public boolean isPreTerminal() {
		return this.preTerminal;
	}

	public void setPreTerminal(boolean preTerminal) {
		this.preTerminal = preTerminal;
	}
	
	
}
