import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Send out Alive message periodically .
 */
public class AliveMesssage implements Runnable {
    public void run (){
////        Config.Neighbors_table
//        for(String IPs:Config.Neighbors_table.keySet()){
//            System.out.println(IPs + ": " + Config.Neighbors_table.get(IPs).Dest + " " + Config.Neighbors_table.get(IPs).Port);
//            Packet neighbor_request = new Packet();
//            neighbor_request.Type = "NEIGHBOR_REQUEST";
//            neighbor_request.Destination = Config.Neighbors_table.get(IPs).Dest;
//            byte[] byteBuffer = neighbor_request.Type.getBytes();
//            int servPort = 4545;
//            try {
//                Socket socket = new Socket(neighbor_request.Destination, servPort);
//                System.out.println("Connected to server...sending echo string");
//                OutputStream out = socket.getOutputStream();
//                out.write(byteBuffer);
//                socket.close();
//            }catch (UnknownHostException ex){
////                 ex.printStackTrace();
//            }catch(ConnectException e){
////                 e.printStackTrace();
//            }catch(IOException e){
////                 e.printStackTrace();
//            }
//            String response = sendPacket(neighbor_request);
//            String response = byteBuffer.toString();
//            if(response.equals("NEIGHBOR_REQUEST")){
//                System.out.println("connection established");
//            }
//        }
//        try {
//            Thread.sleep(Config.HELLO_INTERVAL);
//        }catch (InterruptedException e ){
//            Thread.currentThread().interrupt();
//        }

    }
    public static void receiveAck(){

    }
}
