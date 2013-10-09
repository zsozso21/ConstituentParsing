package constituent.berkeley;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import constituent.berkeley.PCFLGA.FeatureNumberer;
import constituent.berkeley.PCFLGA.GrammarMergerForPos;
import constituent.berkeley.features.MergeFeatures;
import constituent.berkeley.tools.ConstTools;
import constituent.berkeley.tools.Parameters;

import edu.berkeley.nlp.PCFGLA.Binarization;
import edu.berkeley.nlp.PCFGLA.Corpus;
import edu.berkeley.nlp.PCFGLA.Grammar;
import edu.berkeley.nlp.PCFGLA.GrammarMerger;
import edu.berkeley.nlp.PCFGLA.GrammarTrainerForPosMerger;
import edu.berkeley.nlp.PCFGLA.Lexicon;
import edu.berkeley.nlp.PCFGLA.ParserData;
import edu.berkeley.nlp.PCFGLA.SophisticatedLexicon;
import edu.berkeley.nlp.PCFGLA.PosMergerSophisticatedLexicon;
import edu.berkeley.nlp.PCFGLA.StateSetTreeList;
import edu.berkeley.nlp.PCFGLA.PosMergerStateSetTreeList;
import edu.berkeley.nlp.PCFGLA.smoothing.NoSmoothing;
import edu.berkeley.nlp.PCFGLA.smoothing.Smoother;
import edu.berkeley.nlp.syntax.StateSet;
import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.util.Numberer;

public class POSMerger {
	private Grammar grammar;
	private Lexicon lexicon;
	private StateSetTreeList trainStateSetTrees;
	private StateSetTreeList validationStateSetTrees;
	private double randomness;
	private boolean allowMoreSubstatesThanCounts;
	private double[] smoothParams;
	private double filter;
	private int nTrees;
	private Binarization binarization;
	private String outFileName;
	private boolean separateMergingThreshold;
	private double mergingPercentage;
	private int rare;
	private String splitPosOut;
	private List<Tree<String>> trainTrees;
	private List<Tree<String>> testTrees;
	private String trainOutPath;
	private String testOutPath;
	private int maxSentenceLength;
	private boolean manualAnnotation;

	public POSMerger(Grammar grammar, Lexicon lexicon,
			StateSetTreeList trainStateSetTrees,
			StateSetTreeList validationStateSetTrees, double randomness,
			boolean allowMoreSubstatesThanCounts, double[] smoothParams,
			double filter, int nTrees, Binarization binarization,
			String outFileName, boolean separateMergingThreshold,
			double mergingPercentage, int rare, String splitPosOut) {
		this.grammar = grammar;
		this.lexicon = lexicon;
		this.trainStateSetTrees = trainStateSetTrees;
		this.validationStateSetTrees = validationStateSetTrees;
		this.randomness = randomness;
		this.allowMoreSubstatesThanCounts = allowMoreSubstatesThanCounts;
		this.smoothParams = smoothParams;
		this.filter = filter;
		this.nTrees = nTrees;
		this.binarization = binarization;
		this.outFileName = outFileName;
		this.separateMergingThreshold = separateMergingThreshold;
		this.mergingPercentage = mergingPercentage;
		this.rare = rare;
		this.splitPosOut = splitPosOut;
	}

	public POSMerger(Grammar grammar, Lexicon lexicon,
			StateSetTreeList trainStateSetTrees,
			StateSetTreeList validationStateSetTrees, double randomness,
			boolean allowMoreSubstatesThanCounts, double[] smoothParams,
			double filter, int nTrees, Binarization binarization,
			String outFileName, boolean separateMergingThreshold,
			double mergingPercentage, int rare, String splitPosOut,
			List<Tree<String>> trainTrees, List<Tree<String>> testTrees,
			String trainOutPath, String testOutPath, int maxSentenceLength,
			boolean manualAnnotation) {
		this.grammar = grammar;
		this.lexicon = lexicon;
		this.trainStateSetTrees = trainStateSetTrees;
		this.validationStateSetTrees = validationStateSetTrees;
		this.randomness = randomness;
		this.allowMoreSubstatesThanCounts = allowMoreSubstatesThanCounts;
		this.smoothParams = smoothParams;
		this.filter = filter;
		this.nTrees = nTrees;
		this.binarization = binarization;
		this.outFileName = outFileName;
		this.separateMergingThreshold = separateMergingThreshold;
		this.mergingPercentage = mergingPercentage;
		this.rare = rare;
		this.splitPosOut = splitPosOut;
		this.trainTrees = trainTrees;
		this.testTrees = testTrees;
		this.trainOutPath = trainOutPath;
		this.testOutPath = testOutPath;
		this.maxSentenceLength = maxSentenceLength;
		this.manualAnnotation = manualAnnotation;
	}

	public void splitPos(int numOfSplitIter) {
		for (int i = 0; i < ((FeatureNumberer) grammar.getTagNumberer())
				.maxNumberOfFeats(); i++) {
			for (int j = 0; j < numOfSplitIter; j++) {
				splitPOSByFeat(i, false);
			}
			try {
				ConstTools.writeConstTreeSentence(trainTrees, trainOutPath
						+ "_" + i + ".ptb");
				ConstTools.writeConstTreeSentence(testTrees, testOutPath + "_"
						+ i + ".ptb");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			ConstTools.writeConstTreeSentence(testTrees, testOutPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void splitPOSByFeat(int featId, boolean newMainPoses) {
		System.out.println("Splitting preterminals.");
		calculatePosPropability(featId);
		saveModel();
		boolean[][][] mergeThesePairs = mergePOSes(featId);
		recalculateTrees(featId, mergeThesePairs, newMainPoses);
		System.out.println("Splitting preterminals ends.");
	}

	public void splitPOSOld(int featId) {
		System.out.println("Splitting preterminals.");
		calculatePosPropability(featId);
		saveModel();
		boolean[][][] mergeThesePairs = mergePOSes(featId);

		Map<String, Map<String, String>> mergeMap = generateMergePairs(
				mergeThesePairs, grammar);
		saveMergeMap(mergeMap);
	}

	public void calculatePosPropability(int featId) {
		grammar = grammar.splitPOSByFeats(randomness,
				allowMoreSubstatesThanCounts, 0, featId);
		Smoother grSmoother = new NoSmoothing();
		grammar.setSmoother(grSmoother);
		short[] numSubStatesArray = grammar.numSubStates;

		Lexicon tmp_lexicon = new PosMergerSophisticatedLexicon(numSubStatesArray,
				SophisticatedLexicon.DEFAULT_SMOOTHING_CUTOFF, smoothParams,
				new NoSmoothing(), filter);

		int n = 0;
		boolean secondHalf = false;
		for (Tree<StateSet> stateSetTree : trainStateSetTrees) {
			secondHalf = (n++ > nTrees / 2.0);
			((PosMergerSophisticatedLexicon) tmp_lexicon).trainTreeWithPosData(
					stateSetTree, 0, null, secondHalf, false, rare, featId);
		}
		lexicon = new PosMergerSophisticatedLexicon(numSubStatesArray,
				SophisticatedLexicon.DEFAULT_SMOOTHING_CUTOFF, smoothParams,
				new NoSmoothing(), filter);
		for (Tree<StateSet> stateSetTree : trainStateSetTrees) {
			secondHalf = (n++ > nTrees / 2.0);
			((PosMergerSophisticatedLexicon) lexicon).trainTreeWithPosData(
					stateSetTree, 0, tmp_lexicon, secondHalf, false, rare,
					featId);
		}

		// maxLexicon.optimize();
		// grammar.optimize(0);

		numSubStatesArray = grammar.numSubStates;
		trainStateSetTrees = new PosMergerStateSetTreeList(trainStateSetTrees,
				numSubStatesArray, false);
		validationStateSetTrees = new PosMergerStateSetTreeList(
				validationStateSetTrees, numSubStatesArray, false);
		double maxLikelihood = GrammarTrainerForPosMerger.calculateLogLikelihood(
				grammar, lexicon, validationStateSetTrees);
		System.out
				.println("we get a validation likelihood of " + maxLikelihood);

	}

	public void saveModel() {
		ParserData pData = new ParserData(lexicon, grammar, null,
				Numberer.getNumberers(), grammar.numSubStates,
				GrammarTrainerForPosMerger.VERTICAL_MARKOVIZATION,
				GrammarTrainerForPosMerger.HORIZONTAL_MARKOVIZATION, binarization);
		String outTmpName = outFileName + "_1_featSplitted.gr";
		System.out.println("Saving grammar to " + outTmpName + ".");
		if (pData.Save(outTmpName))
			System.out.println("Saving successful.");
		else
			System.out.println("Saving failed!");
	}

	public boolean[][][] mergePOSes(int featId) {
		double[][] mergeWeights = GrammarMergerForPos.computeMergeWeightsModEM(
				grammar, lexicon, trainStateSetTrees);
		double[][][] deltas = GrammarMergerForPos.computeDeltasModEM(grammar,
				lexicon, mergeWeights, trainStateSetTrees, featId);
		boolean[][][] mergeThesePairs = GrammarMerger.determineMergePairs(
				deltas, separateMergingThreshold, mergingPercentage, grammar);
		return mergeThesePairs;
	}

	public void saveMergeMap(Map<String, Map<String, String>> mergeMap) {
		String mergeString = "";
		for (Entry<String, Map<String, String>> ent : mergeMap.entrySet()) {
			for (Entry<String, String> ent2 : ent.getValue().entrySet()) {
				mergeString += ent.getKey() + "\t" + ent2.getKey() + "\t"
						+ ent2.getValue() + "\n";
			}
		}

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					splitPosOut));
			writer.write(mergeString);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void recalculateTrees(int featId, boolean[][][] mergeThesePairs,
			boolean newMainPoses) {
		Map<String, Map<String, String>> mergeMap = generateMergePairsModifyNumberer(
				featId, mergeThesePairs);
		// saveMergeMap(mergeMap);
		mergeMap = convertMergePairs(mergeMap);

		FeatureNumberer featureNumberer = (FeatureNumberer) grammar
				.getTagNumberer();
		trainTrees = MergeFeatures.convertTags(trainTrees, mergeMap, featId,
				featureNumberer);
		if (testTrees != null) {
			testTrees = MergeFeatures.convertTags(testTrees, mergeMap, featId,
					featureNumberer);
		}

		List<Tree<String>> trainBinTrees = Corpus.binarizeAndFilterTrees(
				trainTrees, GrammarTrainerForPosMerger.VERTICAL_MARKOVIZATION,
				GrammarTrainerForPosMerger.HORIZONTAL_MARKOVIZATION,
				maxSentenceLength, binarization, manualAnnotation,
				GrammarTrainerForPosMerger.VERBOSE);

		trainStateSetTrees = new PosMergerStateSetTreeList(trainBinTrees,
				grammar.numSubStates, false, grammar.getTagNumberer());
	}

	public Map<String, Map<String, String>> generateMergePairsModifyNumberer(
			int featId, boolean[][][] mergeThesePairs) {
		Map<String, Map<String, String>> mergeMap = new HashMap<String, Map<String, String>>();
		FeatureNumberer featureNumberer = (FeatureNumberer) grammar
				.getTagNumberer();
		for (short state = 0; state < grammar.numSubStates.length; state++) {
			if (!grammar.isGrammarTag(state)) {
				String tag = (String) grammar.getTagNumberer().object(state);

				for (int i = 0; i < grammar.numSubStates[state]; i++) {
					for (int j = i + 1; j < grammar.numSubStates[state]; j++) {
						if (mergeThesePairs[state][i][j]) {
							Map<String, String> mergePairs;
							if (mergeMap.containsKey(tag)) {
								mergePairs = mergeMap.get(tag);
							} else {
								mergePairs = new HashMap<String, String>();
							}
							String feat1 = featureNumberer
									.getFeatValueIdToName().get((int) state)
									.get(featId).get(i);
							String feat2 = featureNumberer
									.getFeatValueIdToName().get((int) state)
									.get(featId).get(j);
							mergePairs.put(feat1, feat2);
							mergeMap.put(tag, mergePairs);

							// Modify tag numberer
							featureNumberer
									.mergeFeatValues(state, featId, i, j);
						}
					}
				}

				// Reorder merged feats
				featureNumberer.reorderFeatValues(state, featId);
			}
		}
		return mergeMap;
	}

	public Map<String, Map<String, String>> generateMergePairs(
			boolean[][][] mergeThesePairs, Grammar grammar) {
		Map<String, Map<String, String>> mergeMap = new HashMap<String, Map<String, String>>();
		for (short state = 0; state < grammar.numSubStates.length; state++) {
			if (!grammar.isGrammarTag(state)) {
				String tag = (String) grammar.getTagNumberer().object(state);
				for (int i = 0; i < grammar.numSubStates[state]; i++) {
					for (int j = i + 1; j < grammar.numSubStates[state]; j++) {
						if (mergeThesePairs[state][i][j]) {
							Map<String, String> mergePairs;
							if (mergeMap.containsKey(tag)) {
								mergePairs = mergeMap.get(tag);
							} else {
								mergePairs = new HashMap<String, String>();
							}
							String feat1 = ((FeatureNumberer) grammar
									.getTagNumberer()).getFeatValueIdToName()
									.get((int) state).get(0).get(i);
							String feat2 = ((FeatureNumberer) grammar
									.getTagNumberer()).getFeatValueIdToName()
									.get((int) state).get(0).get(j);
							mergePairs.put(feat1, feat2);
							mergeMap.put(tag, mergePairs);
						}
					}
				}
			}
		}

		return mergeMap;
	}

	public static Map<String, Map<String, String>> convertMergePairs(
			Map<String, Map<String, String>> mergeMap) {
		for (Entry<String, Map<String, String>> mergeMapEnt : mergeMap
				.entrySet()) {
			Map<String, String> newMergePairs = new HashMap<String, String>();
			for (Entry<String, String> mergePair : mergeMapEnt.getValue()
					.entrySet()) {
				newMergePairs.put(mergePair.getKey(), mergePair.getKey()
						+ Parameters.MergeSeparator + mergePair.getValue());
				newMergePairs.put(mergePair.getValue(), mergePair.getKey()
						+ Parameters.MergeSeparator + mergePair.getValue());
			}
			mergeMap.put(mergeMapEnt.getKey(), newMergePairs);
		}
		return mergeMap;
	}
}
