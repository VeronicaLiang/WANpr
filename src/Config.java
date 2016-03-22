/**
 * Created by Xiaoyu on 3/10/2016.
 * Reading the configuration file, and setup the initial parameters.
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;

public class Config {
    public static ArrayList<Hashtable<String, String>> Neighbors = new ArrayList<>();
    public static Hashtable<String, String> arg = new Hashtable<>();
    public static String ip;

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
                        Hashtable<String, String> add_port = new Hashtable<>();
                        add_port.put(add_tmp[0].trim(), add_tmp[1].trim());
                        Neighbors.add(add_port);
                    } else {
                        arg.put(para, val);
                    }
                }
            }
            bufferedreader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void register () throws Exception{
        // Register self to the file
        InetAddress inetAddr = InetAddress.getLocalHost();
        ip = inetAddr.getHostAddress();
        System.out.println("My ip: " + ip);
        String hostname = inetAddr.getHostName();
        System.out.println("My host name: " + hostname);
        String output = ip + "\t" + hostname + "\t" + "4555";
        System.out.println("create new file");
        // Register self to the file
        try {
            File file = new File("host_list");
            if (!file.exists()) {
                file.createNewFile();
                System.out.println("create new file");
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            System.out.println("create new file");
            synchronized (fw) {
                fw.write(output);
            }
            fw.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}

