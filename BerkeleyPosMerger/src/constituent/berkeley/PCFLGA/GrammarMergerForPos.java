package constituent.berkeley.PCFLGA;

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
		ArrayParser parser = new ArrayParser(grammar, lexicon);
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
		ArrayParser parser = new ArrayParser(grammar, lexicon);
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

}
