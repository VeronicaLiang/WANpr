import java.util.Map;
import java.util.Map.Entry;
/**
 * Created by Xiaoyu on 3/15/2016.
 */
public class LSAThread implements Runnable{
    long lastHelloTime = 0;
    private int seq_no = 0;
    @Override
    public void run (){
        try {
            while (true) {
                System.out.println("Sending LSA Messages");
                for(int direct_neigh: Config.Established_Connect.keySet()){
                    this.seq_no += 1;
                    LSAMessage needsend = GenerateLSA(direct_neigh);
                    Packet lsapack = new Packet(Config.ROUTER_ID,"LSA_MESSAGE",Config.Neighbors_table.get(direct_neigh).Dest,needsend);
                    sLSRP.sendPacket(lsapack);
                }
//                for (Map.Entry<Integer, Link> e : Router.links.entrySet()) {
//                    if (!e.getValue().toClient) {
//                        LinkStateAdvertisement lsAdd = Router.lsLogic.generateLSAMessage();
//                        lsAdd.age = Router.links.size();
//                        LsaMessage hm = new LsaMessage(Router.id, e.getKey());
//                        hm.data = lsAdd;
////						System.out.println("Sending LSA to " + e.getKey());
//                        e.getValue().send(hm);
////						System.out.println("Sent");
//                    }
//                }
                Thread.sleep(Config.LSA_INTERVAL);
            }
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public LSAMessage GenerateLSA(int adver_id){
        LSAMessage lsa = new LSAMessage(adver_id);
        for (String linkkey: sLSRP.links.keySet()){
            String key_set = Config.ROUTER_ID+"_";
            if(linkkey.contains(key_set)){
                lsa.AddLinks(sLSRP.links.get(linkkey));
            }
        }
        return lsa;
    }


}
