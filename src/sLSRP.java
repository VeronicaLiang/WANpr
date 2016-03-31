/**
 * Main File.
 */
import java.net.*;
import java.net.ServerSocket;
import java.net.ServerSocket.*;
import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.Objects;
import java.io.ObjectOutputStream;

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
            Packet neighbor_request = new Packet(conf.ROUTER_ID,"NEIGHBOR_REQUEST",conf.Neighbors_table.get(IPs).Dest);

            sendPacket(neighbor_request);

        }

        AliveMesssage alivemessage = new AliveMesssage();
        new Thread(alivemessage).start();


        ServerThread ser = new ServerThread();
        new Thread (ser).start();
        // Start the Alive Message Thread


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

    private static void sendPacket (Packet m){
        // TODO send packet to destination
        byte[] byteBuffer = m.Type.getBytes();

        // hard code the port number
        int servPort = 4545;

        try {
            Socket socket = new Socket(m.Destination, servPort);
            System.out.println("Connected to server...sending echo string");
            ObjectOutputStream outputstream  = new ObjectOutputStream(socket.getOutputStream());
            outputstream.writeObject(m);

            //TODO currently have the return string here, may be changed later.
            socket.close();
        }catch (UnknownHostException ex){
           // ex.printStackTrace();
        }catch(ConnectException e){
           // e.printStackTrace();
        }catch(IOException e){
           // e.printStackTrace();
        }
    }
}
