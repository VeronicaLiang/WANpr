import java.io.*;
import java.net.*;

/**
 * Analyze Round Trip Time, and update the links cost, and Routing table information.
 */
//todo timer
//todo round-of-robbin fashion
public class RTTAnalysis implements Runnable{
    public void run(){
        int servPort = 4545;
        while (true) {
            if(sLSRP.Failure){
                // under failure state, doing nothing
                continue;
            }

            for(int dir_neighbor: Config.Established_Connect.keySet()){
                Packet rtt = new Packet(Config.ROUTER_ID, "RTT_ANALYSIS",Config.Neighbors_table.get(dir_neighbor).Dest);
                System.out.println("sending RTT Analysis Message");
                try{
                    long start = System.currentTimeMillis();
                    Socket socket = new Socket(Config.Neighbors_table.get(dir_neighbor).Dest, servPort);
                    ObjectOutputStream outputstream = new ObjectOutputStream(socket.getOutputStream());
                    outputstream.writeObject(rtt);

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String response = in.readLine();
                    if(response.equals("ACK_RTT")) {
                        System.out.println("Received the Acknoledge message");
                        long end = System.currentTimeMillis();
                        long time = end - start;
                        System.out.println("passed time in million second is : " + time);
                        String linkskey = Config.ROUTER_ID+"_"+dir_neighbor;
                        synchronized (sLSRP.links) {
                            Links cur = sLSRP.links.get(linkskey);
//                            cur.active = true;
                            cur.cost = (cur.cost + time) / 2;
                        }
                    }
                    socket.close();
                }catch (UnknownHostException ex) {
                    // ex.printStackTrace();
                } catch (ConnectException e) {
                    // e.printStackTrace();
                } catch (IOException e) {
                    // e.printStackTrace();
                }
            }

            //update the dijkstra maps

            try {
                Thread.sleep(Config.UPDATE_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
