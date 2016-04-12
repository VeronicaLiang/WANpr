import java.util.ArrayList;

/**
 * Deal with Neighbor Request Packet.
 */
public class NeighborRequACKThread implements Runnable {

    private Packet task;
    public NeighborRequACKThread (Packet p){
        this.task = p;
    }

    public void run() {
        while(true){
            Packet recv = this.task;
            int request_id = recv.getId();
//            System.out.println(request_id);
            Neighbors check = Config.Neighbors_table.get(request_id);
            if(check != null){
//                System.out.println("Accept");
//                System.out.println(check.Dest + " ^^^^^^^^^^^^^ ");
                Packet acc = new Packet(Config.ROUTER_ID,"ACK_NEIGH", check.Dest);
                sLSRP.sendPacket(acc);
            }else{
                System.out.println("Reject");
                String destination_host = Config.Id_Host.get(request_id);
                Packet rej = new Packet(Config.ROUTER_ID,"NACK_NEIGH", destination_host);
                sLSRP.sendPacket(rej);
            }
        }
    }
}
