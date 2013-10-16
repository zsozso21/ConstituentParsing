package constituent.berkeley.tools;

import edu.berkeley.nlp.PCFGLA.PosMergerStateSet;
import edu.berkeley.nlp.syntax.StateSet;
import edu.berkeley.nlp.syntax.Tree;

public class TreeTools {
	
	public static Tree<StateSet> setNonCurrentPosIOScores(Tree<StateSet> tree){
		return setNonCurrentPosIOScores(tree, 0);
	}
	
	public static Tree<StateSet> setNonCurrentPosIOScores(Tree<StateSet> tree, int feat){
		for (Tree<StateSet> preTerm : tree.getPreTerminals()) {
			PosMergerStateSet label = (PosMergerStateSet)preTerm.getLabel();
			if (label.getFeats().containsKey(feat)) {
				int currentValue = label.getFeats().get(feat);
				for (int i = 0; i < label.numSubStates(); i++) {
					if (i != currentValue) {
						label.setIScore(i, label.getIScore(i) * 0.001);
						label.setOScore(i, label.getOScore(i) * 0.001);
					}
				}
			}
		}
		
		return tree;
	}
}
