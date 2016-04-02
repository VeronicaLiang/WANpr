import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Deal with connections with neighbors .
 */
public class Connections {

    public static void AddConnect(int key, int val){
        if (Config.Established_Connect.containsKey(key)){

        }else{
            Config.Established_Connect.put(key,val);
        }
    }

    public void RecvNeghPacket (Packet recv){

    }

}
