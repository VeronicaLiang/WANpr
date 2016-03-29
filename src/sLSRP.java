/**
 * Main File.
 */
import java.net.*;
import java.net.ServerSocket;
import java.net.ServerSocket.*;
import java.io.*;
import java.nio.file.NoSuchFileException;

public class sLSRP {
    public static String ip;

    public sLSRP(String inputFile){

    }

    public static void main(String[] args)throws Exception{
        Config conf = new Config();
//        conf.register();
        String file;
        InetAddress inetAddr = InetAddress.getLocalHost();
        try {
            file = args[0];
            conf.configuration(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for(String IPs:conf.Neighbors_table.keySet()){
            System.out.println(IPs + ": " + conf.Neighbors_table.get(IPs).Dest + " " + conf.Neighbors_table.get(IPs).Port);
            Packet neighbor_request = new Packet();
            neighbor_request.type = "NEIGHBOR_REQUEST";
            neighbor_request.Destination = conf.Neighbors_table.get(IPs).Dest;
            String response = sendPacket(neighbor_request);
            if(response.equals("NEIGHBOR_REQUEST")){
                System.out.println("connection established");
            }
        }

        ServerThread ser = new ServerThread();
        new Thread (ser).start();
        // Start the Alive Message Thread
        AliveMesssage alivemessage = new AliveMesssage();
        new Thread(alivemessage).start();

        // Start the LSA Message Thread
        LSAThread lsa = new LSAThread();
        new Thread(lsa).start();


//        File register_file = new File("host_list");
//        try {
//            register_file.delete();
//        }  catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private static String sendPacket (Packet m){
        // TODO send packet to destination
        byte[] byteBuffer = m.type.getBytes();

        // hard code the port number
        int servPort = 4545;

        try {
            Socket socket = new Socket(m.Destination, servPort);
            System.out.println("Connected to server...sending echo string");
//            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            out.write(byteBuffer);  // Send the encoded string to the server

            // Receive the same string back from the server
//            int totalBytesRcvd = 0;  // Total bytes received so far
//            int bytesRcvd;           // Bytes received in last read
//            while (totalBytesRcvd < byteBuffer.length) {
//                if ((bytesRcvd = in.read(byteBuffer, totalBytesRcvd,
//                        byteBuffer.length - totalBytesRcvd)) == -1)
//                    throw new SocketException("Connection close prematurely");
//                totalBytesRcvd += bytesRcvd;
//            }
//
//            System.out.println("Received: " + new String(byteBuffer));
            //TODO currently have the return string here, may be changed later.
            socket.close();
        }catch (UnknownHostException ex){
           // ex.printStackTrace();
        }catch(ConnectException e){
           // e.printStackTrace();
        }catch(IOException e){
           // e.printStackTrace();
        }
        return new String (byteBuffer);

    }
}
