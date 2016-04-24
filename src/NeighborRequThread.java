import java.util.ArrayList;
import java.util.IdentityHashMap;

/**
 * Send out Neighbor Request packet, build up connections.
 */
public class NeighborRequThread implements Runnable {

    private int seqno = 0;
    public static boolean sendflag = true;

    @Override
    public void run (){
//        System.out.println("Inside the NEighnor Requ");
//        System.out.println(sendflag + "\t\t "+sLSRP.Failure);
//        while(sendflag && !sLSRP.Failure) {
        while (!sLSRP.Failure){
            for (int IDs : Config.Neighbors_table.keySet()) {
                if(sLSRP.Established_Connect.containsKey(IDs)){
//                    System.out.println("Neighbor "+ IDs + " in Established Connect");
                    continue;
                }

//                System.out.println(IDs + ": " + Config.Neighbors_table.get(IDs).Dest + " " + Config.Neighbors_table.get(IDs).Port);
                Packet neighbor_request = new Packet(Config.ROUTER_ID, "NEIGHBOR_REQUEST", Config.Neighbors_table.get(IDs).Dest,seqno);
                seqno++;
                sLSRP.sendPacket(neighbor_request);
            }

//            System.out.println("Two size is the same? **** ");
//            System.out.println(Config.Established_Connect.size() + "\t" + Config.Neighbors_table.size());

//            System.out.println("waiting for connecting with neighbors");
            try{
                Thread.sleep(10000);
            }catch (InterruptedException e){
                Thread.currentThread();
            }
        }
//        System.out.println("Neighbor Request Thread is Finished");
    }
}