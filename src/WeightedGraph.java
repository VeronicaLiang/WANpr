/**
 * the weighted graph class for dijkstar algorithm
 */
public class WeightedGraph {
    private double [][]  edges;  // adjacency matrix
    private Object [] labels;

    public WeightedGraph (int n) {
        edges  = new double [n][n];
        labels = new Object[n];
    }

    public int size() { return labels.length; }

    public void   setLabel (int vertex, Object label) { labels[vertex]=label; }
    public Object getLabel (int vertex)               { return labels[vertex]; }

    public int getEdgeCounts(){
        int count = 0;
        for (int j=0; j<edges.length; j++) {
            for (int i=0; i<edges[j].length; i++) {
                if (edges[j][i]>0) {
                    count++;
                }
            }
        }
        return count/2;
    }

    public void    addEdge    (int source, int target, double w)  { edges[source][target] = w; }
    public boolean isEdge     (int source, int target)  { return edges[source][target]>0; }
    public void    removeEdge (int source, int target)  { edges[source][target] = 0; }
    public double     getWeight  (int source, int target)  { return edges[source][target]; }

    public int [] neighbors (int vertex) {
        int count = 0;
        for (int i=0; i<edges[vertex].length; i++) {
            if (edges[vertex][i]>0) count++;
        }
        final int[]answer= new int[count];
        count = 0;
        for (int i=0; i<edges[vertex].length; i++) {
            if (edges[vertex][i]>0) answer[count++]=i;
        }
        return answer;
    }

    public void print () {
        for (int j=0; j<edges.length; j++) {
            System.out.println (labels[j]+": ");
            for (int i=0; i<edges[j].length; i++) {
                if (edges[j][i]>0) System.out.println("-> "+ labels[i]+":"+edges[j][i]+" ");
            }

            System.out.println ();
        }
    }
}
