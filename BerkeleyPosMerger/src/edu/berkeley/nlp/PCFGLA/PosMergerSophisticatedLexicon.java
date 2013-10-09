package edu.berkeley.nlp.PCFGLA;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import edu.berkeley.nlp.PCFGLA.GrammarTrainer;
import edu.berkeley.nlp.PCFGLA.Lexicon;
import edu.berkeley.nlp.PCFGLA.SophisticatedLexicon;
import edu.berkeley.nlp.PCFGLA.smoothing.Smoother;
import edu.berkeley.nlp.syntax.StateSet;
import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.util.Counter;
import edu.berkeley.nlp.util.ScalingTools;

public class PosMergerSophisticatedLexicon extends SophisticatedLexicon {

	public PosMergerSophisticatedLexicon(short[] numSubStates, int smoothingCutoff,
			double[] smoothParam, Smoother smoother, double threshold) {
		super(numSubStates, smoothingCutoff, smoothParam, smoother, threshold);
		
	}

	/**
	 * Trains this lexicon on the Collection of trees.
	 */
	public void trainTreeWithPosData(Tree<StateSet> trainTree, double randomness,
			Lexicon oldLexicon, boolean secondHalf, boolean noSmoothing,
			int threshold, int featId) {
		// scan data
		// for all substates that the word's preterminal tag has
		double sentenceScore = 0;
		if (randomness == -1) {
			sentenceScore = trainTree.getLabel().getIScore(0);
			if (sentenceScore == 0) {
				System.out
						.println("Something is wrong with this tree. I will skip it.");
				return;
			}
		}
		int sentenceScale = trainTree.getLabel().getIScale();

		List<StateSet> words = trainTree.getYield();
		List<StateSet> tags = trainTree.getPreTerminalYield();
		if (words.size() != tags.size()) {
			System.out.println("Yield an preterminal yield do not match!");
			System.out.println(words.toString());
			System.out.println(tags.toString());
		}

		Counter<String> oldWordCounter = null;
		if (oldLexicon != null) {
			oldWordCounter = oldLexicon.getWordCounter();
		}
		// for all words in sentence
		for (int position = 0; position < words.size(); position++) {
			totalWords++;
			String word = words.get(position).getWord();
			int nSubStates = tags.get(position).numSubStates();
			short tag = tags.get(position).getState();

			String sig = getCachedSignature(word, position);
			wordCounter.incrementCount(sig, 0);

			if (unseenWordToTagCounters[tag] == null) {
				unseenWordToTagCounters[tag] = new HashMap<String, double[]>();
			}
			double[] substateCounter2 = unseenWordToTagCounters[tag].get(sig);
			if (substateCounter2 == null) {
				// System.out.print("Sig "+sig+" word "+ word+" pos "+position);
				substateCounter2 = new double[numSubStates[tag]];
				unseenWordToTagCounters[tag].put(sig, substateCounter2);
			}

			// guarantee that the wordToTagCounter element exists so we can
			// tally the combination
			if (wordToTagCounters[tag] == null) {
				wordToTagCounters[tag] = new HashMap<String, double[]>();
			}
			double[] substateCounter = wordToTagCounters[tag].get(word);
			if (substateCounter == null) {
				substateCounter = new double[numSubStates[tag]];
				wordToTagCounters[tag].put(word, substateCounter);
			}

			double[] oldLexiconScores = null;
			if (randomness == -1) {
				oldLexiconScores = oldLexicon.score(word, tag, position,
						noSmoothing, false);
			}

			StateSet currentState = tags.get(position);
			double scale = ScalingTools.calcScaleFactor(currentState
					.getOScale() - sentenceScale)
					/ sentenceScore;
			// double weightSum = 0;

			Map<Integer, Integer> feats = ((PosMergerStateSet)tags.get(position)).getFeats();
			int feat = feats.containsKey(featId) ? ((PosMergerStateSet)tags.get(position)).getFeats().get(featId) : 0;
			for (short substate = 0; substate < nSubStates; substate++) {
				if (feat != substate) {
					continue;
				}
				
				double weight = 1;
				if (randomness == -1) {
					// weight by the probability of seeing the tag and word
					// together, given the sentence
					if (!Double.isInfinite(scale)) {
						weight = currentState.getOScore(substate)
								* oldLexiconScores[substate] * scale;
					} else {
						weight = Math.exp(Math.log(ScalingTools.SCALE)
								* (currentState.getOScale() - sentenceScale)
								- Math.log(sentenceScore)
								+ Math.log(currentState.getOScore(substate))
								+ Math.log(oldLexiconScores[substate]));
					}
					// weightSum+=weight;
				} else if (randomness == 0) {
					// for the baseline
					weight = 1;
				} else {
					// add a bit of randomness
					weight = GrammarTrainer.RANDOM.nextDouble() * randomness
							/ 100.0 + 1.0;
				}
				
				if (weight == 0) {
					continue;
				}
				// tally in the tag with the given weight
				substateCounter[substate] += weight;
				// update the counters
				tagCounter[tag][substate] += weight;
				wordCounter.incrementCount(word, weight);
				totalTokens += weight;

				if (Double.isNaN(totalTokens)) {
					throw new Error(
							"totalTokens is NaN: this would fail if we let it continue!");
				}

				if (oldLexicon != null
						&& oldWordCounter.getCount(word) < threshold + 0.5) {
					wordCounter.incrementCount(sig, weight);
					substateCounter2[substate] += weight;
					unseenTagCounter[tag][substate] += weight;
					totalUnseenTokens += weight;
				}
			}
		}
	}
}
