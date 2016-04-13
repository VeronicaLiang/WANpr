
import java.io.Serializable;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.Handler;

/**
 * Alive message thread, sending out alive message periodically
 */
public class AliveMessageThread implements Runnable {
    public static Hashtable<Integer,Boolean> sentmessages = new Hashtable<>();
    private int seq_no = 0;
    private String lastone = null;

    @Override
    public void run(){
        while (!sLSRP.Failure) {
            for (int IDs : Config.Neighbors_table.keySet()) {
                if(!Config.Established_Connect.containsKey(IDs)){
                    continue;
                }
                if(lastone != null){
                    if(!sentmessages.get(seq_no-1)){
                        // previous one did not receive acknowledgement until now (timer expired)
                        // do not re-send, since we use counter to indicate links active state.
                        synchronized (sLSRP.links){
                            if(sLSRP.links.containsKey(lastone)){
                                if(sLSRP.links.get(lastone).getNon_Active_Count() > 3){
                                    // the links cannot be reached at least 3 times, consecutively. Then report the failure
                                    //TODO failed link needs to take care of. need to send out LSA messsage
                                }else{
                                    sLSRP.links.get(lastone).active = false;
                                    sLSRP.links.get(lastone).IncreaseNonActiveCount();
                                }
                            }
                        }
                    }else{
                        synchronized (sLSRP.links){
                            sLSRP.links.get(lastone).active = true;
                            sLSRP.links.get(lastone).resetNon_Active_Count();
                        }
                    }
                }

                Packet neighbor_request = new Packet(Config.ROUTER_ID, "ALIVE_MESSAGE", Config.Neighbors_table.get(IDs).Dest, seq_no);
                sLSRP.sendPacket(neighbor_request);
                lastone = String.valueOf(Config.ROUTER_ID) + "_" + String.valueOf(IDs);
                sentmessages.put(seq_no,false);
                seq_no++;
//                System.out.println("sending ALIVE MESSAGE to "+ IDs);
//                try {
//                    Socket socket = new Socket(Config.Neighbors_table.get(IDs).Dest, servPort);
//                    ObjectOutputStream outputstream = new ObjectOutputStream(socket.getOutputStream());
//                    outputstream.writeObject(neighbor_request);
//
//                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                    String response = in.readLine();
//                    String links_key = String.valueOf(Config.ROUTER_ID) + "_" + String.valueOf(IDs);
//                    if (response.equals("ALIVE")) {
//                        System.out.println("receive alive message");
//                        synchronized (sLSRP.links){
//                            sLSRP.links.get(links_key).active = true;
//                            sLSRP.links.get(links_key).resetNon_Active_Count();
//                        }
//                    } else {
//                        synchronized (sLSRP.links){
//                            if(sLSRP.links.containsKey(links_key)){
//                                if(sLSRP.links.get(links_key).getNon_Active_Count() > 3){
//                                    // the links cannot be reached at least 3 times, consecutively.
//                                    //TODO failed link needs to take care of. need to send out LSA messsage
//                                }else{
//                                    sLSRP.links.get(links_key).active = false;
//                                    sLSRP.links.get(links_key).IncreaseNonActiveCount();
//                                }
//                            }
//                        }
//                    }
//                    // TODO: add a timer
//                    socket.close();
//                } catch (UnknownHostException ex) {
//                    // ex.printStackTrace();
//                } catch (ConnectException e) {
//                    // e.printStackTrace();
//                } catch (IOException e) {
//                    // e.printStackTrace();
//                }
                try {
                    // A Round of Robbin Fashion
                    Thread.sleep(Config.HELLO_INTERVAL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

}
