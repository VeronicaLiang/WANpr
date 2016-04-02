import java.util.Map;
import java.util.Map.Entry;
/**
 * Created by Xiaoyu on 3/15/2016.
 */
public class LSAThread implements Runnable{
    long lastHelloTime = 0;
    @Override
    public void run (){
        try {
            while (true) {
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
}
