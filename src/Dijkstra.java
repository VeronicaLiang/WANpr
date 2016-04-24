/**
 * Dijkstra algorithm adopted from web.
 */
import java.util.ArrayList;

public class Dijkstra {
    // Dijkstra's algorithm to find shortest path from s to all other nodes
    public static double [] dist;
    public static int [] dijkstra (WeightedGraph G, int s) {
//        final double [] dist = new double [G.size()];  // shortest known distance from "s"
        dist = new double[G.size()];
        final int [] pred = new int [G.size()];  // preceeding node in path
        final boolean [] visited = new boolean [G.size()]; // all false initially

        for (int i=0; i<dist.length; i++) {
            dist[i] = Double.MAX_VALUE;
        }

        dist[s] = 0;
        for (int i=0; i<dist.length; i++) {
            final int next = minVertex (dist, visited);
            if(next == -1){
                // graph not connected
                continue;
            }
//            System.out.println("this is the next vertex ####### "+next);
            visited[next] = true;
            // The shortest path to next is dist[next] and via pred[next].
            final int [] n = G.neighbors (next);
            for (int j=0; j<n.length; j++) {
                final int v = n[j];
                final double d = dist[next] + G.getWeight(next,v);
                if (dist[v] > d) {
                    dist[v] = d;
                    pred[v] = next;
                }
            }
        }
        return pred;  // (ignore pred[s]==0!)
    }

    private static int minVertex (double [] dist, boolean [] v) {
        double x = Double.MAX_VALUE;
        int y = -1;   // graph not connected, or no unvisited vertices
        for (int i=0; i<dist.length; i++) {
            if (!v[i] && dist[i]<x) {y=i; x=dist[i];}
        }
        return y;
    }

    public static void printPath (WeightedGraph G, int [] pred, int s, int e) {
        final java.util.ArrayList path = new java.util.ArrayList();
//        final ArrayList path = new ArrayList<>();

        if(dist[e] == Double.MAX_VALUE){
            String dest_router_name = G.getLabel(e).toString();
            String [] reords = dest_router_name.split("_");
            int dest_rout_id = Integer.parseInt(reords[1]);

//            System.out.println("there is no rout from "+G.getLabel(s)+" to " + G.getLabel(e));
            if(sLSRP.routing_table.containsKey(dest_rout_id)){
                synchronized (sLSRP.routing_table){
                    sLSRP.routing_table.remove(dest_rout_id);
                }
            }
        }else{

            int x = e;
            while(x!=s){
                path.add (0, G.getLabel(x));
                x = pred[x];
            }
            path.add(0,G.getLabel(s));

            if(sLSRP.sf_path != null) {
                synchronized (sLSRP.sf_path) {
                    sLSRP.sf_path = path;
                }
            }else{
                sLSRP.sf_path = path;
            }
//            System.out.println (path);
//        if(path.get(0).toString().equals(Config.ROUTER_ID)){
            //update the routing table
            if(path.size()>1){
                String destr = path.get(path.size()-1).toString();
                String [] tmp = destr.split("_");
                String forwardr = path.get(1).toString();
                String [] tmp2 = forwardr.split("_");
                synchronized (sLSRP.routing_table) {
                    sLSRP.routing_table.put(Integer.parseInt(tmp[1]), Integer.parseInt(tmp2[1]));
//                System.out.println("update the routing table ###################");
//                System.out.println(sLSRP.routing_table);
                }
            }

//            System.out.println("The route from "+ G.getLabel(s) +" to "+G.getLabel(e)+" is:");
//            System.out.println(path);
//            System.out.println("Distance is: "+dist[e]);

        }
//        int x = e;
//        while (x!=s) {
//            path.add (0, G.getLabel(x));
//            x = pred[x];
//        }
//        path.add (0, G.getLabel(s));
//        if(sLSRP.sf_path != null) {
//            synchronized (sLSRP.sf_path) {
//                sLSRP.sf_path = path;
//            }
//        }else{
//            sLSRP.sf_path = path;
//        }
//        System.out.println (path);
//        System.out.println();
////        if(path.get(0).toString().equals(Config.ROUTER_ID)){
//            //update the routing table
//        if(path.size()>1){
//            String destr = path.get(path.size()-1).toString();
//            String [] tmp = destr.split("_");
//            String forwardr = path.get(1).toString();
//            String [] tmp2 = forwardr.split("_");
//            synchronized (sLSRP.routing_table) {
//                sLSRP.routing_table.put(Integer.parseInt(tmp[1]), Integer.parseInt(tmp2[1]));
////                System.out.println("update the routing table ###################");
////                System.out.println(sLSRP.routing_table);
//            }
//        }


    }
}

