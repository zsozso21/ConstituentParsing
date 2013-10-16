package constituent.berkeley.graph;

import java.util.List;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.alg.ConnectivityInspector;

public class GraphTool {
	public static List<Set<Integer>> getConnectedComponents(boolean[][] mergePairs) {
		UndirectedGraph<Integer, DefaultEdge> g =
	            new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
		for (int i = 0 ; i < mergePairs.length; i++) {
			g.addVertex(i);
		}
		for (int i = 0; i < mergePairs.length; i++) {
			for (int j = i+1; j < mergePairs.length; j++) {
				if (mergePairs[i][j]) {
					g.addEdge(i, j);
				}
			}
		}
		
		ConnectivityInspector<Integer, DefaultEdge> ci = new ConnectivityInspector<Integer, DefaultEdge>(g);
		
		return ci.connectedSets();
	}
}
