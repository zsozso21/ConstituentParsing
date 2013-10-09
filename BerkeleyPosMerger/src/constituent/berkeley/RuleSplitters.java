package constituent.berkeley;

import java.util.Random;

import edu.berkeley.nlp.PCFGLA.BinaryRule;
import edu.berkeley.nlp.PCFGLA.UnaryRule;

public class RuleSplitters {

	public static BinaryRule splitBinaryRuleByPos(BinaryRule rule,
			short[] numSubStates, short[] newNumSubStates, Random random,
			double randomness, boolean doNotNormalize, int mode,
			boolean leftChildIsPOS, boolean rightChildIsPOS) {
		if (!leftChildIsPOS && !rightChildIsPOS) {
			return rule;
		}
		int numLeftChildSubs = numSubStates[rule.leftChildState];
		int numRightChildSubs = numSubStates[rule.rightChildState];
		int numParentSubs = numSubStates[rule.parentState];

		int newNumLeftChildSubs = newNumSubStates[rule.leftChildState];
		int newNumRightChildSubs = newNumSubStates[rule.rightChildState];
		int newNumParentSubs = newNumSubStates[rule.parentState];

		double[][][] oldScores = rule.getScores2();
		double[][][] newScores = new double[newNumLeftChildSubs][newNumRightChildSubs][newNumParentSubs];

		double sum = 0;
		double finalScore = 0;
		if (leftChildIsPOS && rightChildIsPOS) {
			for (short pS = 0; pS < numParentSubs; pS++) {
				for (short lcS = 0; lcS < numLeftChildSubs; lcS++) {
					for (short rcS = 0; rcS < numRightChildSubs; rcS++) {
						if (oldScores[lcS][rcS] == null)
							continue;
						double score = oldScores[lcS][rcS][pS];
						sum += score;
					}
				}
				finalScore = sum / (newNumLeftChildSubs * newNumRightChildSubs);
				sum = 0.0;
				// The number off parent's subset is same in the 2 rule
				for (short lcS = 0; lcS < newNumLeftChildSubs; lcS++) {
					for (short rcS = 0; rcS < newNumRightChildSubs; rcS++) {
						newScores[lcS][rcS][pS] = finalScore;
					}
				}
			}
		} else if (!leftChildIsPOS && rightChildIsPOS) {
			for (short pS = 0; pS < numParentSubs; pS++) {
				for (short lcS = 0; lcS < numLeftChildSubs; lcS++) {
					for (short rcS = 0; rcS < numRightChildSubs; rcS++) {
						if (oldScores[lcS][rcS] == null)
							continue;
						double score = oldScores[lcS][rcS][pS];
						sum += score;
					}
					finalScore = sum / newNumRightChildSubs;
					sum = 0.0;
					for (short rcS = 0; rcS < newNumRightChildSubs; rcS++) {
						newScores[lcS][rcS][pS] = finalScore;
					}
				}
			}
		} else { // leftChildIsPOS && !rightChildIsPOS
			for (short pS = 0; pS < numParentSubs; pS++) {
				for (short rcS = 0; rcS < numRightChildSubs; rcS++) {
					for (short lcS = 0; lcS < numLeftChildSubs; lcS++) {
						if (oldScores[lcS][rcS] == null)
							continue;
						double score = oldScores[lcS][rcS][pS];
						sum += score;
					}
					finalScore = sum / newNumLeftChildSubs;
					sum = 0.0;
					for (short lcS = 0; lcS < newNumLeftChildSubs; lcS++) {
						newScores[lcS][rcS][pS] = finalScore;
					}
				}
			}
		}

		BinaryRule newRule = new BinaryRule(rule, newScores);
		return newRule;
	}

	public static UnaryRule splitUniaryRuleByPos(UnaryRule rule, short[] numSubStates,
			short[] newNumSubStates, Random random, double randomness,
			boolean doNotNormalize, int mode, boolean childIsPOS) {
		if (!childIsPOS) {
			return rule;
		}
		int numchildSubs = numSubStates[rule.childState];
		int numParentSubs = numSubStates[rule.parentState];

		int newNumchildSubs = newNumSubStates[rule.childState];
		int newNumParentSubs = newNumSubStates[rule.parentState];

		double[][] oldScores = rule.getScores2();
		double[][] newScores = new double[newNumchildSubs][newNumParentSubs];

		double sum = 0;
		double finalScore = 0;
		for (short pS = 0; pS < numParentSubs; pS++) {
			for (short cS = 0; cS < numchildSubs; cS++) {
				if (oldScores[cS] == null)
					continue;
				double score = oldScores[cS][pS];
				sum += score;
			}
			finalScore = sum / (newNumchildSubs);
			sum = 0.0;
			// The number off parent's subset is same in the 2 rule
			for (short cS = 0; cS < newNumchildSubs; cS++) {
				newScores[cS][pS] = finalScore;
			}
		}

		UnaryRule newRule = new UnaryRule(rule, newScores);
		return newRule;
	}

}
