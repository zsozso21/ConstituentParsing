package constituent.berkeley.PCFLGA;

import java.util.ArrayList;
import java.util.Collections;

import constituent.berkeley.tools.TreeTools;
import edu.berkeley.nlp.PCFGLA.ArrayParser;
import edu.berkeley.nlp.PCFGLA.PosMergerArrayParser;
import edu.berkeley.nlp.PCFGLA.Grammar;
import edu.berkeley.nlp.PCFGLA.Lexicon;
import edu.berkeley.nlp.PCFGLA.StateSetTreeList;
import edu.berkeley.nlp.syntax.StateSet;
import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.util.ArrayUtil;

public class GrammarMergerForPos {


	/**
	 * @param grammar
	 * @param lexicon
	 * @param mergeWeights
	 * @param trainStateSetTrees
	 * @return
	 */
	/*
	public static double[][][] computeDeltasSzte(Grammar grammar, Lexicon lexicon,
			double[][] mergeWeights, StateSetTreeList trainStateSetTrees) {
		ArrayParser parser = new PosMergerArrayParser(grammar, lexicon);
		double[][][] deltas = new double[grammar.numSubStates.length][mergeWeights[0].length][mergeWeights[0].length];
		boolean noSmoothing = false, debugOutput = false;
		for (Tree<StateSet> stateSetTree : trainStateSetTrees) {
			parser.doInsideOutsideScores(stateSetTree, noSmoothing, debugOutput); // E
																					// Step
			double ll = stateSetTree.getLabel().getIScore(0);
			ll = Math.log(ll) + (100 * stateSetTree.getLabel().getIScale());// System.out.println(stateSetTree);
			if (!Double.isInfinite(ll))
				grammar.tallyMergeScores(stateSetTree, deltas, mergeWeights);
		}
		return deltas;
	}*/
	
	/**
	 * @param grammar
	 * @param lexicon
	 * @param mergeWeights
	 * @param trainStateSetTrees
	 * @return
	 */
	
	public static double[][][] computeDeltasModEM(Grammar grammar, Lexicon lexicon,
			double[][] mergeWeights, StateSetTreeList trainStateSetTrees, int featId) {
		ArrayParser parser = new PosMergerArrayParser(grammar, lexicon);
		double[][][] deltas = new double[grammar.numSubStates.length][mergeWeights[0].length][mergeWeights[0].length];
		boolean noSmoothing = false, debugOutput = false;
		for (Tree<StateSet> stateSetTree : trainStateSetTrees) {
			parser.doInsideOutsideScores(stateSetTree, noSmoothing, debugOutput); // E
			TreeTools.setNonCurrentPosIOScores(stateSetTree, featId);
																					// Step
			double ll = stateSetTree.getLabel().getIScore(0);
			ll = Math.log(ll) + (100 * stateSetTree.getLabel().getIScale());// System.out.println(stateSetTree);
			if (!Double.isInfinite(ll))
				grammar.tallyMergeScores(stateSetTree, deltas, mergeWeights);
		}
		return deltas;
	}

	/**
	 * @param grammar
	 * @param lexicon
	 * @param trainStateSetTrees
	 * @return
	 */
	/*
	public static double[][] computeMergeWeightsSzte(Grammar grammar,
			Lexicon lexicon, StateSetTreeList trainStateSetTrees) {
		double[][] mergeWeights = new double[grammar.numSubStates.length][(int) ArrayUtil
				.max(grammar.numSubStates)];
		double trainingLikelihood = 0;
		ArrayParser parser = new PosMergerArrayParser(grammar, lexicon);
		boolean noSmoothing = false, debugOutput = false;
		int n = 0;
		for (Tree<StateSet> stateSetTree : trainStateSetTrees) {
			parser.doInsideOutsideScores(stateSetTree, noSmoothing, debugOutput); // E
																					// Step
			double ll = stateSetTree.getLabel().getIScore(0);
			ll = Math.log(ll) + (100 * stateSetTree.getLabel().getIScale());// System.out.println(stateSetTree);
			if (Double.isInfinite(ll)) {
				System.out.println("Training sentence " + n
						+ " is given -inf log likelihood!");
			} else {
				trainingLikelihood += ll; // there are for some reason some
											// sentences that are unparsable
				grammar.tallyMergeWeights(stateSetTree, mergeWeights);
			}
			n++;
		}
		System.out.println("The trainings LL before merging is "
				+ trainingLikelihood);
		// normalize the weights
		grammar.normalizeMergeWeights(mergeWeights);

		return mergeWeights;
	}*/
	
	
	
	/**
	 * @param grammar
	 * @param lexicon
	 * @param trainStateSetTrees
	 * @return
	 */
	public static double[][] computeMergeWeightsModEM(Grammar grammar,
			Lexicon lexicon, StateSetTreeList trainStateSetTrees) {
		double[][] mergeWeights = new double[grammar.numSubStates.length][(int) ArrayUtil
				.max(grammar.numSubStates)];
		double trainingLikelihood = 0;
		ArrayParser parser = new PosMergerArrayParser(grammar, lexicon);
		boolean noSmoothing = false, debugOutput = false;
		int n = 0;
		for (Tree<StateSet> stateSetTree : trainStateSetTrees) {
			parser.doInsideOutsideScores(stateSetTree, noSmoothing, debugOutput); // E
																					// Step
			//TreeTools.setNonCurrentPosIOScores(stateSetTree);
			double ll = stateSetTree.getLabel().getIScore(0);
			ll = Math.log(ll) + (100 * stateSetTree.getLabel().getIScale());// System.out.println(stateSetTree);
			if (Double.isInfinite(ll)) {
				System.out.println("Training sentence " + n
						+ " is given -inf log likelihood!");
			} else if(Double.isNaN(ll)) {
				System.out.println("Training sentence " + n
						+ " is given NaN log likelihood!");
			} else {
				trainingLikelihood += ll; // there are for some reason some
											// sentences that are unparsable
				grammar.tallyMergeWeights(stateSetTree, mergeWeights);
			}
			n++;
		}
		System.out.println("The trainings LL before merging is "
				+ trainingLikelihood);
		// normalize the weights
		grammar.normalizeMergeWeights(mergeWeights);

		return mergeWeights;
	}

	public static boolean[][][] determineMergePairs(double[][][] deltas,
			boolean separateMerge, double mergingPercentage, Grammar grammar) {
		boolean[][][] mergeThesePairs = new boolean[grammar.numSubStates.length][][];
		short[] numSubStatesArray = grammar.numSubStates;
		// set the threshold so that p percent of the splits are merged again.
		ArrayList<Double> deltaSiblings = new ArrayList<Double>();
		ArrayList<Double> deltaPairs = new ArrayList<Double>();
		ArrayList<Double> deltaLexicon = new ArrayList<Double>();
		ArrayList<Double> deltaGrammar = new ArrayList<Double>();
		int nSiblings = 0, nPairs = 0, nSiblingsGr = 0, nSiblingsLex = 0;
		int numSubtates = 0;
		for (int state = 0; state < mergeThesePairs.length; state++) {
			for (int sub1 = 0; sub1 < numSubStatesArray[state] - 1; sub1++) {
				numSubtates++;
				for (int sub2 = sub1 + 1; sub2 < numSubStatesArray[state]; sub2++) {
					if (!(sub2 != sub1 + 1 && sub1 % 2 != 0)
							&& deltas[state][sub1][sub2] != 0) {
						deltaPairs.add(deltas[state][sub1][sub2]);
						nPairs++;
					}
					
					if (deltas[state][sub1][sub2] != 0) {
						deltaSiblings.add(deltas[state][sub1][sub2]);
						nSiblings++;
					}
				}
				/*if (sub1 % 2 == 0 && deltas[state][sub1][sub1 + 1] != 0) {
					deltaSiblings.add(deltas[state][sub1][sub1 + 1]);
					if (separateMerge) {
						if (grammar.isGrammarTag(state)) {
							deltaGrammar.add(deltas[state][sub1][sub1 + 1]);
							nSiblingsGr++;
						} else {
							deltaLexicon.add(deltas[state][sub1][sub1 + 1]);
							nSiblingsLex++;
						}
					}
					nSiblings++;
				}*/
				for (int sub2 = sub1 + 1; sub2 < numSubStatesArray[state]; sub2++) {
					if (!(sub2 != sub1 + 1 && sub1 % 2 != 0)
							&& deltas[state][sub1][sub2] != 0) {
						deltaPairs.add(deltas[state][sub1][sub2]);
						nPairs++;
					}
				}
			}
		}
		numSubtates += mergeThesePairs.length; //Mivel az állapotoknál mindig n-1-ig mentünk. 
		double threshold = -1, threshold2 = -1, thresholdGr = -1, thresholdLex = -1;
		if (separateMerge) {
			System.out.println("Going to merge "
					+ (int) (mergingPercentage * 100)
					+ "% of the substates siblings.");
			System.out
					.println("Setting the merging threshold for lexicon and grammar separately.");
			Collections.sort(deltaGrammar);
			Collections.sort(deltaLexicon);
			thresholdGr = deltaGrammar
					.get((int) (nSiblingsGr * mergingPercentage));
			thresholdLex = deltaLexicon.get((int) (nSiblingsLex
					* mergingPercentage * 1.5));
			System.out.println("Setting the threshold for lexical siblings to "
					+ thresholdLex);
			System.out
					.println("Setting the threshold for grammatical siblings to "
							+ thresholdGr);
		} else {
			// String topNmerge = CommandLineUtils.getValueOrUseDefault(input,
			// "-top", "");
			// Collections.sort(deltaPairs);
			// System.out.println(deltaPairs);
			Collections.sort(deltaSiblings);
			// if (topNmerge.equals("")) {
			System.out.println("Going to merge "
					+ (int) (mergingPercentage * 100)
					+ "% of the substates siblings.");
			// System.out.println("Furthermore "+(int)(mergingPercentage2*100)+"% of the non-siblings will be merged.");
			// threshold = deltaSiblings
			//		.get((int) (nSiblings * mergingPercentage));
			threshold = deltaSiblings
							.get((int) (numSubtates * mergingPercentage / 2));
			
			// if (maxSubStates>2 && mergingPercentage2>0) threshold2 =
			// deltaPairs.get((int)(nPairs*mergingPercentage2));
			// } else {
			// int top = Integer.parseInt(topNmerge);
			// System.out.println("Keeping the top "+top+" substates.");
			// threshold = deltaSiblings.get(nPairs-top);
			// }
			System.out.println("Setting the threshold for siblings to "
					+ threshold + ".");
		}
		// if (maxSubStates>2 && mergingPercentage2>0)
		// System.out.println("Setting the threshold for other pairs to "+threshold2);
		int mergePair = 0, mergeSiblings = 0;
		for (int state = 0; state < mergeThesePairs.length; state++) {
			mergeThesePairs[state] = new boolean[numSubStatesArray[state]][numSubStatesArray[state]];
			for (int i = 0; i < numSubStatesArray[state] - 1; i++) {
				for (int j = i + 1; j < numSubStatesArray[state]; j++) {
					mergeThesePairs[state][i][j] = deltas[state][i][j] <= threshold;
					if (mergeThesePairs[state][i][j]) {
						mergeSiblings++;
					}
				}
				/*if (i % 2 == 0 && deltas[state][i][i + 1] != 0) {
					if (separateMerge) {
						if (grammar.isGrammarTag(state))
							mergeThesePairs[state][i][i + 1] = deltas[state][i][i + 1] <= thresholdGr;
						else
							mergeThesePairs[state][i][i + 1] = deltas[state][i][i + 1] <= thresholdLex;
					} else
						mergeThesePairs[state][i][i + 1] = deltas[state][i][i + 1] <= threshold;
					if (mergeThesePairs[state][i][i + 1]) {
						mergeSiblings++;
					}
				}*/
				// if (mergingPercentage2>0) {
				// for (int j=i+1; j<numSubStatesArray[state]; j++) {
				// if (!(j!=i+1 && i%2!=0) && deltas[state][i][j]!=0 &&
				// deltas[state][i][j] <= threshold2){
				// mergeThesePairs[state][i][j] = true;
				// mergePair++;
				// System.out.println("Merging pair ("+i+","+j+") of state "+tagNumberer.object(state));
				// }
				// }
				// }
			}
		}
		System.out.println("Merging " + mergeSiblings + " siblings and "
				+ mergePair + " other pairs.");
		for (short state = 0; state < deltas.length; state++) {
			System.out.print("State " + grammar.getTagNumberer().object(state));
			for (int i = 0; i < numSubStatesArray[state]; i++) {
				for (int j = i + 1; j < numSubStatesArray[state]; j++) {
					if (mergeThesePairs[state][i][j])
						System.out.print(". Merging pair (" + i + "," + j
								+ ") at cost " + deltas[state][i][j]);
				}
			}
			System.out.print(".\n");
		}
		return mergeThesePairs;
	}
	
	
}
