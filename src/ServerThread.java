import java.net.*;
import java.io.*;

/**
 * Created by Xiaoyu on 3/21/2016.
 */

public class ServerThread implements Runnable {
    private static final int BUFSIZE = 32;
    public void run() {
        try {
            ServerSocket servSock = new ServerSocket(4242);
            int recvMsgSize;   // Size of received message
            byte[] byteBuffer = new byte[BUFSIZE];  // Receive buffer

            while(true) { // Run forever, accepting and servicing connections
                Socket clntSock = servSock.accept();     // Get client connection

                System.out.println("Handling client at " +
                        clntSock.getInetAddress().getHostAddress() + " on port " +
                        clntSock.getPort());

                InputStream in = clntSock.getInputStream();
                OutputStream out = clntSock.getOutputStream();

                // Receive until client closes connection, indicated by -1 return
                while ((recvMsgSize = in.read(byteBuffer)) != -1)
                    out.write(byteBuffer, 0, recvMsgSize);

                clntSock.close();  // Close the socket.  We are done with this client!
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
