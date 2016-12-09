package haskell.complex.reduction;

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
     * @param graph the adjacency matrix
     * @return the transitive closure
     */
    public static int[][] getTransitiveClosure(int[][] graph) {
        int V = graph.length;
	    /* reach[][] will be the output matrix that will finally
	       have the shortest  distances between every pair of
           vertices */
        int reach[][] = new int[V][V];
        int  i, j, k;

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
        for (k = 0; k < V; k++)
        {
            // Pick all vertices as source one by one
            for (i = 0; i < V; i++)
            {
                // Pick all vertices as destination for the
                // above picked source
                for (j = 0; j < V; j++)
                {
                    // If vertex k is on a path from i to j,
                    // then make sure that the value of reach[i][j] is 1
                    reach[i][j] = (reach[i][j]!=0) || ((reach[i][k]!=0) && (reach[k][j]!=0))?1:0;
                }
            }
        }

        return reach;
    }

    /**
     * Returns a list of maximal cliques (represented as a set of indices).
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
     * @param node the node
     * @param clique the clique
     * @param graph the graph
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
     * @param cliques
     * @param graph
     */
    public static void topologicallySortCliques(List<Set<Integer>> cliques, int[][] graph) {
        int n = graph.length;

        // TODO: doesn't work
        // create a new graph which stores the row-index together with the graph
        // because that information gets lost when we later contract the graph
        /*List<List<Pair<Integer, Pair<Integer, Integer>>>> contractedGraph = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            List<Pair<Integer, Pair<Integer, Integer>>> row = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                Pair<Integer, Integer> index = new Pair<>(i, j);
                Pair<Integer, Pair<Integer, Integer>> entryIndex = new Pair<>(graph[i][j], index);
                row.add(entryIndex);
            }
            contractedGraph.add(row);
        }

        // then we contract cliques one by one
        for (Set<Integer> clique : cliques) {
            // we remove the row and column of every element except for the first one
            boolean firstElement = true;
            for (int node : clique) {
                if (firstElement) {
                    firstElement = false;
                }
                else {
                    // remove row and column belonging to this index

                    // remove row
                    Iterator<List<Pair<Integer, Pair<Integer, Integer>>>> rowIt = contractedGraph.iterator();
                    while (rowIt.hasNext()) {
                        List<Pair<Integer, Pair<Integer, Integer>>> row = rowIt.next();

                        if (row.get(0).snd.fst == node) {
                            rowIt.remove();
                            break;
                        }
                        else {
                            // remove column
                            Iterator<Pair<Integer, Pair<Integer, Integer>>> colIt = row.iterator();
                            while (colIt.hasNext()) {
                                Pair<Integer, Pair<Integer, Integer>> col = colIt.next();

                                if (col.snd.snd == node) {
                                    colIt.remove();
                                }
                            }
                        }
                    }
                }
            }

            System.out.println("\nGraph after contracting clique:");
            n = contractedGraph.size();
            System.out.print("\t");
            for (int i = 0; i < n; i++) {
                System.out.print(contractedGraph.get(0).get(i).snd.snd+" ");
            }
            System.out.println();
            for (int i = 0; i < n; i++) {
                System.out.print("#"+contractedGraph.get(i).get(0).snd.fst+"\t");
                for (int j = 0; j < n; j++) {
                    System.out.print(contractedGraph.get(i).get(j).fst + " ");
                }
                System.out.println();
            }
        }*/
    }
}