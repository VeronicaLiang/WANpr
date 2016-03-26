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
    public static Hashtable<String, String> arg = new Hashtable<>();
    public static String ip;
    public static int Hello_Interval;
    public static Hashtable<String, Neighbors> Neighbors = new Hashtable<>(); // key is IP address

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
                        Neighbors new_one = new Neighbors(add_tmp[0].trim(),add_tmp[1].trim(),"unknown");
                        Neighbors_List.add(new_one);
                    } else {
                        arg.put(para, val);
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
        System.out.println("create new file");
        // Register self to the file
        try {
            File file = new File("host_list");
            if (!file.exists()) {
                file.createNewFile();
                System.out.println("create new file");
            }
//            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
//            synchronized (fw) {
//                fw.write(output);
//            }
//            fw.close();
        }catch (Exception e) {
            e.printStackTrace();
        }

        // Look up the host_list file for each neighbor.
        while(!Neighbors_List.isEmpty()){
            Hashtable<String, String> tmp = new Hashtable<>();
            try{
                FileReader filereader = new FileReader("host_list");
                BufferedReader bufferedreader = new BufferedReader(filereader);
                while ((line = bufferedreader.readLine()) != null) {
                    String [] records = line.split("\t");
                    tmp.put(records[0].trim(),records[1].trim());
                }
                bufferedreader.close();
            }catch (Exception e){
                e.printStackTrace();
            }

            System.out.println("after reading the host_list");
            System.out.println(tmp);
            System.out.println("list size: "+ Neighbors_List.size());
            for(int i = 0;i<Neighbors_List.size();i++){
                System.out.println(Neighbors_List);
                Neighbors check = Neighbors_List.get(i);
                System.out.println(check.IP);
                if(tmp.containsKey(check.IP)){
                    check.Dest = tmp.get(check.IP);
                    Neighbors.put(check.IP,check);
                    Neighbors_List.remove(i);
                }
            }
            System.out.println("list size: "+ Neighbors_List.size());
            try {
                Thread.sleep(10000);
            }catch (InterruptedException e ){
                Thread.currentThread().interrupt();
            }


        }
        System.out.println("done with neighbors");
        System.out.println(Neighbors);
    }

}

