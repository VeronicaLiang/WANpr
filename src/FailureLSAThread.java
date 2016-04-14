import java.util.*;

/**
 * Immediately send out failure message to all neighbors.
 */
public class FailureLSAThread implements Runnable{
    private String link_key;
    private int seq_no = 0;

    public FailureLSAThread(String lkey){
        this.link_key = lkey;
    }

    @Override
    public void run() {
        try {
            for (int direct_neigh : Config.Established_Connect.keySet()) {
                this.seq_no += 1;
                LSAMessage needsend = GenerateLSA(this.link_key);
                needsend.setSeqno(this.seq_no);
                Packet lsapack = new Packet(Config.ROUTER_ID, "FAILURE_LSA", Config.Neighbors_table.get(direct_neigh).Dest, needsend);
                lsapack.setLSAMessage(needsend);
//                sLSRP.sendPacket(lsapack);
            }


        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public LSAMessage GenerateLSA(String key_set){
        LSAMessage lsa = new LSAMessage(Config.ROUTER_ID);
        int link_count = 0;
        synchronized (sLSRP.links){
            sLSRP.links.get(key_set).cost = Double.MAX_VALUE;
        }
        lsa.setLinkCount(link_count);
        return lsa;
    }
}
