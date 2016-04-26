/**
 * Main File.
 */
import java.net.*;
import java.net.ServerSocket;
import java.net.ServerSocket.*;
import java.io.*;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.ObjectOutputStream;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;

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

    public static Hashtable<Integer, Integer> Established_Connect = new Hashtable<>(); // key is Router ID

    // the default
    public static boolean Failure = false;

    // the endsystem instance
    public static EndSystem application = new EndSystem();
    public static LSADatabaseThread lsadth = new LSADatabaseThread();

    public static ArrayList<Integer> faillist = new ArrayList<>();

    public sLSRP(String inputFile){

    }

    public static int User_Interface(){
        boolean running_flag = true;
        while(running_flag) {
            System.out.println("Please select action:");
            System.out.println("1: Drop");
            System.out.println("2: Recover");
            System.out.println("3: Print Out the current Topology");
            System.out.println("4: Print the current routing table");
            System.out.println("5: Print current LSA database");
            System.out.println("6: Transfer a file");
            System.out.println("7: Exit the program");
            Scanner sc = new Scanner(System.in);
            int choice2 = Integer.parseInt(sc.nextLine());
            switch (choice2) {
                case 1:
                    System.out.println("Choose from the following options");
                    System.out.println("A - The whole router is down");
                    int charindex = 66; // 66 is B
                    ArrayList<String> index_record = new ArrayList<>();
                    for(String available_links: sLSRP.links.keySet()){
                        if(available_links.contains(Config.ROUTER_ID+"_")){
                            System.out.println((char) charindex + " - Drop link "+available_links);
                            index_record.add(available_links);
                            charindex++;
                        }
                    }
                    String subchoice = sc.nextLine().trim();

                    if(subchoice.equals("A")){
                        System.out.println("The Router will be down");
                        Failure = true;
                        ServerThread.running = false;
                    }else{
                        char tmp = subchoice.charAt(0);
                        String faillink = index_record.get((int) tmp - 65 - 1);
                        System.out.println("link "+ faillink +" is going down");
                        String [] nodes = faillink.split("_");
                        faillist.add(Integer.parseInt(nodes[1]));
                        //remove the failed link
                        synchronized (sLSRP.links){
                            if(sLSRP.links.containsKey(faillink)) {
                                System.out.println("remove fail link "+ faillink);
                                sLSRP.links.remove(faillink);
                                LSAThread.sendFailureLSA(faillink);
                            }
                        }

                        //remove the established connect
                        synchronized (sLSRP.Established_Connect){
                            if(sLSRP.Established_Connect.containsKey(Integer.parseInt(nodes[1]))){
                                sLSRP.Established_Connect.remove(Integer.parseInt(nodes[1]));
                            }
                        }

                    }
                    break;
                case 2:
                    System.out.println("Recovering the router/link ... ");
                    synchronized (Established_Connect) {
                        Established_Connect = new Hashtable<>();
                    }

                    sLSRP.starttime = System.currentTimeMillis();
                    if(Failure) {
                        Failure = false;
                        ServerThread.running = true;
                        Runnable serv = new ServerThread();
                        new Thread(serv).start();

                        sLSRP.converge = false;
                        NeighborRequThread.sendflag = true;
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

                        new Thread(lsadth).start();
                    }else{
                        // empty fail list
                        faillist = new ArrayList<>();
                    }
                    break;
                case 3:
                    sLSRP.graph.print();
                    break;
                case 4:
                    int source_id = sLSRP.router_nodes.get(Config.ROUTER_ID);
                    int [] pred = Dijkstra.dijkstra (sLSRP.graph, source_id);
                    for (int n=0; n<sLSRP.graph.size(); n++) {
                        Dijkstra.printPath (sLSRP.graph, pred, source_id, n);
                    }
                    System.out.println(sLSRP.routing_table);
                    break;
                case 5:
                    System.out.println("Link ID \t Adv. Router Id\t Link Counts\t Seq #\t Time Created");
                    for(int i:sLSRP.lsadb.keySet()){
                        LSADatabase tmp = sLSRP.lsadb.get(i);
                        System.out.println(tmp.linkid+"\t"+tmp.adv_router+"\t"+tmp.linkcounts+"\t"+tmp.seqno+"\t"+tmp.createdtime);
                    }
                    break;
                case 6:
                    System.out.println("Please input the destination IP ");
                    Hashtable<Integer, Integer> chose_dest_table = new Hashtable<>();
                    int option_count = 1;
                    for (int ava_rout_id: sLSRP.routing_table.keySet()){
                        System.out.println(option_count +": "+Config.Id_Host.get(ava_rout_id)+" with router id "+ava_rout_id);
                        chose_dest_table.put(option_count,ava_rout_id);
                        option_count++;
                    }
                    int chose_dest = Integer.parseInt(sc.nextLine());
                    int chose_dest_id = chose_dest_table.get(chose_dest);
                    System.out.println("Please input the file you want to transfer");
                    String path = sc.nextLine();
                    application.setPar(chose_dest_id, Config.ROUTER_ID, path);
                    try {
                        application.sender();
                    }catch (Exception e){

                    }
                    break;
                case 7:
                    sLSRP.Failure = true;
                    running_flag = false;
                    break;
            }
//            System.out.println(running_flag);
        }
        System.out.println("END");
        System.exit(0);
        return 1;

    }

    public static void main(String[] args)throws Exception{
        // Read in the parameters, find out neighbors
        if(args.length > 1){
            sLSRP.edgeno = Integer.parseInt(args[1]);
        }


        try {
            String file = args[0];
            Config.configuration(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        System.out.println(Config.Neighbors_table.keySet() );

        sLSRP.starttime = System.currentTimeMillis();
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

        // Start the LSA Database Thread
        new Thread(lsadth).start();


        int a = User_Interface();

//        File register_file = new File("host_list");
//        try {
//            register_file.delete();
//        }  catch (Exception e) {
//            e.printStackTrace();
//        }

        System.exit(0);

    }

    public static void sendPacket (Packet m){
        String send_dest = m.getDestination();
        int testnodeid = Config.Host_Id.get(send_dest);
        int servPort = Config.Id_Port.get(testnodeid);
        try {
            if(!sLSRP.faillist.contains(testnodeid)){
//                System.out.println("sending packet to "+send_dest+" having port no "+servPort);
                Socket socket = new Socket(m.getDestination(), servPort);
                ObjectOutputStream outputstream  = new ObjectOutputStream(socket.getOutputStream());
                outputstream.writeObject(m);
                socket.close();
            }

        }catch (UnknownHostException ex){
//            ex.printStackTrace();
        }catch(ConnectException e){
//            e.printStackTrace();

        }catch(IOException e){
//            e.printStackTrace();
        }

    }
}
