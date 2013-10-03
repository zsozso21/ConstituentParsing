package edu.berkeley.nlp.PCFGLA;

//import ims.hypergraph.EvalBF;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.berkeley.nlp.PCFGLA.smoothing.Smoother;
import edu.berkeley.nlp.util.Numberer;

public class LatticeLexicon extends SophisticatedLexicon {
	protected Map<String, Set<String>> morph_analysis = new HashMap<String, Set<String>>();
	private Numberer tagNumberer = null;
	private static double WEIGHT;

	public void init(){
		try{  
		  morph_analysis = new HashMap<String, Set<String>>();
	      BufferedReader reader = new BufferedReader(new InputStreamReader(
	              new FileInputStream("../morph_analysis"), "UTF-8"));
	      String line;
	      while ((line = reader.readLine()) != null) {
	    	  String l[] = line.split("\\s");
	    	  if(!morph_analysis.containsKey(l[0]))
	    		  morph_analysis.put(l[0], new HashSet<String>());
	    	  morph_analysis.get(l[0]).add(l[2].equals("X") ?  "N" : l[2]);
	      }
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public LatticeLexicon(short[] numSubStates, int smoothingCutoff, double[] smoothParam, 
			Smoother smoother, double threshold, Numberer wnum) {
		super(numSubStates, smoothingCutoff, smoothParam, smoother, threshold,wnum);
		init();
	}

	public LatticeLexicon(short[] numSubStates, int smoothingCutoff, double[] smoothParam, 
			Smoother smoother, double threshold, Numberer wnum, Numberer tnum) {
		super(numSubStates, smoothingCutoff, smoothParam, smoother, threshold,wnum);
		init();
		tagNumberer = tnum;
	}

	public LatticeLexicon(int uwm, short[] numSubStates, int smoothingCutoff, double[] smoothParam, 
			Smoother smoother, double threshold, Numberer wnum) {
		super(uwm,numSubStates, smoothingCutoff, smoothParam, smoother, threshold,wnum);
		init();
	}

	public LatticeLexicon(int uwm, short[] numSubStates, int smoothingCutoff, double[] smoothParam, 
			Smoother smoother, double threshold, Numberer wnum, Numberer tnum) {
		super(uwm,numSubStates, smoothingCutoff, smoothParam, smoother, threshold,wnum);
		init();
		tagNumberer = tnum;
	}

	public SophisticatedLexicon splitAllStates(int[] counts, boolean moreSubstatesThanCounts, int mode) {
		SophisticatedLexicon l = super.splitAllStates(counts, moreSubstatesThanCounts, mode);
		((LatticeLexicon)l).tagNumberer = this.tagNumberer;
		return l;
	}	

	public SophisticatedLexicon projectLexicon(double[] condProbs, int[][] mapping, int[][] toSubstateMapping) {
		SophisticatedLexicon l = super.projectLexicon(condProbs, mapping, toSubstateMapping);
		((LatticeLexicon)l).tagNumberer = this.tagNumberer;
		return l;
	}	

	public SophisticatedLexicon copyLexicon() {
		SophisticatedLexicon l = super.copyLexicon();
		((LatticeLexicon)l).tagNumberer = this.tagNumberer;
		return l;
	}	

	  public double[] score(String word, short tag, int loc, boolean noSmoothing, boolean isSignature) {
  	    double[] resultArray = super.score(word, tag, loc, noSmoothing, isSignature);  
  	    
  	    if(!morph_analysis.get(word).contains(tagNumberer.object(tag).toString()))
        	return resultArray;

    	double pb_T_W = 1.0/morph_analysis.get(word).size()/resultArray.length;
    	double c_W = wordCounter.getCount(word);
    	double p_W = (c_W<0.1 ? 1f : c_W) / totalTokens;
		for(int substate=0; substate<resultArray.length; ++substate){
			double c_Tseen = tagCounter[tag][substate];
    		double p_T = (c_Tseen / totalTokens);
			double pb_W_T = pb_T_W * p_W / p_T;

			double c_TW = 1f;
	  	    if (wordToTagCounters[tag]!=null &&
					wordToTagCounters[tag].get(word)!=null) {
				c_TW = wordToTagCounters[tag].get(word)[substate];
			}

	  	    if (logarithmMode) {
	  	    	resultArray[substate]=Math.exp(resultArray[substate]);
	  	    }

	  	    resultArray[substate] = (resultArray[substate]*c_TW + WEIGHT*pb_W_T) / (c_TW + WEIGHT);
		}
	    	
        smoother.smooth(tag,resultArray);
        if (logarithmMode) {
        	for (int i=0; i<resultArray.length; i++) {
        		resultArray[i] = Math.log(resultArray[i]);
        		if (Double.isNaN(resultArray[i]))
        			resultArray[i] = Double.NEGATIVE_INFINITY;
        	}
        }

        return resultArray;
    }

	public void setTagNumberer(Numberer numb) {
		tagNumberer  = numb;
	}
	    	
	public static void main(String[] args) throws Exception {
		for(double i=0.1;i<=3.0;i+=0.5){
			args[5]+=i;
			System.out.println(i);
			WEIGHT = i;
			BerkeleyParser.main(args);
		}
	}
}
