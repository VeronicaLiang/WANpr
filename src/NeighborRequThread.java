import java.util.ArrayList;

/**
 * Send out Neighbor Request packet, build up connections.
 */
public class NeighborRequThread implements Runnable {

    public void run (){
        boolean flag = true;
        while(flag) {
            for (int IDs : Config.Neighbors_table.keySet()) {
                if(Config.Established_Connect.containsKey(IDs)){
                    continue;
                }
                System.out.println(IDs + ": " + Config.Neighbors_table.get(IDs).Dest + " " + Config.Neighbors_table.get(IDs).Port);
                Packet neighbor_request = new Packet(Config.ROUTER_ID, "NEIGHBOR_REQUEST", Config.Neighbors_table.get(IDs).Dest);
                sLSRP.sendPacket(neighbor_request);
            }

//            System.out.println("Two size is the same? **** ");
//            System.out.println(Config.Established_Connect.size() + "\t" + Config.Neighbors_table.size());
            if(Config.Established_Connect.size() == Config.Neighbors_table.size()){
                flag = false;
            }else{
                System.out.println("waiting for connecting with neighbors");
                try{
                    Thread.sleep(10000);
                }catch (InterruptedException e){
                    Thread.currentThread();
                }
            }
        }
    }
}