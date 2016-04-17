import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Analyze Round Trip Time, and update the links cost, and Routing table information.
 */
//todo timer
//todo round-of-robbin fashion
public class RTTAnalysis implements Runnable{

    public static Hashtable<Integer, PacketHistory> rttsenthistory = new Hashtable<>();
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

                if(rttsenthistory.size()>0){
//                    printSentHistory();
                    checkHistory();
                }
//
//                System.out.println("Sending RTT Analysis Message...");
//                System.out.println("Sequence no is: " + seqno);
                Packet rtt = new Packet(Config.ROUTER_ID, "RTT_ANALYSIS",Config.Neighbors_table.get(dir_neighbor).Dest, seqno);
                sLSRP.sendPacket(rtt);
                synchronized (rttsenthistory) {
                    rttsenthistory.put(seqno, new PacketHistory(rtt, dir_neighbor));
                }
                seqno++;

                // every time only send to one neighbor
                try {
                    Thread.sleep(Config.UPDATE_INTERVAL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void checkHistory(){
        // Currently, if the rtt is not ACKED for three times, then the cost is set to be the inter.max
        // It won't resend any packets.
        ArrayList<Integer> remove_keys = new ArrayList<>();
        for(int seq_key: rttsenthistory.keySet()){
            PacketHistory check = rttsenthistory.get(seq_key);
            String linkkey = Config.ROUTER_ID + "_" + check.getDest_id();
//            String re_linkkey = check.getDest_id() + "_" + Config.ROUTER_ID;
            if(check.getAck()){
                //remove ones that have acked.
                remove_keys.add(seq_key);
            }else{
                if(check.getCounts() > 3){
                    if (sLSRP.links.containsKey(linkkey)) {
                        if(check.getSendtime() < sLSRP.links.get(linkkey).last_update){
                            // has been updated
                            // the packet may be lost
//                            System.out.println("Remove the send history entry "+seq_key+" without update the link cost to max");
                        }else{
                            // after faced 3 times out of timer, there was no update for the link.
                            synchronized (sLSRP.links) {
                                sLSRP.links.get(linkkey).cost = Integer.MAX_VALUE;
                            }
                        }
                    }
                    remove_keys.add(seq_key);
                }else{
                    synchronized (rttsenthistory){
                        rttsenthistory.get(seq_key).increaseCounts();
                    }
                }
            }

        }
        for(int i=0; i<remove_keys.size();i++){
            synchronized (rttsenthistory){
                rttsenthistory.remove(remove_keys.get(i));
            }
        }
    }

    public void printSentHistory(){
        System.out.println("seq_no\tACK\tCounts\tDest_Router_ID");
        for(int seq_no_key: rttsenthistory.keySet()){
            System.out.println(seq_no_key+"\t"+rttsenthistory.get(seq_no_key).getAck()+"\t"+rttsenthistory.get(seq_no_key).getCounts()+"\t"+rttsenthistory.get(seq_no_key).getDest_id());
        }
    }
}
