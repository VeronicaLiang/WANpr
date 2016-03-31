/**
 * Deal with Neighbor Request Packet.
 */
public class NeigborRequThread implements Runnable {
    public Packet recv;
    public NeigborRequThread (Packet p){
        this.recv = p;
    }
    public void run (){
        int request_id = this.recv.Id;
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

    }
}
