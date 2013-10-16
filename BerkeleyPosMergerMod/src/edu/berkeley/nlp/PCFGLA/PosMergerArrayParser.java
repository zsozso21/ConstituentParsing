package edu.berkeley.nlp.PCFGLA;

import java.util.Arrays;
import java.util.List;

import edu.berkeley.nlp.PCFGLA.ArrayParser;
import edu.berkeley.nlp.PCFGLA.Lexicon;
import edu.berkeley.nlp.syntax.StateSet;
import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.util.ArrayUtil;

public class PosMergerArrayParser extends ArrayParser {

	public PosMergerArrayParser() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PosMergerArrayParser(Grammar gr, Lexicon lex) {
		super(gr, lex);
		// TODO Auto-generated constructor stub
	}


//	void doInsideScores(Tree<StateSet> tree, boolean noSmoothing,
//			boolean debugOutput, double[][][] spanScores) {
//		if (grammar.isLogarithmMode() || lexicon.isLogarithmMode())
//			throw new Error(
//					"Grammar in logarithm mode!  Cannot do inside scores!");
//		if (tree.isLeaf()) {
//			return;
//		}
//		List<Tree<StateSet>> children = tree.getChildren();
//		for (Tree<StateSet> child : children) {
//			if (!child.isLeaf())
//				doInsideScores(child, noSmoothing, debugOutput, spanScores);
//		}
//		StateSet parent = tree.getLabel();
//		short pState = parent.getState();
//		int nParentStates = parent.numSubStates();
//		if (tree.isPreTerminal()) {
//			// Plays a role similar to initializeChart()
//			StateSet wordStateSet = tree.getChildren().get(0).getLabel();
//			double[] lexiconScores = lexicon.score(wordStateSet, pState,
//					noSmoothing, false);
//			if (lexiconScores.length != nParentStates) {
//				System.out.println("Have more scores than substates!"
//						+ lexiconScores.length + " " + nParentStates);// truncate
//																		// the
//																		// array
//			}
//			parent.setIScores(lexiconScores);
//			parent.scaleIScores(0);
//		} else {
//			switch (children.size()) {
//			case 0:
//				break;
//			case 1:
//				StateSet child = children.get(0).getLabel();
//				short cState = child.getState();
//				int nChildStates = child.numSubStates();
//				double[][] uscores = grammar.getUnaryScore(pState, cState);
//				double[] iScores = new double[nParentStates];
//				boolean foundOne = false;
//				for (int j = 0; j < nChildStates; j++) {
//					if (uscores[j] != null) { // check whether one of the
//												// parents can produce this
//												// child
//						double cS = child.getIScore(j);
//						if (cS == 0)
//							continue;
//						for (int i = 0; i < nParentStates; i++) {
//							double rS = uscores[j][i]; // rule score
//							if (rS == 0)
//								continue;
//							double res = ((PosMergerStateSet)child).weightForFeat(j) * rS * cS;
//
//							/*
//							 * if (res == 0) {
//							 * System.out.println("Prevented an underflow: rS "
//							 * +rS+" cS "+cS); res = Double.MIN_VALUE; }
//							 */
//							iScores[i] += res;
//							foundOne = true;
//						}
//					}
//				}
//				if (debugOutput && !foundOne) {
//					System.out.println("iscore reached zero!");
//					System.out.println(grammar.getUnaryRule(pState, cState));
//					System.out.println(Arrays.toString(iScores));
//					System.out.println(ArrayUtil.toString(uscores));
//					System.out.println(Arrays.toString(child.getIScores()));
//				}
//				parent.setIScores(iScores);
//				parent.scaleIScores(child.getIScale());
//				break;
//			case 2:
//				StateSet leftChild = children.get(0).getLabel();
//				StateSet rightChild = children.get(1).getLabel();
//				int nLeftChildStates = leftChild.numSubStates();
//				int nRightChildStates = rightChild.numSubStates();
//				short lState = leftChild.getState();
//				short rState = rightChild.getState();
//				double[][][] bscores = grammar.getBinaryScore(pState, lState,
//						rState);
//				double[] iScores2 = new double[nParentStates];
//				boolean foundOne2 = false;
//				for (int j = 0; j < nLeftChildStates; j++) {
//					double lcS = leftChild.getIScore(j);
//					if (lcS == 0)
//						continue;
//					for (int k = 0; k < nRightChildStates; k++) {
//						double rcS = rightChild.getIScore(k);
//						if (rcS == 0)
//							continue;
//						if (bscores[j][k] != null) { // check whether one of the
//														// parents can produce
//														// these kids
//							for (int i = 0; i < nParentStates; i++) {
//								double rS = bscores[j][k][i];
//								if (rS == 0)
//									continue;
//								double res = ((PosMergerStateSet)leftChild).weightForFeat(j) * ((PosMergerStateSet)rightChild).weightForFeat(k) * rS * lcS * rcS;
//								/*
//								 * if (res == 0) {
//								 * System.out.println("Prevented an underflow: rS "
//								 * +rS+" lcS "+lcS+" rcS "+rcS); res =
//								 * Double.MIN_VALUE; }
//								 */
//								iScores2[i] += res;
//								foundOne2 = true;
//							}
//						}
//					}
//				}
//				if (spanScores != null) {
//					for (int i = 0; i < nParentStates; i++) {
//						iScores2[i] *= spanScores[parent.from][parent.to][stateClass[pState]];
//					}
//				}
//
//				// if (!foundOne2)
//				// System.out.println("Did not find a way to build binary transition from "+pState+" to "+lState+" and "+rState+" "+ArrayUtil.toString(bscores));
//				if (debugOutput && !foundOne2) {
//					System.out.println("iscore reached zero!");
//					System.out.println(grammar.getBinaryRule(pState, lState,
//							rState));
//					System.out.println(Arrays.toString(iScores2));
//					System.out.println(Arrays.toString(bscores));
//					System.out.println(Arrays.toString(leftChild.getIScores()));
//					System.out
//							.println(Arrays.toString(rightChild.getIScores()));
//				}
//				parent.setIScores(iScores2);
//				parent.scaleIScores(leftChild.getIScale()
//						+ rightChild.getIScale());
//				break;
//			default:
//				throw new Error("Malformed tree: more than two children");
//			}
//		}
//	}
	
	void doInsideScores(Tree<StateSet> tree, boolean noSmoothing,
			boolean debugOutput, double[][][] spanScores) {
		if (grammar.isLogarithmMode() || lexicon.isLogarithmMode())
			throw new Error(
					"Grammar in logarithm mode!  Cannot do inside scores!");
		if (tree.isLeaf()) {
			return;
		}
		List<Tree<StateSet>> children = tree.getChildren();
		for (Tree<StateSet> child : children) {
			if (!child.isLeaf())
				doInsideScores(child, noSmoothing, debugOutput, spanScores);
		}
		StateSet parent = tree.getLabel();
		short pState = parent.getState();
		int nParentStates = parent.numSubStates();
		if (tree.isPreTerminal()) {
			// Plays a role similar to initializeChart()
			StateSet wordStateSet = tree.getChildren().get(0).getLabel();
			double[] lexiconScores = lexicon.score(wordStateSet, pState,
					noSmoothing, false);
			
			for (int i = 0; i < lexiconScores.length; i++) {
				lexiconScores[i] = lexiconScores[i]*((PosMergerStateSet)parent).weightForFeat(i);
			}
			
			if (lexiconScores.length != nParentStates) {
				System.out.println("Have more scores than substates!"
						+ lexiconScores.length + " " + nParentStates);// truncate
																		// the
																		// array
			}
			parent.setIScores(lexiconScores);
			parent.scaleIScores(0);
		} else {
			switch (children.size()) {
			case 0:
				break;
			case 1:
				StateSet child = children.get(0).getLabel();
				short cState = child.getState();
				int nChildStates = child.numSubStates();
				double[][] uscores = grammar.getUnaryScore(pState, cState);
				double[] iScores = new double[nParentStates];
				boolean foundOne = false;
				for (int j = 0; j < nChildStates; j++) {
					if (uscores[j] != null) { // check whether one of the
												// parents can produce this
												// child
						double cS = child.getIScore(j);
						if (cS == 0)
							continue;
						for (int i = 0; i < nParentStates; i++) {
							double rS = uscores[j][i]; // rule score
							if (rS == 0)
								continue;
							double res = rS * cS;
							/*
							 * if (res == 0) {
							 * System.out.println("Prevented an underflow: rS "
							 * +rS+" cS "+cS); res = Double.MIN_VALUE; }
							 */
							iScores[i] += res;
							foundOne = true;
						}
					}
				}
				if (debugOutput && !foundOne) {
					System.out.println("iscore reached zero!");
					System.out.println(grammar.getUnaryRule(pState, cState));
					System.out.println(Arrays.toString(iScores));
					System.out.println(ArrayUtil.toString(uscores));
					System.out.println(Arrays.toString(child.getIScores()));
				}
				parent.setIScores(iScores);
				parent.scaleIScores(child.getIScale());
				break;
			case 2:
				StateSet leftChild = children.get(0).getLabel();
				StateSet rightChild = children.get(1).getLabel();
				int nLeftChildStates = leftChild.numSubStates();
				int nRightChildStates = rightChild.numSubStates();
				short lState = leftChild.getState();
				short rState = rightChild.getState();
				double[][][] bscores = grammar.getBinaryScore(pState, lState,
						rState);
				double[] iScores2 = new double[nParentStates];
				boolean foundOne2 = false;
				for (int j = 0; j < nLeftChildStates; j++) {
					double lcS = leftChild.getIScore(j);
					if (lcS == 0)
						continue;
					for (int k = 0; k < nRightChildStates; k++) {
						double rcS = rightChild.getIScore(k);
						if (rcS == 0)
							continue;
						if (bscores[j][k] != null) { // check whether one of the
														// parents can produce
														// these kids
							for (int i = 0; i < nParentStates; i++) {
								double rS = bscores[j][k][i];
								if (rS == 0)
									continue;
								double res = rS * lcS * rcS;
								/*
								 * if (res == 0) {
								 * System.out.println("Prevented an underflow: rS "
								 * +rS+" lcS "+lcS+" rcS "+rcS); res =
								 * Double.MIN_VALUE; }
								 */
								iScores2[i] += res;
								foundOne2 = true;
							}
						}
					}
				}
				if (spanScores != null) {
					for (int i = 0; i < nParentStates; i++) {
						iScores2[i] *= spanScores[parent.from][parent.to][stateClass[pState]];
					}
				}

				// if (!foundOne2)
				// System.out.println("Did not find a way to build binary transition from "+pState+" to "+lState+" and "+rState+" "+ArrayUtil.toString(bscores));
				if (debugOutput && !foundOne2) {
					System.out.println("iscore reached zero!");
					System.out.println(grammar.getBinaryRule(pState, lState,
							rState));
					System.out.println(Arrays.toString(iScores2));
					System.out.println(Arrays.toString(bscores));
					System.out.println(Arrays.toString(leftChild.getIScores()));
					System.out
							.println(Arrays.toString(rightChild.getIScores()));
				}
				parent.setIScores(iScores2);
				parent.scaleIScores(leftChild.getIScale()
						+ rightChild.getIScale());
				break;
			default:
				throw new Error("Malformed tree: more than two children");
			}
		}
	}
	
	void doOutsideScores(Tree<StateSet> tree, boolean unaryAbove,
			double[][][] spanScores) {
		if (grammar.isLogarithmMode() || lexicon.isLogarithmMode())
			throw new Error(
					"Grammar in logarithm mode!  Cannot do inside scores!");
		if (tree.isLeaf())
			return;
		List<Tree<StateSet>> children = tree.getChildren();
		StateSet parent = tree.getLabel();
		short pState = parent.getState();
		int nParentStates = parent.numSubStates();
		// this sets the outside scores for the children
		if (tree.isPreTerminal()) {

		} else {
			double[] parentScores = parent.getOScores();
			if (spanScores != null && !unaryAbove) {
				for (int i = 0; i < nParentStates; i++) {
					parentScores[i] *= spanScores[parent.from][parent.to][stateClass[pState]];
				}
			}
			switch (children.size()) {
			case 0:
				// Nothing to do
				break;
			case 1:
				StateSet child = children.get(0).getLabel();
				short cState = child.getState();
				int nChildStates = child.numSubStates();
				// UnaryRule uR = new UnaryRule(pState,cState);
				double[][] uscores = grammar.getUnaryScore(pState, cState);
				double[] oScores = new double[nChildStates];
				for (int j = 0; j < nChildStates; j++) {
					if (uscores[j] != null) {
						double childScore = 0;
						for (int i = 0; i < nParentStates; i++) {
							double pS = parentScores[i];
							if (pS == 0)
								continue;
							double rS = uscores[j][i]; // rule score
							if (rS == 0)
								continue;
							childScore += pS * rS;
						}
						oScores[j] = childScore;
					}
				}
				child.setOScores(oScores);
				child.scaleOScores(parent.getOScale());
				unaryAbove = true;
				break;
			case 2:
				StateSet leftChild = children.get(0).getLabel();
				StateSet rightChild = children.get(1).getLabel();
				int nLeftChildStates = leftChild.numSubStates();
				int nRightChildStates = rightChild.numSubStates();
				short lState = leftChild.getState();
				short rState = rightChild.getState();
				// double[] leftScoresToAdd -> use childScores array instead =
				// new double[nRightChildStates * nParentStates];
				// double[][] rightScoresToAdd -> use binaryScores array instead
				// = new double[nRightChildStates][nLeftChildStates *
				// nParentStates];
				double[][][] bscores = grammar.getBinaryScore(pState, lState,
						rState);
				double[] lOScores = new double[nLeftChildStates];
				double[] rOScores = new double[nRightChildStates];
				for (int j = 0; j < nLeftChildStates; j++) {
					double lcS = leftChild.getIScore(j);
					double leftScore = 0;
					for (int k = 0; k < nRightChildStates; k++) {
						double rcS = rightChild.getIScore(k);
						if (bscores[j][k] != null) {
							for (int i = 0; i < nParentStates; i++) {
								double pS = parentScores[i];
								if (pS == 0)
									continue;
								double rS = bscores[j][k][i];
								if (rS == 0)
									continue;
								leftScore += pS * rS * rcS;
								rOScores[k] += pS * rS * lcS;
							}
						}
						lOScores[j] = leftScore;
					}
				}
				leftChild.setOScores(lOScores);
				leftChild.scaleOScores(parent.getOScale()
						+ rightChild.getIScale());
				rightChild.setOScores(rOScores);
				rightChild.scaleOScores(parent.getOScale()
						+ leftChild.getIScale());
				unaryAbove = false;
				break;
			default:
				throw new Error("Malformed tree: more than two children");
			}
			for (Tree<StateSet> child : children) {
				doOutsideScores(child, unaryAbove, spanScores);
			}
		}
	}
	
	@Override
	public void doInsideOutsideScores(Tree<StateSet> tree, boolean noSmoothing,
			boolean debugOutput) {
		doInsideScores(tree, noSmoothing, debugOutput, null);
		setRootOutsideScore(tree);
		doOutsideScores(tree, false, null);
	}

	
}
