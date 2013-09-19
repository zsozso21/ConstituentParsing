package ims.productParser;

import java.util.LinkedList;
import java.util.List;

import edu.berkeley.nlp.PCFGLA.CoarseToFineNBestParser;
import edu.berkeley.nlp.PCFGLA.GrammarTrainer;
import edu.berkeley.nlp.PCFGLA.Option;
import edu.berkeley.nlp.PCFGLA.OptionParser;
import edu.berkeley.nlp.PCFGLA.ParserData;
import edu.berkeley.nlp.syntax.Tree;

public class ProductParserTrainer implements Runnable {
	Thread thread;
	int randSeed;
	CoarseToFineNBestParser parser;
	static List<Tree<String>> trainTrees;
	static int unknownLevel = 1;
	static int numSplits = 3;
	
	public ProductParserTrainer(int _randSeed){
		thread = new Thread(this);
		randSeed = _randSeed;
		parser = null;
	}
	
	public ProductParserTrainer(int _randSeed, CoarseToFineNBestParser origparser){
		this(_randSeed);
		parser = origparser;
	}

	public Thread getThread(){
		return thread;
	}

	public void run() {
		parser = BerkeleyParserWrapper.constructNBestParser(BerkeleyParserWrapper.train(trainTrees, randSeed, parser));
  }

	public static ProductParserData train(List<Tree<String>> corpus, int nParser, ProductParserData origGrammars){
		trainTrees = corpus;
		if(origGrammars != null){
			numSplits=1;
		}
		
		List<ProductParserTrainer> threads = new LinkedList<ProductParserTrainer>();
		for(int id=0;id<nParser;++id){
			CoarseToFineNBestParser origP = origGrammars==null ? null : origGrammars.parsers.get(id);
			ProductParserTrainer p = new ProductParserTrainer(id, origP);
			threads.add(p);
			p.getThread().start();
		}
		try {
			for(ProductParserTrainer m : threads)
				m.getThread().join();
		} catch (InterruptedException e) {
			System.out.println("Main thread Interrupted");
		}

		ProductParserData ppd = new ProductParserData();
		for(ProductParserTrainer p : threads){
			ppd.parsers.add(p.parser);
		}
		
		return ppd;
	}
	
	public static class Options {

		@Option(name = "-out", required = true, usage = "Output File for Grammar (Required)")
		public String outFileName;
		
		@Option(name = "-path", required = true, usage = "Path to Corpus (Required)")
		public String path = null;

		@Option(name = "-nParser", usage = "The number of parsers (Default: 4)")
		public int nParser = 4;

		@Option(name = "-uwm", usage = "Lexicon's unknownLevel")
		public int unknownLevel = 5;
		
		@Option(name = "-SMcycles", usage = "The number of split&merge iterations (Default: 3)")
		public int numSplits = 3;
		
		@Option(name = "-in", usage = "Input File for Grammar")
		public String inFile = null;
	}
	
	public static void main(String[] args) {
		OptionParser optParser = new OptionParser(Options.class);
		Options opts = (Options) optParser.parse(args, true);
		
		ProductParserTrainer.unknownLevel = opts.unknownLevel;
		List<Tree<String>> corpus = BerkeleyParserWrapper.readCorpus(opts.path);
		ProductParserTrainer.numSplits    = opts.numSplits;
		ProductParserData origGrammars = null;
		if(opts.inFile != null)
			origGrammars = ProductParserData.loadModel(opts.inFile);
		ProductParserData ppd  = train(corpus, opts.nParser, origGrammars);
		ppd.saveModel(opts.outFileName);
	}
}
