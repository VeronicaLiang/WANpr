/**
 * Main File.
 */
import java.net.ServerSocket.*;

public class sLSRP {
    public static String ip;

    public sLSRP(String inputFile){

    }

    public static void main(String[] args)throws Exception{
        String file = "config.ini";
        try {
            file = args[0];
            Config.configuration(file);


        } catch (Exception e) {

        }

        ip = java.net.InetAddress.getLocalHost().getHostAddress();
        System.out.println("My ip: " + ip);

        // Start the Alive Message Thread
        AliveMesssage alivemessage = new AliveMesssage();
        new Thread(alivemessage).start();

        // Start the LSA Message Thread
        LSAThread lsa = new LSAThread();
        new Thread(lsa).start();
    }
}
