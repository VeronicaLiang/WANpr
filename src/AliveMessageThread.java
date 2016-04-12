
import java.io.Serializable;
import java.net.*;
import java.io.*;

/**
 * Alive message thread, sending out alive message periodically
 */
public class AliveMessageThread implements Runnable {

    public void run(){
        int servPort = 4545;
        while (!sLSRP.Failure) {
//            for (int IDs : Config.Neighbors_table.keySet()) {
            if(Config.Established_Connect.size() == 0){
                continue;
            }
            for (int IDs: Config.Established_Connect.keySet()){
                Packet neighbor_request = new Packet(Config.ROUTER_ID, "ALIVE_MESSAGE", Config.Neighbors_table.get(IDs).Dest);
                System.out.println("sending ALIVE MESSAGE to "+ IDs);
                try {
                    Socket socket = new Socket(Config.Neighbors_table.get(IDs).Dest, servPort);
                    ObjectOutputStream outputstream = new ObjectOutputStream(socket.getOutputStream());
                    outputstream.writeObject(neighbor_request);

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String response = in.readLine();
                    String links_key = String.valueOf(Config.ROUTER_ID) + "_" + String.valueOf(IDs);
                    if (response.equals("ALIVE")) {
                        System.out.println("receive alive message");
                    } else {
                        synchronized (sLSRP.links){
                            if(sLSRP.links.containsKey(links_key)){
                                sLSRP.links.get(links_key).active = false;
                            }
                        }

                    }

                    // TODO: add a timer
                    // TODO: add a counter for indicating the link is die
                    socket.close();
                } catch (UnknownHostException ex) {
                    // ex.printStackTrace();
                } catch (ConnectException e) {
                    // e.printStackTrace();
                } catch (IOException e) {
                    // e.printStackTrace();
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

}
