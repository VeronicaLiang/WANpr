/**
 * Created by Xiaoyu on 3/10/2016.
 * Reading the configuration file, and setup the initial parameters.
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;

public class Config {
    public static ArrayList<Neighbors> Neighbors_List = new ArrayList<>();
    public static String ip;
    public static int ROUTER_ID = 0;
    public static int HELLO_INTERVAL;
    public static int UPDATE_INTERVAL;
    public static int FORWARD_INTERVAL;
    public static Hashtable<String, Neighbors> Neighbors_table = new Hashtable<>(); // key is IP address

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
                        String port_no = add_tmp[1].trim();
                        Neighbors new_one = new Neighbors(ip_addr,port_no,"unknown");
                        Neighbors_List.add(new_one);
                    } else {
                        switch(para){
                            case "HELLO_INTERVAL":
                                HELLO_INTERVAL = Integer.parseInt(val) * 1000;
                            case "UPDATE_INTERVAL":
                                UPDATE_INTERVAL = Integer.parseInt(val) * 1000;
                            case "FORWARD_INTERVAL":
                                FORWARD_INTERVAL = Integer.parseInt(val) * 1000;
                            case "ROUTER_ID":
                                ROUTER_ID = Integer.parseInt(val);
                        }
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
        String output = ip + "\t" + hostname + "\t" + "4555\n";

        // Look up the host_list file for each neighbor.
        while(true){
            Hashtable<String, String> tmp = new Hashtable<>();
            try{
                FileReader filereader = new FileReader("host_list");
                BufferedReader bufferedreader = new BufferedReader(filereader);
                while ((line = bufferedreader.readLine()) != null) {
                    String [] records = line.split("\t");
                    // TODO now hard code the key to the last part of ip, since the containsKey, get doesn't work
                    String [] tmp_addr = records[0].trim().split("\\.");
                    tmp.put(tmp_addr[tmp_addr.length - 1], records[1].trim());
                }
                bufferedreader.close();

            }catch (Exception e){
                e.printStackTrace();
            }

            System.out.println("after reading the host_list");
            System.out.println(tmp);
            System.out.println("list size: "+ Neighbors_List.size());

            for(int i = 0;i<Neighbors_List.size();i++){
                Neighbors check_neigh = Neighbors_List.get(i);
                // TODO again, use only the last part as the key, which should be pay more attention
                String [] check_ip = check_neigh.IP.split("\\.");

                String host_name = tmp.get(check_ip[check_ip.length-1]);
                System.out.println("host_name "+ host_name);
                if(host_name != null){
                    System.out.println("remove one entry");
                    check_neigh.Dest = tmp.get(check_ip[check_ip.length - 1]);
                    // TODO hard code the port number, need to know how to sign
                    check_neigh.Port = "4545";
                    System.out.println(check_neigh.IP + " " + check_neigh.Dest + " " + tmp.get(check_ip[check_ip.length-1]));
                    Neighbors_table.put(check_neigh.IP,check_neigh);
                    Neighbors_List.remove(i);
                }
            }
            if (Neighbors_List.isEmpty()){
                break;
            }
            System.out.println("list size: "+ Neighbors_List.size());
            try {
                Thread.sleep(10000);
            }catch (InterruptedException e ){
                Thread.currentThread().interrupt();
            }


        }
        System.out.println("done with neighbors");
        System.out.println(Neighbors_table);
    }

}

