package edu.berkeley.nlp.PCFGLA;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import constituent.berkeley.PCFLGA.FeatureNumberer;

import edu.berkeley.nlp.PCFGLA.StateSetTreeList;
import edu.berkeley.nlp.syntax.StateSet;
import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.util.Numberer;

public class PosMergerStateSetTreeList extends StateSetTreeList {
	
	public PosMergerStateSetTreeList(List<Tree<String>> trees, short[] numStates,
			boolean allSplitTheSame, Numberer tagNumberer) {
		this.trees = new ArrayList<Tree<StateSet>>();
		for (Tree<String> tree : trees) {
			this.trees.add(stringTreeToStatesetTree(tree, numStates,
					allSplitTheSame, tagNumberer));
			tree = null;
		}
	}

	public PosMergerStateSetTreeList(StateSetTreeList treeList, short[] numStates,
			boolean constant) {
		this.trees = new ArrayList<Tree<StateSet>>();
		for (Tree<StateSet> tree : treeList.trees) {
			this.trees.add(resizeStateSetTree(tree, numStates, constant));
		}
	}

	public PosMergerStateSetTreeList() {
		this.trees = new ArrayList<Tree<StateSet>>();
	}
	
	
	
	private static Tree<StateSet> stringTreeToStatesetTree(Tree<String> tree,
			short[] numStates, boolean allSplitTheSame, Numberer tagNumberer,
			boolean splitRoot, int from, int to) {
		if (tree.isLeaf()) {
			StateSet newState = new StateSet(zero, one, tree.getLabel()
					.intern(), (short) from, (short) to);
			return new Tree<StateSet>(newState);
		}
		String labelStr = tree.getLabel();
		Map<Integer, Integer> feats = null;
		if (tree.isPreTerminal()) {
			feats = ((FeatureNumberer)tagNumberer).numberFeats(labelStr);
			labelStr = ((FeatureNumberer)tagNumberer).getFeaturizer().getPos(labelStr);
		}
		short label = (short) tagNumberer.number(labelStr);
		if (label < 0)
			label = 0;
		// System.out.println(label + " " +tree.getLabel());
		if (label >= numStates.length) {
			// System.err.println("Have never seen this state before: "+tree.getLabel());
			// StateSet newState = new StateSet(zero, one,
			// tree.getLabel().intern(),(short)from,(short)to);
			// return new Tree<StateSet>(newState);
		}
		short nodeNumStates = (allSplitTheSame || numStates.length <= label) ? numStates[0]
				: numStates[label];
		if (!splitRoot)
			nodeNumStates = 1;
		StateSet newState = new PosMergerStateSet(label, nodeNumStates, null,
				(short) from, (short) to, feats);
		Tree<StateSet> newTree = new Tree<StateSet>(newState);
		List<Tree<StateSet>> newChildren = new ArrayList<Tree<StateSet>>();
		for (Tree<String> child : tree.getChildren()) {
			short length = (short) child.getYield().size();
			Tree<StateSet> newChild = stringTreeToStatesetTree(child,
					numStates, allSplitTheSame, tagNumberer, true, from, from
							+ length);
			from += length;
			newChildren.add(newChild);
		}
		newTree.setChildren(newChildren);
		return newTree;
	}
	
	/*
	 * Unmodified methodes.
	 */
	public static Tree<StateSet> stringTreeToStatesetTree(Tree<String> tree,
			short[] numStates, boolean allSplitTheSame, Numberer tagNumberer) {
		Tree<StateSet> result = stringTreeToStatesetTree(tree, numStates,
				allSplitTheSame, tagNumberer, false, 0, tree.getYield().size());
		// set the positions properly:
		List<StateSet> words = result.getYield();
		// for all words in sentence
		for (short position = 0; position < words.size(); position++) {
			words.get(position).from = position;
			words.get(position).to = (short) (position + 1);
		}
		return result;
	}
	
	public static void initializeTagNumberer(List<Tree<String>> trees,
			Numberer tagNumberer) {
		short[] nSub = new short[2];
		nSub[0] = 1;
		nSub[1] = 1;
		for (Tree<String> tree : trees) {
			Tree<StateSet> tmp = stringTreeToStatesetTree(tree, nSub, true,
					tagNumberer);
		}
	}
	
	private static Tree<StateSet> resizeStateSetTree(Tree<StateSet> tree,
			short[] numStates, boolean constant) {
		if (tree.isLeaf()) {
			return tree;
		}
		short state = tree.getLabel().getState();
		short newNumStates = constant ? numStates[0] : numStates[state];
		StateSet newState = new PosMergerStateSet(tree.getLabel(), newNumStates);
		Tree<StateSet> newTree = new Tree<StateSet>(newState);
		List<Tree<StateSet>> newChildren = new ArrayList<Tree<StateSet>>();
		for (Tree<StateSet> child : tree.getChildren()) {
			newChildren.add(resizeStateSetTree(child, numStates, constant));
		}
		newTree.setChildren(newChildren);
		return newTree;
	}
}
