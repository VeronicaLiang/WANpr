import java.net.*;
import java.io.*;
import java.io.ObjectInputStream;

/**
 * The server side of the router.
 */

public class ServerThread implements Runnable {
    private static final int BUFSIZE = 32;
    private static final int WINDOWSIZE = 10;
    public void run() {
        try {
            ServerSocket servSock = new ServerSocket(4545);
            int recvMsgSize;   // Size of received message
            byte[] byteBuffer = new byte[BUFSIZE];  // Receive buffer

            while(true) { // Run forever, accepting and servicing connections
                Socket clntSock = servSock.accept();     // Get client connection

//                System.out.println("Handling client at " +
//                        clntSock.getInetAddress().getHostAddress() + " on port " +
//                        clntSock.getPort());

                ObjectInputStream inputstream = new ObjectInputStream(clntSock.getInputStream());
                Packet recv = (Packet) inputstream.readObject();
                String packet_type= recv.getType();
                switch (packet_type){
                    case "NEIGHBOR_REQUEST":
                        System.out.println("NEIGHBOR REQUEST RECEIVED");
                        Runnable ser = new NeighborRequACKThread(recv);
                        new Thread(ser).start();
                        break;
                    case "ACK_NEIGH":
                        System.out.println("Establish Neighbor Relationship");
                        Connections.AddConnect(recv.getId(),0);
                        String link_key = Config.ROUTER_ID +"_"+recv.getId();
                        // Temporarily the directed connected neighbors have cost of 1
                        Links tmp = new Links(Config.ROUTER_ID, recv.getId(),1);
                        sLSRP.links.put(link_key, tmp);
                        for(int i : Config.Established_Connect.keySet()){
                            System.out.println(i+"\t"+ Config.Established_Connect.get(i)+"%%%%");
                        }
                        break;
                    case "ALIVE_MESSAGE":
                        if(Config.Established_Connect.containsKey(recv.getId())){
//                            Packet ack = new Packet(Config.ROUTER_ID,"ACK_ALIVE", recv.getId());
                            System.out.println("This link is alive");
                            OutputStream out = clntSock.getOutputStream();
                            String ack = "ALIVE";
                            out.write(ack.getBytes());
                            clntSock.close();
                        }
                        break;
                    case "LSA_MESSAGE":
                        System.out.println("Receive LSA Message");
                        Runnable lsadb = new LSADatabaseThread(recv);
                        new Thread(lsadb).start();
                        break;
                    case "RTT_ANALYSIS":
                        OutputStream out = clntSock.getOutputStream();
                        String ack = "ACK_RTT";
                        out.write(ack.getBytes());
                        clntSock.close();
                        break;
                }
                inputstream.close();
                clntSock.close();  // Close the socket.  We are done with this client!
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
