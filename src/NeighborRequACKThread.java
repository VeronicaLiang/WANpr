import java.util.ArrayList;

/**
 * Deal with Neighbor Request Packet.
 */
public class NeighborRequACKThread implements Runnable {

    private ArrayList<Packet> queue = new ArrayList<>();
    public NeighborRequACKThread (Packet p){
        //this.recv = p;
        queue.add(p);
    }

    public void run() {
        while(!this.queue.isEmpty()){
            Packet recv = this.queue.get(0);
            int request_id = recv.getId();
            System.out.println(request_id);
            Neighbors check = Config.Neighbors_table.get(request_id);
            if(check != null){
                System.out.println("Accept");
                Packet acc = new Packet(Config.ROUTER_ID,"ACK_NEIGH", check.Dest);
                sLSRP.sendPacket(acc);
            }else{
                System.out.println("Reject");
                Packet rej = new Packet(Config.ROUTER_ID,"NACK_NEIGH", check.Dest);
                sLSRP.sendPacket(rej);
            }
            this.queue.remove(0);
        }
    }
}
