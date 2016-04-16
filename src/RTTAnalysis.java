import java.io.*;
import java.net.*;
import java.util.Hashtable;

/**
 * Analyze Round Trip Time, and update the links cost, and Routing table information.
 */
//todo timer
//todo round-of-robbin fashion
public class RTTAnalysis implements Runnable{

    public static Hashtable<Integer, PacketHistory> senthistory = new Hashtable<>();
    private int seqno = 0;

    @Override
    public void run(){
        while (!sLSRP.Failure) {
            if(sLSRP.Failure){
                // under failure state, doing nothing
                continue;
            }

            for (int dir_neighbor: Config.Neighbors_table.keySet()){
                if(!Config.Established_Connect.containsKey(dir_neighbor)){
                    continue;
                }
                boolean resendflag = false;
                int cur_size = senthistory.size();
                if(cur_size>0){
//                    printSentHistory();
                    resendflag = checkHistory();
                }
                if(!resendflag){
//                    System.out.println("Sending RTT Analysis Message...");
//                    System.out.println("Sequence no is: " + seqno);
                    Packet rtt = new Packet(Config.ROUTER_ID, "RTT_ANALYSIS",Config.Neighbors_table.get(dir_neighbor).Dest, seqno);
//                    System.out.println(rtt.getSeqno());
                    sLSRP.sendPacket(rtt);
                    synchronized (senthistory) {
                        senthistory.put(seqno, new PacketHistory(rtt, dir_neighbor));
                    }
                    seqno++;
                }

                // every time only send to one neighbor
                try {
                    Thread.sleep(Config.UPDATE_INTERVAL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public boolean checkHistory(){
        boolean resend_flag = false;
        for(int seq_key: senthistory.keySet()){
            PacketHistory check = senthistory.get(seq_key);
            String linkkey = Config.ROUTER_ID + "_" + check.getDest_id();
//            String re_linkkey = check.getDest_id() + "_" + Config.ROUTER_ID;
            if(check.getAck()){
                //remove ones that have acked.
//                System.out.println("remove ACKED element");
                synchronized (senthistory){
                    senthistory.remove(seq_key);
                }

            }else{
                synchronized (sLSRP.links) {
                    if (sLSRP.links.containsKey(linkkey)) {
                        sLSRP.links.get(linkkey).cost = Integer.MAX_VALUE;
                    }
                }
                Packet resendp = check.getPacket();
//                System.out.println("Resend the packet "+this.seqno);
                resendp.setSeqno(this.seqno);
                sLSRP.sendPacket(resendp);
                PacketHistory resendrecord = new PacketHistory(resendp,check.getDest_id());
                synchronized (senthistory) {
                    senthistory.put(this.seqno, resendrecord);
                }
                this.seqno++;
                resend_flag = true;
                synchronized (senthistory){
                    senthistory.remove(seq_key);
                }
            }

        }
        return resend_flag;

    }

    public void printSentHistory(){
        System.out.println("seq_no\tACK\tCounts\tDest_Router_ID");
        for(int seq_no_key: senthistory.keySet()){
            System.out.println(seq_no_key+"\t"+senthistory.get(seq_no_key).getAck()+"\t"+senthistory.get(seq_no_key).getCounts()+"\t"+senthistory.get(seq_no_key).getDest_id());
        }
    }
}
