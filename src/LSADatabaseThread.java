import java.util.ArrayList;
import java.util.Hashtable;

/**
 * deal with LSA messages.
 * build LSA database
 * build the topology
 */
public class LSADatabaseThread implements Runnable {
    private Packet recv;

    public LSADatabaseThread (Packet p){
        this.recv = p;
    }
    public void run(){
        LSAMessage cur = recv.getLSA();
        long now = System.currentTimeMillis();

        long check_age = now-cur.getTime_created();
        if(check_age > Config.AGE_LIMITATION){
            //if age is larger than the limitation, too old, ignore
            System.out.println("Printing inside the LSADatabaseThread, the message is too old");
            //The package should not be passed out.
        }else{
            //forwarding to neighbors.
            for(int direct_neigh: Config.Established_Connect.keySet()){
                if(direct_neigh != recv.getId()) {
                    Packet lsapack = new Packet(Config.ROUTER_ID, "LSA_MESSAGE", Config.Neighbors_table.get(direct_neigh).Dest, cur);
                    lsapack.setLSAMessage(cur);
                    sLSRP.sendPacket(lsapack);
                }
            }
            //todo the sequence number would be the other condition to judge whether the message is too old
            int id = Integer.parseInt(cur.getLinkID());
            LSADatabase workdb = sLSRP.lsadb.get(id);
            if(workdb != null){
                if(workdb.seqno < cur.getSeqno()){
                    //update lsa database
                    workdb.seqno = cur.getSeqno();
                    sLSRP.lsadb.put(id, workdb);

                    //update linkstates
                    UpdateLinks(cur);
                }
            }else{
                // no entry in lsa database for this router, add new one
                LSADatabase newentry = new LSADatabase();
                newentry.fromLSAMessage(cur);
                sLSRP.lsadb.put(Integer.parseInt(cur.getLinkID()),newentry);

                // update linkstates
                UpdateLinks(cur);
            }
        }
    }

    private int CountNodes (){
        for(String j: sLSRP.links.keySet()){
            String [] records = j.split("_");
            System.out.println(j);
            sLSRP.router_nodes.put(Integer.parseInt(records[0]), 0);
            sLSRP.router_nodes.put(Integer.parseInt(records[1]),0);
        }
        sLSRP.router_nodes.put(Config.ROUTER_ID,0);

        return sLSRP.router_nodes.size();
    }

    private void SetupGraph (int nodecount) {
        WeightedGraph t = new WeightedGraph (nodecount);

        int vertex_index = 0;
        for (int p: sLSRP.router_nodes.keySet()){
            String router = "Router_" + p;
            t.setLabel(vertex_index, router);
            sLSRP.router_nodes.put(p,vertex_index);
            vertex_index++;
        }


        for (String j: sLSRP.links.keySet()){
            Links worklink = sLSRP.links.get(j);
            System.out.println("Adding Edge: "+ (worklink.source-1)+" -- "+ (worklink.destination-1)+": "+worklink.cost);
            int start = sLSRP.router_nodes.get(worklink.source);
            int end = sLSRP.router_nodes.get(worklink.destination);
            t.addEdge(start, end, worklink.cost);
            t.addEdge(end, start,worklink.cost);
        }

        t.print();

        int source_id = sLSRP.router_nodes.get(Config.ROUTER_ID);
        final int [] pred = Dijkstra.dijkstra (t, source_id);
        for (int n=0; n<nodecount; n++) {
            Dijkstra.printPath (t, pred, source_id, n);
        }

    }

    private void UpdateLinks(LSAMessage cur){
        boolean update_flag = false;
        ArrayList<Links> updated = cur.getLinkArray();
        for(int i=0; i<updated.size(); i++){
            String tmp_key = updated.get(i).source + "_" + updated.get(i).destination;
            Links orign = sLSRP.links.get(tmp_key);
            // no matter it existed or not, will update the links
            if(orign != null){
                if(updated.get(i).cost == Double.POSITIVE_INFINITY){
                    // The link is down, should be removed from established links
                    sLSRP.links.remove(tmp_key);
                    update_flag = true;
                }else if (updated.get(i).cost != orign.cost){
                    //if the cost does not change, links does not need to be updated
                    sLSRP.links.put(tmp_key, updated.get(i));
                    update_flag = true;
                }
            }else{
                sLSRP.links.put(tmp_key, updated.get(i));
                update_flag = true;
            }

        }

        if(update_flag){
            //recalculate routing table
            //if no updates, the routing table does not need to be recalculated
            int nodecount = CountNodes();
            System.out.println("Setting up the Graph ... with "+nodecount+" nodes");
            SetupGraph(nodecount);
        }
    }
}
