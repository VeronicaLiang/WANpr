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
    public static Object sf_path;
    public static WeightedGraph graph = new WeightedGraph (1);
    public static int edgeno ;
    public static boolean converge = false;
    public static long starttime;

    // key is the destination Router Id, value is the forwarding Router Id
    public static Hashtable<Integer, Integer> routing_table = new Hashtable<>();

    public static Hashtable<Integer,Integer> router_nodes = new Hashtable<>(); // Key is the router id, value is the vertex id in graph

    // the default
    public static boolean Failure = false;

    public sLSRP(String inputFile){

    }

    public static void User_Interface(){
        boolean running_flag = true;
        while(running_flag) {
            System.out.println("Please select action:");
            System.out.println("1: Drop");
            System.out.println("2: Recover");
            System.out.println("4: Print Out the current Topology");
            System.out.println("5: Print the current routing table");
            System.out.println("6: Print current LSA database");
            Scanner sc = new Scanner(System.in);
            int choice2 = Integer.parseInt(sc.nextLine());
            switch (choice2) {
                case 1:
                    System.out.println("preparing drop the link");
                    Failure = true;
                    break;
                case 2:
                    System.out.println("Reovering the router ... ");
                    Failure = false;
                    break;
                case 3:
                    running_flag = false;
                    break;
                case 4:
                    sLSRP.graph.print();
                    break;
                case 5:
                    int source_id = sLSRP.router_nodes.get(Config.ROUTER_ID);
                    int [] pred = Dijkstra.dijkstra (sLSRP.graph, source_id);
                    for (int n=0; n<sLSRP.graph.size(); n++) {
                        Dijkstra.printPath (sLSRP.graph, pred, source_id, n);
                    }
                    System.out.println(sLSRP.routing_table);
                    break;
                case 6:
                    System.out.println("Link ID \t Adv. Router Id\t Link Counts\t Seq #\t Time Created");
                    for(int i:sLSRP.lsadb.keySet()){
                        LSADatabase tmp = sLSRP.lsadb.get(i);
                        System.out.println(tmp.linkid+"\t"+tmp.adv_router+"\t"+tmp.linkcounts+"\t"+tmp.seqno+"\t"+tmp.createdtime);
                    }


            }
        }


    }

    public static void main(String[] args)throws Exception{
        // Read in the parameters, find out neighbors
        if(args.length > 1){
            sLSRP.edgeno = Integer.parseInt(args[1]);
        }

        sLSRP.starttime = System.currentTimeMillis();
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

        // Start the Alive Message Thread
        AliveMessageThread alive = new AliveMessageThread();
        new Thread(alive).start();

        // Start the RTT Analysis Thread
        RTTAnalysis rtt = new RTTAnalysis();
        new Thread(rtt).start();

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
