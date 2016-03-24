/**
 * Main File.
 */
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.ServerSocket.*;
import java.io.*;
import java.nio.file.NoSuchFileException;

public class sLSRP {
    public static String ip;

    public sLSRP(String inputFile){

    }

    public static void main(String[] args)throws Exception{
        Config conf = new Config();
        conf.register();
        String file;
        InetAddress inetAddr = InetAddress.getLocalHost();
        try {
            file = args[0];
            conf.configuration(file);
            conf.register();

        } catch (Exception e) {
            e.printStackTrace();
        }

//        ip = inetAddr.getHostAddress();
//        System.out.println("My ip: " + ip);
//        String hostname = inetAddr.getHostName();
//        System.out.println("My host name: "+hostname);

        ServerThread ser = new ServerThread();
        new Thread (ser).start();
        // Start the Alive Message Thread
        AliveMesssage alivemessage = new AliveMesssage();
        new Thread(alivemessage).start();

        // Start the LSA Message Thread
        LSAThread lsa = new LSAThread();
        new Thread(lsa).start();


        File register_file = new File("host_list");
        try {
            register_file.delete();
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }
}
