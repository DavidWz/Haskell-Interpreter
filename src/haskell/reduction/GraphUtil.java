package haskell.reduction;

import java.util.*;

/**
 * A class which offers a few graph algorithms.
 */
public class GraphUtil {
    public static class Pair<A, B> {
        public A fst;
        public B snd;

        public Pair(A fst, B snd) {
            this.fst = fst;
            this.snd = snd;
        }
    }

    /**
     * Calculates the transitive closure using the Floyd Warshall algorithm.
     * Taken from http://www.geeksforgeeks.org/transitive-closure-of-a-graph/
     * This code is contributed by Aakash Hasija
     *
     * @param graph the adjacency matrix
     * @return the transitive closure
     */
    public static int[][] getTransitiveClosure(int[][] graph) {
        int V = graph.length;
        /* reach[][] will be the output matrix that will finally
           have the shortest  distances between every pair of
           vertices */
        int reach[][] = new int[V][V];
        int i, j, k;

	    /* Initialize the solution matrix same as input graph
	       matrix. Or we can say the initial values of shortest
	       distances are based  on shortest paths considering
	       no intermediate vertex. */
        for (i = 0; i < V; i++) {
            for (j = 0; j < V; j++) {
                reach[i][j] = graph[i][j];
            }
        }

	    /* Add all vertices one by one to the set of intermediate
	       vertices.
	      ---> Before start of a iteration, we have reachability
	           values for all  pairs of vertices such that the
               reachability values consider only the vertices in
               set {0, 1, 2, .. k-1} as intermediate vertices.
	      ----> After the end of a iteration, vertex no. k is
	            added to the set of intermediate vertices and the
	            set becomes {0, 1, 2, .. k} */
        for (k = 0; k < V; k++) {
            // Pick all vertices as source one by one
            for (i = 0; i < V; i++) {
                // Pick all vertices as destination for the
                // above picked source
                for (j = 0; j < V; j++) {
                    // If vertex k is on a path from i to j,
                    // then make sure that the value of reach[i][j] is 1
                    reach[i][j] = (reach[i][j] != 0) || ((reach[i][k] != 0) && (reach[k][j] != 0)) ? 1 : 0;
                }
            }
        }

        return reach;
    }

    /**
     * Returns a list of maximal cliques (represented as a set of indices).
     *
     * @param closure the transitive closure of a graph
     * @return
     */
    public static List<Set<Integer>> getMaximalCliques(int[][] closure) {
        int n = closure.length;
        List<Set<Integer>> cliques = new ArrayList<>();

        // we store which nodes we have not yet put into a clique
        Stack<Integer> notFoundYet = new Stack<>();
        for (int i = 0; i < n; i++) {
            notFoundYet.add(i);
        }

        while (!notFoundYet.isEmpty()) {
            int currentNode = notFoundYet.pop();

            // check if this node belongs to a previous clique
            boolean foundClique = false;
            for (Set<Integer> clique : cliques) {
                if (belongsToClique(currentNode, clique, closure)) {
                    clique.add(currentNode);
                    foundClique = true;
                }
            }

            if (!foundClique) {
                // we create a new clique for this node
                Set<Integer> newClique = new HashSet<>();
                newClique.add(currentNode);
                cliques.add(newClique);
            }
        }

        return cliques;
    }

    /**
     * Checks if the given node belongs to the given clique in the given graph.
     *
     * @param node   the node
     * @param clique the clique
     * @param graph  the graph
     * @return
     */
    private static boolean belongsToClique(int node, Set<Integer> clique, int[][] graph) {
        // if the node belongs to the clique, it must be mutually connected with every node there
        for (int cliqueNode : clique) {
            if (graph[node][cliqueNode] == 1 && graph[cliqueNode][node] == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Topologically sorts the given cliques from the given graph.
     *
     * @param cliques
     * @param graph
     * @return the sorted cliques
     */
    public static List<Set<Integer>> topologicallySortCliques(List<Set<Integer>> cliques, int[][] graph) {
        /*
         * I am so sorry.
         */
        int n = graph.length;

        // create a new graph which stores the node index together with the graph
        // because that information gets lost when we later contract the graph
        List<List<Pair<Integer, Integer>>> contractedGraph = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            List<Pair<Integer, Integer>> row = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                Pair<Integer, Integer> entryIndex = new Pair<>(graph[i][j], i);
                row.add(entryIndex);
            }
            contractedGraph.add(row);
        }

        // then we need to contract cliques to single nodes

        // to this end, we store which nodes will get deleted (all but one from each group)
        List<Integer> toRemove = new ArrayList<>();
        for (Set<Integer> clique : cliques) {
            boolean firstElement = true;
            for (int node : clique) {
                if (firstElement) {
                    firstElement = false;
                } else {
                    toRemove.add(node);
                }
            }
        }

        // now actually remove the rows and columns

        // remove rows
        Iterator<List<Pair<Integer, Integer>>> rowIt = contractedGraph.iterator();
        int currentRow = 0;
        while (rowIt.hasNext()) {
            List<Pair<Integer, Integer>> row = rowIt.next();

            if (toRemove.contains(currentRow)) {
                rowIt.remove();
            }

            currentRow++;
        }

        // remove columns
        rowIt = contractedGraph.iterator();
        while (rowIt.hasNext()) {
            List<Pair<Integer, Integer>> row = rowIt.next();
            Iterator<Pair<Integer, Integer>> colIt = row.iterator();

            int currentCol = 0;
            while (colIt.hasNext()) {
                Pair<Integer, Integer> col = colIt.next();

                if (toRemove.contains(currentCol)) {
                    colIt.remove();
                }

                currentCol++;
            }
        }

        // convert the graph back to int array array
        Map<Integer, Integer> indexToNode = new HashMap<>();
        n = contractedGraph.size();
        int[][] contractedAdjacency = new int[n][n];

        int rowId = 0;
        for (List<Pair<Integer, Integer>> row : contractedGraph) {
            int colId = 0;
            int node = row.get(colId).snd;
            indexToNode.put(rowId, node);

            for (Pair<Integer, Integer> col : row) {
                int value = col.fst;
                if (rowId == colId) {
                    // no self-loops
                    contractedAdjacency[rowId][colId] = 0;
                }
                else {
                    contractedAdjacency[rowId][colId] = value;
                }
                colId++;
            }
            rowId++;
        }

        // next, we topologically sort this new contracted graph

        // determine all nodes with no incoming dependencies
        ArrayList<Integer> S = new ArrayList<>();
        for (int node = 0; node < n; node++) {
            boolean hasIncoming = false;
            for (int row = 0; row < n; row++) {
                if (contractedAdjacency[row][node] == 1) {
                    hasIncoming = true;
                    break;
                }
            }
            if (!hasIncoming) {
                S.add(node);
            }
        }

        // perform the topological sort
        ArrayList<Integer> topologicalOrder = topologicalSort(contractedAdjacency, S);

        // reorder the cliques
        List<Set<Integer>> sortedCliques = new ArrayList<>();
        for (int i = 0; i < topologicalOrder.size(); i++) {
            int node = indexToNode.get(topologicalOrder.get(i));

            // find the clique to which this node belongs and add it to the new sorted cliques
            for (Set<Integer> currentClique : cliques) {
                if (currentClique.contains(node)) {
                    sortedCliques.add(currentClique);
                    break;
                }
            }
        }

        return sortedCliques;
    }

    /**
     * Performs the topological sort on a given graph from a given source.
     * @param adjacencyMatrix
     * @param S a set of all nodes with no incomming edges
     * @return
     */
    public static ArrayList<Integer> topologicalSort(int adjacencyMatrix[][], ArrayList<Integer> S) {
        ArrayList<Integer> L = new ArrayList<>();

        while (!S.isEmpty()) {
            int n = S.remove(0);
            L.add(n);

            // for each node m with edge from n to m do
            for (int m = 0; m < adjacencyMatrix.length; m++) {
                if (adjacencyMatrix[n][m] == 1) {
                    // remove that edge from the graph
                    adjacencyMatrix[n][m] = 0;

                    // if m has no other incoming edges, add it to S
                    boolean hasIncoming = false;
                    for (int in = 0; in < adjacencyMatrix.length; in++) {
                        if (adjacencyMatrix[in][m] == 1) {
                            hasIncoming = true;
                            break;
                        }
                    }

                    if (!hasIncoming) {
                        S.add(m);
                    }
                }
            }
        }

        return L;
    }
}