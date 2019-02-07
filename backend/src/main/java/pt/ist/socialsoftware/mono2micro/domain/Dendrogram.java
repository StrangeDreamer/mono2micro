package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;

public class Dendrogram {
	private ArrayList<Node> nodes = new ArrayList<>();
	private ArrayList<Edge> edges = new ArrayList<>();
	private ArrayList<Graph> graphs = new ArrayList<>();

    private static Dendrogram instance = null; 

	public Dendrogram() {
	}

	public static Dendrogram getInstance() { 
        if (instance == null) 
            instance = new Dendrogram(); 

        return instance; 
	}
	
	public void destroy() {
		instance = null;
	}

	public ArrayList<Node> getNodes() {
		return this.nodes;
	}

	public void addNode(Node node) {
		this.nodes.add(node);
	}

	public ArrayList<Edge> getEdges() {
		return this.edges;
	}

	public void addEdge(Edge edge) {
		this.edges.add(edge);
	}

	public ArrayList<Graph> getGraphs() {
		return this.graphs;
	}

	public void addGraph(Graph graph) {
		this.graphs.add(graph);
	}

	public void mergeClusters(String graphName, String cluster1, String cluster2) {
		for (int i = 0; i < graphs.size(); i++) {
			if (graphs.get(i).getName().equals(graphName)) {
				graphs.get(i).mergeClusters(cluster1, cluster2);
			}
		}
	}

}
