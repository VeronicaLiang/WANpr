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

                System.out.println("Handling client at " +
                        clntSock.getInetAddress().getHostAddress() + " on port " +
                        clntSock.getPort());

                ObjectInputStream inputstream = new ObjectInputStream(clntSock.getInputStream());
                Packet recv = (Packet) inputstream.readObject();
                String packet_type= recv.Type;
                switch (packet_type){
                    case "NEIGHBOR_REQUEST":
                        System.out.println("NEIGHBOR REQUEST RECEIVED");
                }
                inputstream.close();
                clntSock.close();  // Close the socket.  We are done with this client!
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
