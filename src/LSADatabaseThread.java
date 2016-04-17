import javax.annotation.processing.SupportedSourceVersion;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.SynchronousQueue;

/**
 * deal with LSA messages.
 * build LSA database
 * build the topology
 */
public class LSADatabaseThread implements Runnable {
    private Packet recv;
    private boolean printmap = false;

    public LSADatabaseThread (Packet p){
        this.recv = p;
    }

    public void run(){
        LSAMessage cur = recv.getLSA();
//        System.out.println("from received packet "+ cur.getLinkID() + " has "+cur.getLinkCount()+ " link counts");
//        int initialid = cur.getAdvertising_Id();
        long now = System.currentTimeMillis();

        long check_age = now-cur.getTime_created();

        //every time receive the LSA message, also remove the information that older than age limitation
//        if(sLSRP.lsadb.size()>0) {
//            checkLSAdb(now);
//        }
        if(check_age > Config.AGE_LIMITATION){
            //if age is larger than the limitation, too old, ignore
            System.out.println("Printing inside the LSADatabaseThread, the message is too old");
            //ignore the packet.
        }else{
            int id = Integer.parseInt(cur.getLinkID());
            LSADatabase workdb = sLSRP.lsadb.get(id);
            String lsa_type = recv.getType();
            if(workdb != null) {
                // todo may use the formula given in class to check which one is newer
                if (cur.getSeqno() > workdb.seqno) {
                    for(int direct_neigh: Config.Neighbors_table.keySet()){
                        if(!Config.Established_Connect.containsKey(direct_neigh)){
                            continue;
                        }

                        // don't send to source and initial router
                        if((direct_neigh != recv.getId())&&(direct_neigh != id) ) {
                            Packet lsapack = new Packet(Config.ROUTER_ID, lsa_type, Config.Neighbors_table.get(direct_neigh).Dest, cur);
                            lsapack.setLSAMessage(cur);
                            sLSRP.sendPacket(lsapack);
                        }
                    }

                    //update lsa database
                    workdb.fromLSAMessage(cur);
//                    System.out.println("Router Id "+ workdb.linkid + " has "+workdb.linkcounts+" link counts");
                    sLSRP.lsadb.put(id, workdb);

                    //update linkstates
                    UpdateLinks(cur);


                }else{
                    // ignore the message
                }
            }else{
                // no entry in lsa database for this router, add new one
                LSADatabase newentry = new LSADatabase();
                newentry.fromLSAMessage(cur);
                sLSRP.lsadb.put(Integer.parseInt(cur.getLinkID()), newentry);

                for(int direct_neigh: Config.Established_Connect.keySet()){
                    // don't send to source and initial router
                    if((direct_neigh != recv.getId())&&(direct_neigh != id) ) {
                        Packet lsapack = new Packet(Config.ROUTER_ID, lsa_type, Config.Neighbors_table.get(direct_neigh).Dest, cur);
                        lsapack.setLSAMessage(cur);
                        sLSRP.sendPacket(lsapack);
                    }
                }

                // update linkstates
                UpdateLinks(cur);
            }
        }
    }

    private int CountNodes (){
        for(String j: sLSRP.links.keySet()){
            String [] records = j.split("_");
//            System.out.println(j);
            sLSRP.router_nodes.put(Integer.parseInt(records[0]), 0);
            sLSRP.router_nodes.put(Integer.parseInt(records[1]), 0);
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
//            System.out.println("Adding Edge: "+ (worklink.source-1)+" -- "+ (worklink.destination-1)+": "+worklink.cost);
            int start = sLSRP.router_nodes.get(worklink.source);
            int end = sLSRP.router_nodes.get(worklink.destination);
            t.addEdge(start, end, worklink.cost);
//            t.addEdge(end, start,worklink.cost);
        }

//        System.out.println("edge no: "+t.getEdgeCounts()+" should be "+sLSRP.edgeno+" whether converge "+sLSRP.converge);
        if((t.getEdgeCounts() == sLSRP.edgeno && sLSRP.converge == false)||(printmap)){
            System.out.println("Converge");
            t.print();
            sLSRP.converge = true;
            long time = System.currentTimeMillis() - sLSRP.starttime;
            System.out.println("*** in "+time+" million seconds ***");
        }
        synchronized (sLSRP.graph){
            sLSRP.graph = t;
        }
//        t.print();

        int source_id = sLSRP.router_nodes.get(Config.ROUTER_ID);
        final int [] pred = Dijkstra.dijkstra (t, source_id);
        for (int n=0; n<nodecount; n++) {
            Dijkstra.printPath (t, pred, source_id, n);
        }

    }

    private void UpdateLinks(LSAMessage cur){
        String lsatype = cur.getType();
        boolean update_flag = false;
        if(lsatype.equals("FAIL_LSA")){
            String faillink = cur.getFaillink();
            String tmp[] = faillink.split("_");
            String rev_link = tmp[1]+"_"+tmp[0];
            synchronized (sLSRP.links){
                if(sLSRP.links.containsKey(faillink)){
                    System.out.println("remove the fail link "+ faillink);
                    sLSRP.links.remove(faillink);
                    System.out.println("remove the reverse fail link "+ rev_link);
                    sLSRP.links.remove(rev_link);
                    update_flag = true;
                }
            }

            printmap = true;


        }else if(lsatype.equals("LSA")){
            ArrayList<Links> updated = cur.getLinkArray();
            for(int i=0; i<updated.size(); i++){
                String tmp_key = updated.get(i).source + "_" + updated.get(i).destination;
                Links orign = sLSRP.links.get(tmp_key);
                // no matter it existed or not, will update the links
                if(orign != null){
                    if(orign.cost != updated.get(i).cost){
                        synchronized (sLSRP.links){
                            sLSRP.links.put(tmp_key, updated.get(i));
                        }
                        update_flag = true;
                    }
                }else {
                    synchronized (sLSRP.links) {
                        sLSRP.links.put(tmp_key, updated.get(i));
                    }
                    update_flag = true;
                }
            }

        }
        if(update_flag){
            //recalculate routing table
            //if no updates, the routing table does not need to be recalculated
            int nodecount = CountNodes();
//            System.out.println("Setting up the Graph ... with "+nodecount+" nodes");
            SetupGraph(nodecount);
        }
    }

    public void checkLSAdb(long now){
        ArrayList<Integer> outdated = new ArrayList<>();
        for (int i = 0 ; i < sLSRP.lsadb.size(); i++){
            long age =  now - sLSRP.lsadb.get(i).createdtime;
            if(age > Config.AGE_LIMITATION){
                outdated.add(i);
            }
        }

        synchronized (sLSRP.lsadb){
            for(int j = 0; j<outdated.size(); j++) {
                sLSRP.lsadb.remove(j);
            }
        }
    }

}
