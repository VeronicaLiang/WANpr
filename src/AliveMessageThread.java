
import java.io.Serializable;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.Handler;

/**
 * Alive message thread, sending out alive message periodically
 */
public class AliveMessageThread implements Runnable {
    // record sent packets. key is the seq no, and the packethistory object contains packet, ack, and counts
    public static Hashtable<Integer, PacketHistory> senthistory = new Hashtable<>();
    private int seq_no = 0;

    @Override
    public void run(){
        while (!sLSRP.Failure) {
            for (int IDs : Config.Neighbors_table.keySet()) {
                boolean continue_flag = false;
                if(senthistory.size()>0) {
//                    printSentHistory();
                    continue_flag = checkHistory();
                }
                if(!continue_flag) {
                    if (!Config.Established_Connect.containsKey(IDs)) {
                        continue;
                    }

                    Packet neighbor_request = new Packet(Config.ROUTER_ID, "ALIVE_MESSAGE", Config.Neighbors_table.get(IDs).Dest, seq_no);
                    sLSRP.sendPacket(neighbor_request);
                    synchronized (senthistory) {
                        senthistory.put(seq_no, new PacketHistory(neighbor_request, IDs));
                    }
                    seq_no++;
                }

                try {
                    // A Round of Robbin Fashion
                    Thread.sleep(Config.HELLO_INTERVAL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private boolean checkHistory(){
        boolean resend_flag = false;
        for(int seq_key: senthistory.keySet()){
            PacketHistory check = senthistory.get(seq_key);
            String linkkey = Config.ROUTER_ID + "_" + check.getDest_id();
//            String re_linkkey = check.getDest_id() + "_" + Config.ROUTER_ID;
            if(check.getAck()){
                //remove ones that have acked.
//                System.out.println("remove ACKED element");
                senthistory.remove(seq_key);
            }else{
                if(check.getCounts()>=3){
                    // update links
                    synchronized (sLSRP.links){
                        if(sLSRP.links.containsKey(linkkey)){
                            sLSRP.links.get(linkkey).cost = Double.MAX_VALUE;
                            sLSRP.links.get(linkkey).active = false;
                        }
//                        if(sLSRP.links.containsKey(re_linkkey)){
//                            sLSRP.links.get(re_linkkey).cost = Double.MAX_VALUE;
//                            sLSRP.links.get(re_linkkey).active = false;
//                        }
                    }
                    // send out LAS failure message
//                    Runnable lsaf = new FailureLSAThread(linkkey);
//                    new Thread(lsaf).start();
                    senthistory.remove(seq_key);
                }else{
                    senthistory.get(seq_key).increasCounts();
                    sLSRP.sendPacket(check.getPacket());
                    resend_flag = true;
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
