
import java.io.Serializable;
import java.net.*;
import java.io.*;

/**
 * Alive message thread, sending out alive message periodically
 */
public class AliveMessageThread implements Runnable {

    public void run(){
        int servPort = 4545;
        while (true) {
//            for (int IDs : Config.Neighbors_table.keySet()) {
            for (int IDs: Config.Established_Connect.keySet()){
                Packet neighbor_request = new Packet(Config.ROUTER_ID, "ALIVE_MESSAGE", Config.Neighbors_table.get(IDs).Dest);
                System.out.println("sending ALIVE MESSAGE");
//              sLSRP.sendPacket(neighbor_request);

                try {
                    long start = System.currentTimeMillis();
                    Socket socket = new Socket(Config.Neighbors_table.get(IDs).Dest, servPort);
                    System.out.println("Connected to server...");
                    ObjectOutputStream outputstream = new ObjectOutputStream(socket.getOutputStream());
                    outputstream.writeObject(neighbor_request);

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String response = in.readLine();
                    String links_key = String.valueOf(Config.ROUTER_ID) + "_" + String.valueOf(IDs);
                    if (response.equals("ALIVE")) {
                        System.out.println("receive alive message");
                        long end = System.currentTimeMillis();
                        long time = end - start;
                        System.out.println("passed time in million second is : " + time);
//                        synchronized (sLSRP.links) {
//                            if (sLSRP.links.containsKey(links_key)) {
//                                //update the cost
//                                Links cur = sLSRP.links.get(links_key);
//                                cur.active = true;
//                                cur.cost = (cur.cost + time) / 2;
////                                System.out.println("updating a link " + Config.ROUTER_ID + " " + IDs + " " + time);
//                            } else {
//                                Links newitem = new Links(Config.ROUTER_ID, IDs, time);
//                                sLSRP.links.put(links_key, newitem);
////                                System.out.println("Add a new link " + Config.ROUTER_ID + " " + IDs + " " + time);
//                            }
//                        }
                    } else {
                        synchronized (sLSRP.links){
                            if(sLSRP.links.containsKey(links_key)){
                                sLSRP.links.get(links_key).active = false;
                            }
                        }

                    }

                    // TODO: add a timer
                    socket.close();
                } catch (UnknownHostException ex) {
                    // ex.printStackTrace();
                } catch (ConnectException e) {
                    // e.printStackTrace();
                } catch (IOException e) {
                    // e.printStackTrace();
                }
            }
            try {
                Thread.sleep(Config.HELLO_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
