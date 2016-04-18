import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Periodically generate and send out LSA Messages to neighbors
 */
public class LSAThread implements Runnable{
    public static int seq_no = 0;
    public static Hashtable<Integer, PacketHistory> lsasenthistory = new Hashtable<>();
    public static Hashtable<Integer, PacketHistory> faillsasenthistory = new Hashtable<>();

    public void run (){
        try {
            double times = (Config.LSA_INTERVAL/Config.LSA_TIMER);
            while (!sLSRP.Failure) {
                LSAMessage needsend = GenerateLSA(Config.ROUTER_ID);
                for(int direct_neigh: Config.Neighbors_table.keySet()){
                    if(!Config.Established_Connect.containsKey(direct_neigh)){
                        continue;
                    }
                    needsend.setSeqno(seq_no);
                    Packet lsapack = new Packet(Config.ROUTER_ID,"LSA_MESSAGE",Config.Neighbors_table.get(direct_neigh).Dest,needsend);
                    lsapack.setLSAMessage(needsend);
                    sLSRP.sendPacket(lsapack);
                    synchronized (lsasenthistory) {
                        lsasenthistory.put(seq_no, new PacketHistory(lsapack, direct_neigh) );
                    }
//                    System.out.println("Sending LSA Message with seqno " + seq_no);
                    seq_no ++;
                }

                int timercount = 0;
                do{
                    Thread.sleep(Config.LSA_TIMER);
                    timercount ++;
//                    printSentHistory();
//                    System.out.println("Has been experienced "+timercount+" times time out, total would be "+ times);
                }while((checkHistory()) && (timercount < times));

//                System.out.println("OUT SIDE THE TIMER LOOP  ****** ");

                //todo  Round of Robbin Fashion.
                long sleeptime = Config.LSA_INTERVAL - timercount*Config.LSA_TIMER;
//                System.out.println("It will wait another "+sleeptime);
                Thread.sleep(sleeptime);
            }
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public static LSAMessage GenerateLSA(int adver_id){
        LSAMessage lsa = new LSAMessage(adver_id);
        String key_set = Config.ROUTER_ID+"_";
//        System.out.println(" the start router is "+key_set);
        int link_count = 0;
        for (String linkkey: sLSRP.links.keySet()){
//            System.out.println("linkkey from sLSRPlinks "+ linkkey);
            if(linkkey.contains(key_set)){
//                System.out.println("Contains");
                lsa.AddLinks(sLSRP.links.get(linkkey));
                link_count ++;
            }
        }
//        System.out.println(link_count);
        lsa.setLinkCount(link_count);
        return lsa;
    }

    public static LSAMessage GenerateFailLSA(int adver_id, String deletelink){
        LSAMessage lsa = new LSAMessage(adver_id, deletelink);
        String key_set = Config.ROUTER_ID+"_";
        int link_count = 0;
        for (String linkkey: sLSRP.links.keySet()){
            if(linkkey.contains(key_set)){
                lsa.AddLinks(sLSRP.links.get(linkkey));
                link_count ++;
            }
        }
        lsa.setLinkCount(link_count);
        return lsa;
    }

    public static void sendFailureLSA(String passlinkkey){
        LSAMessage failm = GenerateFailLSA(Config.ROUTER_ID, passlinkkey);
        for(int direct_neigh: Config.Established_Connect.keySet()){
            failm.setSeqno(seq_no);
            Packet faillsapack = new Packet(Config.ROUTER_ID, "FAILURE_LSA", Config.Neighbors_table.get(direct_neigh).Dest, failm);
            faillsapack.setLSAMessage(failm);
            synchronized (faillsasenthistory) {
                faillsasenthistory.put(seq_no, new PacketHistory(faillsapack, direct_neigh) );
            }
//          synchronized (seq_no) {
            LSADatabaseThread.fail_time = System.currentTimeMillis();
            seq_no++;
//          }
            sLSRP.sendPacket(faillsapack);
        }
        // wait for twice time out
        int timeoutcount = 0;
        while(faillsasenthistory.size()>0 && timeoutcount<2){
            ArrayList<Integer> acked = new ArrayList<>();
            for(int i : faillsasenthistory.keySet()){
                PacketHistory check = faillsasenthistory.get(i);
                if(check.getAck()){
                    acked.add(i);
                }else{
                    Packet resend = check.getPacket();
                    sLSRP.sendPacket(resend);
                }
            }
            synchronized (faillsasenthistory){
                for(int j=0;j<acked.size();j++){
                    faillsasenthistory.remove(acked.get(j));
                }
            }
            timeoutcount++;
            try {
                Thread.sleep(5000);
            }catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }


    private boolean checkHistory(){
        boolean resend_flag = false;
        ArrayList<Integer> remove_list = new ArrayList<>();
        for(int seq_key: lsasenthistory.keySet()) {
            PacketHistory check = lsasenthistory.get(seq_key);
            if (check.getAck()) {
                //remove ones that have acked.
//                System.out.println("remove ACKED element");
                remove_list.add(seq_key);
            } else {
                if(check.getCounts() > 5){
                    // if it has been sent for 5 times, remove it from the history table
                    remove_list.add(seq_key);
                }else{
                    // resend the packet
//                    System.out.println("resend the packet with seqno: " + seq_key);
                    synchronized (lsasenthistory) {
                        lsasenthistory.get(seq_key).increaseCounts();
                    }
                    sLSRP.sendPacket(check.getPacket());
                    resend_flag = true;
                }
            }
        }

        for(int i=0; i< remove_list.size(); i++){
            synchronized (lsasenthistory){
                lsasenthistory.remove(remove_list.get(i));
            }
        }
        return resend_flag;
    }

    public void printSentHistory(){
        System.out.println("seq_no\tACK\tCounts\tDest_Router_ID");
        for(int seq_no_key: lsasenthistory.keySet()){
            System.out.println(seq_no_key+"\t"+lsasenthistory.get(seq_no_key).getAck()+"\t"+lsasenthistory.get(seq_no_key).getCounts()+"\t"+lsasenthistory.get(seq_no_key).getDest_id());
        }
    }


}
