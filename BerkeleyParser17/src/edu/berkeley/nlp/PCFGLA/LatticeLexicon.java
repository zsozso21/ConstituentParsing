package edu.berkeley.nlp.PCFGLA;

//import ims.hypergraph.EvalBF;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.berkeley.nlp.PCFGLA.smoothing.Smoother;
import edu.berkeley.nlp.util.Numberer;

public class LatticeLexicon extends SophisticatedLexicon {
	protected Map<String, Set<String>> morph_analysis = new HashMap<String, Set<String>>();
	private Numberer tagNumberer = null;
	private static double WEIGHT = 0.000000001;
	protected Map<String, Map<String, Double>> wordsPerMainPos = new HashMap<String, Map<String, Double>>();
	protected Map<String, Map<String, Double>> mainPosPerWords = new HashMap<String, Map<String, Double>>();
	
	public void init() {
		try {
			morph_analysis = new HashMap<String, Set<String>>();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(
									"res/morph_analysis"),
							"UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				String l[] = line.split("\\s");
				if (!morph_analysis.containsKey(l[0]))
					morph_analysis.put(l[0], new HashSet<String>());
				morph_analysis.get(l[0]).add(l[2].equals("X") ? "N" : l[2]);
			}
			reader.close();

			reader = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(
									"res/lexicon_freq"),
							"UTF-8"));
			reader.readLine();
			while ((line = reader.readLine()) != null) {
				line = line.replaceFirst("^[\\s]*", "");
				String l[] = line.split("[\\t\\s]");
				double num = Double.parseDouble(l[0].replaceAll("\\s", ""));
				String word = l[1];
				String pos = l[2];
				word = word.replace("(", "-LRB-");
				word = word.replace(")", "-RRB-");
				pos = pos.replace("K", "PUNC");
				pos = pos.replace("-", "PUNC");
				pos = pos.replace("!", "PUNC");
				pos = pos.replace(",", "PUNC");
				pos = pos.replace(".", "PUNC");
				pos = pos.replace(":", "PUNC");
				pos = pos.replace(";", "PUNC");
				pos = pos.replace("?", "PUNC");
				if (!wordsPerMainPos.containsKey(pos))
					wordsPerMainPos.put(pos, new HashMap<String, Double>());
				wordsPerMainPos.get(pos).put(word, num);
				
				if (!mainPosPerWords.containsKey(word))
					mainPosPerWords.put(word, new HashMap<String, Double>());
				mainPosPerWords.get(word).put(pos, num);
			}
			reader.close();

			for (Entry<String, Map<String, Double>> ent : wordsPerMainPos.entrySet()) {
				double sum = 0.0;
				for (Entry<String, Double> ent2 : ent.getValue().entrySet()) {
					sum += ent2.getValue();
				}
				for (Entry<String, Double> ent2 : ent.getValue().entrySet()) {
					ent.getValue().put(ent2.getKey(), ent2.getValue() / sum);
				}
			}
			
			for (Entry<String, Map<String, Double>> ent : mainPosPerWords.entrySet()) {
				double sum = 0.0;
				for (Entry<String, Double> ent2 : ent.getValue().entrySet()) {
					sum += ent2.getValue();
				}
				for (Entry<String, Double> ent2 : ent.getValue().entrySet()) {
					ent.getValue().put(ent2.getKey(), ent2.getValue() / sum);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public LatticeLexicon(short[] numSubStates, int smoothingCutoff,
			double[] smoothParam, Smoother smoother, double threshold) {
		super(numSubStates, smoothingCutoff, smoothParam, smoother, threshold);
		init();
	}

	public LatticeLexicon(short[] numSubStates, int smoothingCutoff,
			double[] smoothParam, Smoother smoother, double threshold,
			Numberer tnum) {
		super(numSubStates, smoothingCutoff, smoothParam, smoother, threshold);
		init();
		tagNumberer = tnum;
	}

	public LatticeLexicon(int uwm, short[] numSubStates, int smoothingCutoff,
			double[] smoothParam, Smoother smoother, double threshold) {
		super(uwm, numSubStates, smoothingCutoff, smoothParam, smoother,
				threshold);
		init();
	}

	public LatticeLexicon(int uwm, short[] numSubStates, int smoothingCutoff,
			double[] smoothParam, Smoother smoother, double threshold, Numberer tnum) {
		super(uwm, numSubStates, smoothingCutoff, smoothParam, smoother,
				threshold);
		init();
		tagNumberer = tnum;
	}

	public SophisticatedLexicon splitAllStates(int[] counts,
			boolean moreSubstatesThanCounts, int mode) {
		SophisticatedLexicon l = super.splitAllStates(counts,
				moreSubstatesThanCounts, mode);
		((LatticeLexicon) l).tagNumberer = this.tagNumberer;
		return l;
	}

	public SophisticatedLexicon projectLexicon(double[] condProbs,
			int[][] mapping, int[][] toSubstateMapping) {
		SophisticatedLexicon l = super.projectLexicon(condProbs, mapping,
				toSubstateMapping);
		((LatticeLexicon) l).tagNumberer = this.tagNumberer;
		return l;
	}

	public SophisticatedLexicon copyLexicon() {
		SophisticatedLexicon l = super.copyLexicon();
		((LatticeLexicon) l).tagNumberer = this.tagNumberer;
		return l;
	}
	
	public double[] score(String word, short tag, int loc, boolean noSmoothing,
			boolean isSignature) {
		double[] resultArray = super.score(word, tag, loc, noSmoothing,
				isSignature);

		String pos = getMainPos(tagNumberer.object(tag)
				.toString());
		Set<String> possibleTags = morph_analysis.get(word);
		if (wordCounter.getCount(word) > 4
				|| possibleTags == null
				|| !possibleTags.contains(pos))
			return resultArray;

//		double pb_T_W = 1.0 / morph_analysis.get(word).size()
//				/ resultArray.length;

		double pb_T_W;
		if (mainPosPerWords.containsKey(word) && mainPosPerWords.get(word).containsKey(pos)) {
			pb_T_W = mainPosPerWords.get(word).get(pos);
		} else {
			pb_T_W = 0.0;
			System.err.println(word + " " + pos);
		}
		double c_W = wordCounter.getCount(word);
		double p_W = (c_W < 0.1 ? 1f : c_W) / totalTokens;
		for (int substate = 0; substate < resultArray.length; ++substate) {
			double c_Tseen = tagCounter[tag][substate];
			double p_T = (c_Tseen / totalTokens);
			double pb_W_T = pb_T_W * p_W / p_T;

			if (logarithmMode) {
				resultArray[substate] = Math.exp(resultArray[substate]);
			}

			resultArray[substate] = (resultArray[substate] * c_W + WEIGHT
					* pb_W_T)
					/ (c_W + WEIGHT);
		}

		smoother.smooth(tag, resultArray);
		if (logarithmMode) {
			for (int i = 0; i < resultArray.length; i++) {
				resultArray[i] = Math.log(resultArray[i]);
				if (Double.isNaN(resultArray[i]))
					resultArray[i] = Double.NEGATIVE_INFINITY;
			}
		}

		return resultArray;
	}

/*
	public double[] score(String word, short tag, int loc, boolean noSmoothing,
			boolean isSignature) {
		double[] resultArray = super.score(word, tag, loc, noSmoothing,
				isSignature);

		String mainPos = getMainPos(tagNumberer.object(tag)
				.toString());
		Set<String> possibleTags = morph_analysis.get(word);
		if (wordCounter.getCount(word) > 10
				|| possibleTags == null
				|| !possibleTags.contains(mainPos))
			return resultArray;

		double pb_T_W = 1.0 / possibleTags.size() / tagsPerMainPos.get(mainPos).size() / resultArray.length;
		double c_W = wordCounter.getCount(word);
		double p_W = (c_W < 0.1 ? 1f : c_W) / totalTokens;
		for (int substate = 0; substate < resultArray.length; ++substate) {
			double c_Tseen = tagCounter[tag][substate];
			double p_T = (c_Tseen / totalTokens);
			double pb_W_T = pb_T_W * p_W / p_T;

			if (logarithmMode) {
				resultArray[substate] = Math.exp(resultArray[substate]);
			}

			resultArray[substate] = (resultArray[substate] * c_W + WEIGHT
					* pb_W_T)
					/ (c_W + WEIGHT);
		}

		smoother.smooth(tag, resultArray);
		if (logarithmMode) {
			for (int i = 0; i < resultArray.length; i++) {
				resultArray[i] = Math.log(resultArray[i]);
				if (Double.isNaN(resultArray[i]))
					resultArray[i] = Double.NEGATIVE_INFINITY;
			}
		}

		return resultArray;
	}*/

	public static String getMainPos(String tag) {
		if (tag.equals("PUNC"))
			return "PUNC";
		return tag.length() > 0 && !tag.equals(null) ? tag.substring(0, 1)
				: "ERROR";
	}

	public void setTagNumberer(Numberer numb) {
		tagNumberer = numb;
	}

	public static void main(String[] args) throws Exception {
		if (args.length > 6) {
			WEIGHT = Double.parseDouble(args[6]);
			args = Arrays.copyOfRange(args, 0, 6);
		}
		BerkeleyParser.main(args);
		/*
		 * for(double i=0.1;i<=3.0;i+=0.5){ args[5]+=i; System.out.println(i);
		 * WEIGHT = i; BerkeleyParser.main(args); }
		 */
	}
}
