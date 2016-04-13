import java.io.*;
import java.net.*;
import java.util.Hashtable;

/**
 * Analyze Round Trip Time, and update the links cost, and Routing table information.
 */
//todo timer
//todo round-of-robbin fashion
public class RTTAnalysis implements Runnable{
    public static Hashtable<Integer,Long> sentmes = new Hashtable<>();
    private int seqno = 0;
    private long last;
    private Integer pastone;
    @Override
    public void run(){
//        int servPort = 4545;
        while (!sLSRP.Failure) {
            if(sLSRP.Failure){
                // under failure state, doing nothing
                continue;
            }

            for (int dir_neighbor: Config.Neighbors_table.keySet()){
                if(!Config.Established_Connect.containsKey(dir_neighbor)){
                    continue;
                }
//            for(int dir_neighbor: Config.Established_Connect.keySet()){

                if(seqno!=0){
                    //not the first one
                    String linkskey = Config.ROUTER_ID+"_"+pastone;
                    if (sentmes.get(seqno-1) == last){
                        // did not receive the ack within the time interval
                        synchronized (sLSRP.links) {
                            Links cur = sLSRP.links.get(linkskey);
                            cur.cost = (cur.cost + Config.UPDATE_INTERVAL) / 2;
                        }
                    }else{
                        System.out.println("update links "+linkskey+" cost *********");
                        long time = sentmes.get(seqno-1) - last;
                        System.out.println("passed time in million second is : " + time);
                        synchronized (sLSRP.links) {
                            Links cur = sLSRP.links.get(linkskey);
//                            cur.active = true;
                            cur.cost = (cur.cost + time) / 2;
                        }
                    }
                }
                Packet rtt = new Packet(Config.ROUTER_ID, "RTT_ANALYSIS",Config.Neighbors_table.get(dir_neighbor).Dest, seqno);
                last = System.currentTimeMillis();
                sentmes.put(seqno,last);
                pastone = dir_neighbor;
                sLSRP.sendPacket(rtt);
                seqno++;
//                System.out.println("sending RTT Analysis Message");
//                try{
//                    long start = System.currentTimeMillis();
//                    Socket socket = new Socket(Config.Neighbors_table.get(dir_neighbor).Dest, servPort);
//                    ObjectOutputStream outputstream = new ObjectOutputStream(socket.getOutputStream());
//                    outputstream.writeObject(rtt);
//
//                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                    String response = in.readLine();
//                    if(response.equals("ACK_RTT")) {
//                        System.out.println("Received the Acknoledge message");
//                        long end = System.currentTimeMillis();
//                        long time = end - start;
//                        System.out.println("passed time in million second is : " + time);
//                        String linkskey = Config.ROUTER_ID+"_"+dir_neighbor;
//                        synchronized (sLSRP.links) {
//                            Links cur = sLSRP.links.get(linkskey);
////                            cur.active = true;
//                            cur.cost = (cur.cost + time) / 2;
//                        }
//                    }
//                    socket.close();
//                }catch (UnknownHostException ex) {
//                    // ex.printStackTrace();
//                } catch (ConnectException e) {
//                    // e.printStackTrace();
//                } catch (IOException e) {
//                    // e.printStackTrace();
//                }

                // every time only send to one neighbor
                try {
                    Thread.sleep(Config.UPDATE_INTERVAL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
