/**
 * Created by Xiaoyu on 4/2/2016.
 */
/**
 * Alive message thread, sending out alive message periodically
 */
public class AliveMessageThread implements Runnable {

    public void run(){
        for(int IDs:Config.Neighbors_table.keySet()){
            Packet neighbor_request = new Packet(Config.ROUTER_ID,"ALIVE_MESSAGE",Config.Neighbors_table.get(IDs).Dest);
            sLSRP.sendPacket(neighbor_request);
        }
        try {
            Thread.sleep(Config.HELLO_INTERVAL);
        }catch (InterruptedException e ){
            Thread.currentThread().interrupt();
        }
    }
}
