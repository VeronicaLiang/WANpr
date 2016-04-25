/**
 * Created by Xiaoyu on 3/10/2016.
 * Reading the configuration file, and setup the initial parameters.
 */

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;

public class Config {
    public static ArrayList<Neighbors> Neighbors_List = new ArrayList<>();
    public static String ip;
    public static int ROUTER_ID = -1;
    public static int HELLO_INTERVAL = 5000;  // send ALIVE message every 5 seconds
    public static int UPDATE_INTERVAL = 10000;
    public static int FORWARD_INTERVAL;
    public static int LSA_INTERVAL = 10000;    // send LSA message every 10 seconds
    public static int AGE_LIMITATION = 1000000;
    public static long LSA_TIMER = 5000;       // the timer is 5 seconds
    public static long ALIVE_TIMER = 5000;
    public static int SERV_PORT = 5574;
    public static double DROP_RATE = 0.1;
    public static double ERROR_RATE = 0.1;
    public static Hashtable<Integer, Neighbors> Neighbors_table = new Hashtable<>(); // key is the Router ID
    // the key is the directed neighbor, value is the cost. At beginning, the cost is set to 0
//    public static Hashtable<Integer, Integer> Established_Connect = new Hashtable<>(); // key is Router ID
    public static Hashtable<Integer, String> Id_Host = new Hashtable<>(); // key is Router IP's last digits , value is the host name
    public static Hashtable<String, Integer> Host_Id = new Hashtable<>();
    public static Hashtable<Integer, Integer> Id_Port = new Hashtable<>();// key is the Router's ID, value is the serv port number

    public static void configuration (String inputFile) throws IOException {
        String line;

        try {
            FileReader filereader = new FileReader(inputFile);
            BufferedReader bufferedreader = new BufferedReader(filereader);
            while ((line = bufferedreader.readLine()) != null) {
                if (line.contains("=")) {
                    String[] tmp = line.split("=");
                    String para = tmp[0].trim();
                    String val = tmp[1].trim();
                    if (para.equals("NEIGHBOR")) {
                        // Assume the current format is NEIGHBOR = IP, Port
                        String[] add_tmp = tmp[1].split(",");
                        String ip_addr = add_tmp[0].trim();
                        int port_no = Integer.parseInt(add_tmp[1].trim());
                        Neighbors new_one = new Neighbors(ip_addr,"unknown",port_no, -1);
                        Neighbors_List.add(new_one);
                    } else if(para.equals("HELLO_INTERVAL")) {
                        HELLO_INTERVAL = Integer.parseInt(val);
                    }else if(para.equals("UPDATE_INTERVAL")) {
                        UPDATE_INTERVAL = Integer.parseInt(val);
                    }else if(para.equals("FORWARD_INTERVAL")) {
                        FORWARD_INTERVAL = Integer.parseInt(val);
                    }else if(para.equals("ROUTER_ID")){
                        ROUTER_ID = Integer.parseInt(val);
                    }else if(para.equals("SERV_PORT")){
                        SERV_PORT = Integer.parseInt(val);
                    }
                }
            }
            bufferedreader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }




        // Register itself
        InetAddress inetAddr = InetAddress.getLocalHost();
        ip = inetAddr.getHostAddress();
        System.out.println("My ip: " + ip);
        String hostname = inetAddr.getHostName();
        System.out.println("My host name: " + hostname);
//        String output = ip + "\t" + ROUTER_ID + "\t" + hostname + "\t" + "4555\n";
        String output = ip + "\t" + ROUTER_ID + "\t" + hostname + "\t" + SERV_PORT+"\n";
        System.out.println(output);

        // Register self to the host_list file.
        try {
            File file = new File("host_list");
            if (file.createNewFile()) {
                System.out.println("create new file");
            }else{
                System.out.println("File exist");
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            synchronized (fw){
                fw.write(output);
            }
            fw.close();
        }catch (Exception e) {
            e.printStackTrace();
        }

        // Look up the host_list file for each neighbor.
        while(true){
            Hashtable<String, String> tmp = new Hashtable<>();
            Hashtable<String, Integer> id_table = new Hashtable<>();
            try{
                FileReader filereader = new FileReader("host_list");
                BufferedReader bufferedreader = new BufferedReader(filereader);
                while ((line = bufferedreader.readLine()) != null) {
                    String [] records = line.split("\t");
                    // TODO now hard code the key to the last part of ip, since the containsKey, get doesn't work
                    String [] tmp_addr = records[0].trim().split("\\.");
                    tmp.put(tmp_addr[tmp_addr.length - 1], records[2].trim());
                    id_table.put((tmp_addr[tmp_addr.length - 1]), Integer.parseInt(records[1].trim()));
                    String [] tmp_host = records[2].trim().split("\\.");
                    Id_Host.put(Integer.parseInt(records[1].trim()),tmp_host[0].trim());
                    Host_Id.put(records[2].trim(),Integer.parseInt(records[1].trim()));
                    int portno = Integer.parseInt(records[3].trim());
                    Id_Port.put(Integer.parseInt(records[1].trim()), portno);
                }
                bufferedreader.close();

            }catch (Exception e){
                e.printStackTrace();
            }

//            System.out.println("after reading the host_list");
            System.out.println(tmp);
            System.out.println(id_table);
            System.out.println("**************");
            System.out.println(Id_Host);
            System.out.println("**************");
//            System.out.println("list size: "+ Neighbors_List.size());

            for(int i = 0;i<Neighbors_List.size();i++){
                Neighbors check_neigh = Neighbors_List.get(i);
                if (check_neigh.Found){
                    System.out.println("Continue....");
                    continue;
                }
                // TODO again, use only the last part as the key, which should be pay more attention
                String [] check_ip = check_neigh.IP.split("\\.");
                String host_name = tmp.get(check_ip[check_ip.length-1]);
//                System.out.println("host_name "+ host_name);
                if(host_name != null){
//                    System.out.println("remove one entry");
                    check_neigh.Dest = host_name;
                    // TODO hard code the port number, need to know how to sign
                    check_neigh.Port = Id_Port.get(id_table.get(check_ip[check_ip.length - 1]));
                    check_neigh.Router_ID = id_table.get(check_ip[check_ip.length - 1]);
                    Neighbors_table.put(check_neigh.Router_ID,check_neigh);
                    check_neigh.Found = true;
                    Neighbors_List.set(i,check_neigh);;
                }
            }

            if (Neighbors_List.size() == Neighbors_table.size()){
                break;
            }

            try {
                Thread.sleep(10000);
            }catch (InterruptedException e ){
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("done with neighbors");
        System.out.println("My Serv Port is "+SERV_PORT);
        System.out.println(Id_Port);
    }
}

