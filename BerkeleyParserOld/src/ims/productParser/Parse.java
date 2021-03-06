package ims.productParser;

import java.util.LinkedList;
import java.util.List;

import edu.berkeley.nlp.PCFGLA.CoarseToFineMaxRuleParser;
import edu.berkeley.nlp.PCFGLA.TreeAnnotations;
import edu.berkeley.nlp.syntax.Tree;

public class Parse {
		public Parse(){}

		public Parse(Tree<String> ptree, CoarseToFineMaxRuleParser parser) {
			if(!ptree.getChildren().isEmpty()){ 
				tree = TreeAnnotations.unAnnotateTree(ptree);
				confidence = BerkeleyParserWrapper.calcConfidence(parser, tree);
			}
		}

		public Parse(Tree<String> ptree, CoarseToFineMaxRuleParser parser, List<String> sentence) {
			this(ptree,parser);
			if(ptree.getChildren().isEmpty()){ 
				createDummyParse(sentence);
			}
		}

		public Parse(List<String> sentence) {
			createDummyParse(sentence);
		}
		
		public void createDummyParse(List<String> sentence) {
			List<Tree<String>> preterminal_list = new LinkedList<Tree<String>>();
			for(String w : sentence){
				Tree<String> preterminal = new Tree<String>("NN");
				List<Tree<String>> l = new LinkedList<Tree<String>>();
				l.add(new Tree<String>(w));
				preterminal.setChildren(l);
				preterminal_list.add(preterminal);
			}
			tree = new Tree<String>("ROOT");
			tree.setChildren(preterminal_list);
			confidence = Double.NEGATIVE_INFINITY;
		}
		
		public Tree<String> tree = null;
		public Double confidence = Double.NEGATIVE_INFINITY;
	}
