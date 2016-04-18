import java.util.*;

/**
 * Alive message thread, sending out alive message periodically
 */
public class AliveMessageThread implements Runnable {
    // record sent packets. key is the seq no, and the packethistory object contains packet, ack, and counts
    public static Hashtable<Integer, PacketHistory> alivesenthistory = new Hashtable<>();
    private int seq_no = 0;

    @Override
    public void run(){
        while (!sLSRP.Failure) {

            //NOTE: do not add a timer for Alive message. set the Hello_Interval small
            for (int IDs : Config.Neighbors_table.keySet()) {
//                System.out.println("Shall we send ALIVE to neighbor "+IDs+" ?");
                if (!Config.Established_Connect.containsKey(IDs)) {
//                    System.out.println("The Established_Connect does not contain the key "+ IDs);
                    continue;
                }
                boolean continue_flag = false;
                if(alivesenthistory.size()>0) {
//                    printSentHistory();
                    continue_flag = checkHistory();
                }
                if(!continue_flag) {
//                    System.out.println("There is no resend happened in checkHistory function " + continue_flag);
//                    System.out.println("Send ALIVE message to "+ IDs + " with seqno "+seq_no);
                    Packet neighbor_request = new Packet(Config.ROUTER_ID, "ALIVE_MESSAGE", Config.Neighbors_table.get(IDs).Dest, seq_no);
                    sLSRP.sendPacket(neighbor_request);
                    synchronized (alivesenthistory) {
                        alivesenthistory.put(seq_no, new PacketHistory(neighbor_request, IDs));
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
        ArrayList<Integer> remove_keys = new ArrayList<>();
        for(int seq_key: alivesenthistory.keySet()){
            PacketHistory check = alivesenthistory.get(seq_key);
            String linkkey = Config.ROUTER_ID + "_" + check.getDest_id();
            if(check.getAck()){
                //remove ones that have acked.
//                System.out.println("remove ACKED element");
                remove_keys.add(seq_key);

            }else{
                if(check.getCounts()>=3){
                    // remove the link
                    synchronized (sLSRP.links){
                        System.out.println("remove the failed link "+linkkey+" in router "+Config.ROUTER_ID);
                        sLSRP.links.remove(linkkey);
                    }

                    String tmp[] = linkkey.split("_");
                    synchronized (Config.Established_Connect){
                        System.out.println("remove the failed link from establisehd connection with "+ tmp[1]);
                        Config.Established_Connect.remove(tmp[1]);
                    }

                    // remove the entry from established connections
                    // TODO the sendFailure LSA function
                    System.out.println("the link is dead "+ linkkey + " will send out LSA FAILURE to neighbors from "+Config.ROUTER_ID);
                    LSAThread.sendFailureLSA(linkkey);

                    remove_keys.add(seq_key);
                }else{
                    // update links use RTT to update the cost, don't use Alive Message
                    synchronized (alivesenthistory) {
                        alivesenthistory.get(seq_key).increaseCounts();
                    }
                    sLSRP.sendPacket(check.getPacket());
                    resend_flag = true;
                }
            }
        }

        for(int i=0; i<remove_keys.size();i++){
            synchronized (alivesenthistory){
                alivesenthistory.remove(remove_keys.get(i));
            }
        }
        return resend_flag;
    }

    public void printSentHistory(){
        System.out.println("seq_no\tACK\tCounts\tDest_Router_ID");
        for(int seq_no_key: alivesenthistory.keySet()){
            System.out.println(seq_no_key+"\t"+alivesenthistory.get(seq_no_key).getAck()+"\t"+alivesenthistory.get(seq_no_key).getCounts()+"\t"+alivesenthistory.get(seq_no_key).getDest_id());
        }
    }

}
