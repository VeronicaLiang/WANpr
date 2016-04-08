import java.util.ArrayList;
import java.util.Hashtable;

/**
 * deal with LSA messages.
 * build LSA database
 * build the topology
 */
public class LSADatabaseThread implements Runnable {
    private ArrayList<Packet> queue = new ArrayList<>();
    private Packet recv;

    public LSADatabaseThread (Packet p){
//        queue.add(p);
        this.recv = p;
    }
    public void run(){
        LSAMessage cur = recv.getLSA();
        long now = System.currentTimeMillis();

        long check_age = now-cur.getTime_created();
        if(check_age > Config.AGE_LIMITATION){
            //if age is larger than the limitation, too old, ignore
            System.out.println("Printing inside the LSADatabaseThread, the message is too old");
        }else{
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
                // no entry in lsa database, add new one
                LSADatabase newentry = new LSADatabase();
                newentry.fromLSAMessage(cur);
                sLSRP.lsadb.put(Integer.parseInt(cur.getLinkID()),newentry);


                // update linkstates
                UpdateLinks(cur);
            }
        }
    }

    private int CountNodes (){
        Hashtable<Integer,Integer> nodes = new Hashtable<>();
        for(String j: sLSRP.links.keySet()){
            String [] records = j.split("_");
            nodes.put(Integer.parseInt(records[0]),0);
            nodes.put(Integer.parseInt(records[1]),0);
        }

        return nodes.size();
    }

    private void SetupGraph (int nodecount) {
        WeightedGraph t = new WeightedGraph (nodecount);

        for(int i = 1; i<=nodecount; i++){
            String router = "Router_" + Integer.toString(i);
            // NOTE: vertex is indexed from 0
            t.setLabel(i-1,router);
        }

        for (String j: sLSRP.links.keySet()){
            Links worklink = sLSRP.links.get(j);
            System.out.println("Adding Edge: "+ (worklink.source-1)+" -- "+ (worklink.destination-1)+": "+worklink.cost);
            t.addEdge(worklink.source-1, worklink.destination-1,worklink.cost);
            t.addEdge(worklink.destination-1, worklink.source-1,worklink.cost);
        }

        t.print();

        final int [] pred = Dijkstra.dijkstra (t, 0);
//        for (int n=0; n<6; n++) {
//            Dijkstra.printPath (t, pred, 0, n);
//        }

    }

    private void UpdateLinks(LSAMessage cur){
        boolean update_flag = false;
        ArrayList<Links> updated = cur.getLinkArray();
        for(int i=0; i<updated.size(); i++){
            String tmp_key = updated.get(i).source + "_" + updated.get(i).destination;
            Links orign = sLSRP.links.get(tmp_key);
            // no matter it existed or not, will update the links
            if(orign != null){
                if (updated.get(i).cost != orign.cost){
                    //if the cost does not change, links does not need to be updated
                    sLSRP.links.put(tmp_key, updated.get(i));
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
            System.out.println("Setting up the Graph ... ");
            SetupGraph(nodecount);
        }
    }
}
