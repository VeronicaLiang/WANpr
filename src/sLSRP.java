/**
 * Main File.
 */
import java.net.*;
import java.net.ServerSocket;
import java.net.ServerSocket.*;
import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.io.ObjectOutputStream;

public class sLSRP {
//    public static ArrayList<Links> links = new ArrayList<>();

    public static Hashtable<String, Links> links = new Hashtable<>();
    public static Hashtable<Integer, LSADatabase> lsadb = new Hashtable<>();

    public static Hashtable<Integer,Integer> router_nodes = new Hashtable<>(); // Key is the router id, value is the vertex id in graph

    // the default
    public static boolean Failure = false;

    public sLSRP(String inputFile){

    }

    public static void User_Interface(){
        System.out.println("Please select action:");
        System.out.println("1: Drop");
        System.out.println("2: Recover");
        Scanner sc = new Scanner(System.in);
        int choice2 = Integer.parseInt(sc.nextLine());
        if(choice2 == 1){
            System.out.println("preparing drop the link");
            Failure = true;
        }


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

        // Start the RTT Analysis Thread
        RTTAnalysis rtt = new RTTAnalysis();
        new Thread(rtt).start();

        // Start the Alive Message Thread
        AliveMessageThread alive = new AliveMessageThread();
        new Thread(alive).start();

        // Start the LSA Message Thread
        LSAThread lsa = new LSAThread();
        new Thread(lsa).start();

        User_Interface();


//        File register_file = new File("host_list");
//        try {
//            register_file.delete();
//        }  catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    public static void sendPacket (Packet m){
        int servPort = Config.SERV_PORT;
        try {
            Socket socket = new Socket(m.getDestination(), servPort);
//            System.out.println("Connected to server...");
            ObjectOutputStream outputstream  = new ObjectOutputStream(socket.getOutputStream());
            outputstream.writeObject(m);
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
