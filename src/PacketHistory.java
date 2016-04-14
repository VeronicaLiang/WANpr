/**
 * For each thread, the history packets object.
 */
public class PacketHistory {
    private boolean ack;
    private Packet message;
    private int counts;
    private int dest_id;
    private long sendtime;

    public PacketHistory(Packet p, int dest){
        this.message = p;
        this.ack = false;
        this.counts = 0;
        this.dest_id = dest;
        this.sendtime = System.currentTimeMillis();
    }

    public Packet getPacket (){
        return this.message;
    }

    public boolean getAck(){
        return this.ack;
    }

    public void setAck(boolean val){
        this.ack = val;
    }

    public void increasCounts(){
        this.counts++;
    }

    public int getCounts(){
        return this.counts;
    }

    public int getDest_id(){
        return this.dest_id;
    }

    public long getSendtime(){
        return this.sendtime;
    }

    public void setSendtime(long t){
        this.sendtime = t;
    }
}
