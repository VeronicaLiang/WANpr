import java.util.Map;
import java.util.Map.Entry;
/**
 * Periodically generate and send out LSA Messages to neighbors
 */
public class LSAThread implements Runnable{
    private int seq_no = 0;

    public void run (){
        try {
            while (!sLSRP.Failure) {
                System.out.println("Sending LSA Messages");
                for(int direct_neigh: Config.Established_Connect.keySet()){
                    this.seq_no += 1;
                    LSAMessage needsend = GenerateLSA(Config.ROUTER_ID);
                    needsend.setSeqno(this.seq_no);
                    Packet lsapack = new Packet(Config.ROUTER_ID,"LSA_MESSAGE",Config.Neighbors_table.get(direct_neigh).Dest,needsend);
                    lsapack.setLSAMessage(needsend);
                    sLSRP.sendPacket(lsapack);
                }
                //todo  Round of Robbin Fashion.
                Thread.sleep(Config.LSA_INTERVAL);
            }
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public LSAMessage GenerateLSA(int adver_id){
        LSAMessage lsa = new LSAMessage(adver_id);
        String key_set = Config.ROUTER_ID+"_";
        int link_count = 0;
        for (String linkkey: sLSRP.links.keySet()){
            if(linkkey.contains(key_set)){
                lsa.AddLinks(sLSRP.links.get(linkkey));
                link_count +=1;
            }
        }
        lsa.setLinkCount(link_count);
        return lsa;
    }


}
