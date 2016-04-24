import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Take care of sending and receiving files.
 */
public class EndSystem {
    int Destination; // The destination router id
    int Source;
    String Path;
    public static Hashtable<Integer, Boolean> sentdatahistory = new Hashtable<>();
    int seqno = 0;
    boolean connection = false;
    boolean finnish = false;
    int recvseqno = 0;
    public static Hashtable<Integer, String> routing_records = new Hashtable<>();

    public EndSystem(){

    }

    public EndSystem(int d, int s, String p){
        this.Destination = d;
        this.Source = s;
        this.Path = p;
    }

    public void setPar(int d, int s, String p){
        this.Destination = d;
        this.Source = s;
        this.Path = p;
    }

    public void sender () throws Exception{

        do {
            // has to wait for the communication channel established.
            startComm();
            System.out.println("Waiting for the ACK message of the connection");
            Thread.sleep(5000);
        }while(!connection);


        seqno ++;
        Path file_path = Paths.get(Path);
        byte [] data = Files.readAllBytes(file_path);
//        System.out.println("************");
//        System.out.println(Arrays.toString(data));
//        System.out.println("************");

        int packets_no = (int) Math.ceil((double)data.length/32);

        System.out.println("There are " + packets_no + " packets should be sent out");
        for(int i=0; i<packets_no; i++){
            int packet_data_length = 0;
            if(data.length < (i*32 + 31)){
                packet_data_length = data.length - i*32 ;
            }else{
                packet_data_length = 32;
            }
            byte [] packet_data = new byte[packet_data_length];


            for(int j=0; j<packet_data_length; j++){
                packet_data[j] = data[i*32+j];

            }

//            System.out.println("################");
//            System.out.println(Arrays.toString(packet_data));
//            System.out.println("################");
            Checksum checksum = new CRC32();
            checksum.update(packet_data,0,packet_data.length);
            long checksumval = checksum.getValue();
            DataMessage file_part = new DataMessage(Config.ROUTER_ID, Destination, packet_data, seqno, checksumval, "FILE_TRANSFER");
            Packet file_part_packet = new Packet(Config.ROUTER_ID,"FILE_TRANSFER",Config.Neighbors_table.get(sLSRP.routing_table.get(Destination)).Dest,file_part);
//            PacketHistory file_hist = new PacketHistory(file_part_packet,sLSRP.routing_table.get(Destination));

            long sendtime = System.currentTimeMillis();
            boolean ackrev = false;
            while(!ackrev){
//                System.out.println("Sending out the data packet with seq no " + seqno);
                sLSRP.sendPacket(file_part_packet);
                sentdatahistory.put(seqno, false);
                long curtime = System.currentTimeMillis();
                long timepassed = curtime - sendtime;
                while(timepassed < 50000){
                    curtime = System.currentTimeMillis();
                    if(sentdatahistory.get(seqno)){
//                        System.out.println("Receive the ACK for seqno "+seqno);
                        ackrev = true;
                        break;
                    }
                    timepassed = curtime - sendtime;
//
                }
//                System.out.println(timepassed + " million seconds has passed since then");
            }
            seqno++;
        }

        System.out.println("There are "+routing_records.size()+" data packets sent out");
        for(int seqkey: routing_records.keySet()){
            System.out.println(seqkey+": "+routing_records.get(seqkey));
        }

        // send out FILE_FIN packet
        do {
            // has to wait for the communication channel established.
            finComm();
            System.out.println("Waiting for the ACK message of the connection");
            Thread.sleep(5000);
        }while(!finnish);

    }


    public void startComm (){
        //clean the routing records first
        routing_records = new Hashtable<>();

        byte [] empty = new byte [0];
        Checksum check_ini = new CRC32();
        int crccode_tmp = 0;
        //the seqno here should be zero
        DataMessage syn = new DataMessage(Config.ROUTER_ID, Destination, empty, seqno, crccode_tmp, "FILE_TRANSFER_INIT");
        // will use the routing table to decide the current sending destination
        int interm_node_id = sLSRP.routing_table.get(Destination);
        System.out.println("The message is sending to router "+interm_node_id + " first whose destination host name is: "+Config.Id_Host.get(interm_node_id));
        Packet syn_packet = new Packet(Config.ROUTER_ID, "FILE_TRANSFER_INIT", Config.Neighbors_table.get(interm_node_id).Dest, syn);
        System.out.println("sending out the initialization message before transferring file");
        sLSRP.sendPacket(syn_packet);
    }

    public void finComm(){
        byte [] empty = new byte [0];
        Checksum check_ini = new CRC32();
        int crccode_tmp = 0;
        //the seqno here should be zero
        DataMessage fin = new DataMessage(Config.ROUTER_ID, Destination, empty, seqno, crccode_tmp, "FILE_FIN");
        // will use the routing table to decide the current sending destination
        int interm_node_id = sLSRP.routing_table.get(Destination);
        Packet fin_packet = new Packet(Config.ROUTER_ID, "FILE_FIN", Config.Neighbors_table.get(interm_node_id).Dest, fin);
        sLSRP.sendPacket(fin_packet);
    }

    public void receive(DataMessage m) throws Exception{
        System.out.println(seqno + "\t \t \t "+recvseqno);
        recvseqno = m.getSeqno();
        if(recvseqno == seqno+1){
            // packets have to be received in order
            byte[] filedata = m.getData();
            Checksum checksum = new CRC32();
            checksum.update(filedata, 0, filedata.length);
            long checksumval = checksum.getValue();
            if (checksumval == m.getCrc32code()) {
                System.out.println("Got a correct new data");
                if(recvseqno == 1) {
                    File file = new File("new_received.txt");
                    file.createNewFile();
                }
                FileOutputStream output = new FileOutputStream("new_received.txt", true);
                try {
                    output.write(m.getData());
                } finally {
                    output.close();
                }
                seqno = m.getSeqno();

            }else{
                System.out.println("The message has wrong data");
            }
            byte [] empyt = new byte[0];
            DataMessage dataackmes = new DataMessage(Config.ROUTER_ID,m.getSenderId(), empyt, recvseqno+1, 0, "ACK_FILE_TRANSFER");
            dataackmes.setCrossed_Path(m.getCrossed_Path()+"_"+Config.ROUTER_ID);
            Packet data_ack = new Packet(Config.ROUTER_ID, "ACK_FILE_TRANSFER",Config.Neighbors_table.get(sLSRP.routing_table.get(m.getSenderId())).Dest, dataackmes);
            sLSRP.sendPacket(data_ack);
        }

    }
}
