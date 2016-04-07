/**
 * Main File.
 */
import java.net.*;
import java.net.ServerSocket;
import java.net.ServerSocket.*;
import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Objects;
import java.io.ObjectOutputStream;

public class sLSRP {
//    public static ArrayList<Links> links = new ArrayList<>();

    public static Hashtable<String, Links> links = new Hashtable<>();
    public static Hashtable<Integer, LSADatabase> lsadb = new Hashtable<>();

    // the default
    public static boolean Failure = false;

    public sLSRP(String inputFile){

    }

    public void User_Interface(){

    }

    public static void main(String[] args)throws Exception{
        // Read in the parameters, find out neighbors
        try {
            String file = args[0];
            Config.configuration(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Start running server side
        ServerThread ser = new ServerThread();
        new Thread (ser).start();

        // building connection with direct neighbors.
        Runnable connection = new NeighborRequThread();
        new Thread(connection).start();
//        Config.BuildConnections();

        // Start the RTT Analysis Thread
//        RTTAnalysis rtt = new RTTAnalysis();
//        new Thread(rtt).start();

        // Start the Alive Message Thread
        AliveMessageThread alive = new AliveMessageThread();
        new Thread(alive).start();

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

    public static void sendPacket (Packet m){
        // TODO send packet to destination
        // hard code the port number
        int servPort = 4545;

        try {
            Socket socket = new Socket(m.getDestination(), servPort);
            System.out.println("Connected to server...");
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
