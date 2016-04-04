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
    public static int HELLO_INTERVAL;
    public static int UPDATE_INTERVAL;
    public static int FORWARD_INTERVAL;
    public static int LSA_INTERVAL = 10000;
    public static Hashtable<Integer, Neighbors> Neighbors_table = new Hashtable<>(); // key is the Router ID
    // the key is the directed neighbor, value is the cost. At beginning, the cost is set to 0
    public static Hashtable<Integer, Integer> Established_Connect = new Hashtable<>(); // key is Router ID

    public static void configuration (String inputFile) throws IOException {
        String line;

        try {
            FileReader filereader = new FileReader(inputFile);
            BufferedReader bufferedreader = new BufferedReader(filereader);
            while ((line = bufferedreader.readLine()) != null) {
//                System.out.println(line);
                if (line.contains("=")) {
                    String[] tmp = line.split("=");
                    String para = tmp[0].trim();
                    String val = tmp[1].trim();
                    if (para.equals("NEIGHBOR")) {
                        // Assume the current format is NEIGHBOR = IP, Port
                        String[] add_tmp = tmp[1].split(",");
                        String ip_addr = add_tmp[0].trim();
                        String port_no = add_tmp[1].trim();
                        Neighbors new_one = new Neighbors(ip_addr,"unknown",port_no, -1);
                        Neighbors_List.add(new_one);
                    } else if(para.equals("HELLO_INTERVAL")) {
                        HELLO_INTERVAL = Integer.parseInt(val) * 1000;
                    }else if(para.equals("UPDATE_INTERVAL")) {
                        UPDATE_INTERVAL = Integer.parseInt(val) * 1000;
                    }else if(para.equals("FORWARD_INTERVAL")) {
                        FORWARD_INTERVAL = Integer.parseInt(val) * 1000;
                    }else if(para.equals("ROUTER_ID")){
                        ROUTER_ID = Integer.parseInt(val);
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
        String output = ip + "\t" + ROUTER_ID + "\t" + hostname + "\t" + "4555\n";
        System.out.println(output);

        // Register self to the host_list file.
        try {
            File file = new File("host_list");
            if (!file.exists()) {
                file.createNewFile();
                System.out.println("create new file");
            }
//            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
//            synchronized (fw){
//                fw.write(output);
//            }
//            fw.close();
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
                }
                bufferedreader.close();

            }catch (Exception e){
                e.printStackTrace();
            }

//            System.out.println("after reading the host_list");
            System.out.println(tmp);
            System.out.println(id_table);
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
                    check_neigh.Port = "4545";
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
    }

    public static void BuildConnections (){
        boolean flag = true;
        while(flag) {
            for (int IDs : Config.Neighbors_table.keySet()) {
                if(Config.Established_Connect.containsKey(IDs)){
                    continue;
                }
                System.out.println(IDs + ": " + Config.Neighbors_table.get(IDs).Dest + " " + Config.Neighbors_table.get(IDs).Port);
                Packet neighbor_request = new Packet(Config.ROUTER_ID, "NEIGHBOR_REQUEST", Config.Neighbors_table.get(IDs).Dest);
                sLSRP.sendPacket(neighbor_request);
            }

//            System.out.println("Two size is the same? **** ");
//            System.out.println(Config.Established_Connect.size() + "\t" + Config.Neighbors_table.size());
            if(Config.Established_Connect.size() == Config.Neighbors_table.size()){
                flag = false;
            }else{
                System.out.println("waiting for connecting with neighbors");
                try{
                    Thread.sleep(10000);
                }catch (InterruptedException e){
                    Thread.currentThread();
                }
            }
        }
    }
}

