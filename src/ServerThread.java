
import java.net.*;
import java.io.*;
import java.io.ObjectInputStream;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * The server side of the router.
 */

// todo send back acknowledgement .
public class ServerThread implements Runnable {
    private static final int BUFSIZE = 32;
    private static final int WINDOWSIZE = 10;

    public static boolean running = true;


    public void run() {

        try {
            ServerSocket servSock = new ServerSocket(Config.SERV_PORT);
//            int recvMsgSize;   // Size of received message
//            byte[] byteBuffer = new byte[BUFSIZE];  // Receive buffer


//            while(!sLSRP.Failure){
            while(running){
                Socket clntSock = servSock.accept();
                try {
                    ObjectInputStream inputstream = new ObjectInputStream(clntSock.getInputStream());
                    Packet recv = (Packet) inputstream.readObject();
                    String packet_type = recv.getType();


                    if(sLSRP.Failure){
                       continue; // ignore all packets
                    }

                    if(sLSRP.faillist.contains(recv.getId())){
                        continue; // if the packet is from filed link, ignore it.
                    }
//                    double dropval = Math.random();
////                    System.out.println("Receive Packet "+packet_type+"  "+dropval);
//                    if(dropval < Config.DROP_RATE){
////                        System.out.println("Packet is dropped due to the congestion ");
//                        inputstream.close();
//                        clntSock.close();
//                        continue;
//                    }
//
//                    double errorval = Math.random();
//                    if(errorval < Config.ERROR_RATE){
//                        inputstream.close();
//                        clntSock.close();
//                        continue;
//                    }

//                    System.out.println("Receive a Packet with seqno: "+recv.getSeqno() +" with type of "+recv.getType());
                    int ack_seqno = recv.getSeqno()+1;
//
                    switch (packet_type) {
                        case "NEIGHBOR_REQUEST":
//                          System.out.println("NEIGHBOR REQUEST RECEIVED from "+recv.getId());
                            int request_id = recv.getId();
                            Neighbors check = Config.Neighbors_table.get(request_id);
                            if(check != null){
                                System.out.println("send back to "+check.Dest);
                                Packet acc = new Packet(Config.ROUTER_ID,"ACK_NEIGH", check.Dest,ack_seqno);

                                sLSRP.sendPacket(acc);

                            }
                            clntSock.close();
                            break;
                        case "ACK_NEIGH":
//                            System.out.println("Establish Neighbor Relationship");
                            if (!sLSRP.Established_Connect.containsKey(recv.getId())) {
                                synchronized (sLSRP.Established_Connect) {
                                    sLSRP.Established_Connect.put(recv.getId(), 0);
                                }
                            }
                            String link_key = Config.ROUTER_ID + "_" + recv.getId();
                            // Temporarily the directed connected neighbors have cost of 1
                            Links tmp = new Links(Config.ROUTER_ID, recv.getId(), 1);
                            synchronized (sLSRP.links) {
                                sLSRP.links.put(link_key, tmp);
                            }
//                            System.out.println("adding link "+link_key);
                            if(sLSRP.Established_Connect.size() == Config.Neighbors_table.size()){
                                NeighborRequThread.sendflag = false;
                                System.out.println("All Connections Have Been Built for Every Neighbor");
                            }
                            clntSock.close();
                            break;
                        case "ALIVE_MESSAGE":
//                            if (Config.Established_Connect.containsKey(recv.getId())) {
                            Packet neighbor_ack = new Packet(Config.ROUTER_ID, "ACK_ALIVE", Config.Neighbors_table.get(recv.getId()).Dest, ack_seqno);
                            sLSRP.sendPacket(neighbor_ack);
//                            }
                            clntSock.close();
                            break;
                        case "ACK_ALIVE":
                            int ackno = recv.getSeqno() - 1;
                            if(AliveMessageThread.alivesenthistory.containsKey(ackno)) {
                                synchronized (AliveMessageThread.alivesenthistory) {
                                    AliveMessageThread.alivesenthistory.get(ackno).setAck(true);
                                }
                            }
                            clntSock.close();
                            break;
                        case "LSA_MESSAGE":
                            LSAMessage receivelsa = recv.getLSA();
                            int lsaackno = receivelsa.getSeqno() + 1;
//                            System.out.println("*******");
//                            System.out.println("Receive LSA Message from router "+recv.getId()+" "+Config.Neighbors_table.get(recv.getId()).Dest );
//                            System.out.println("ACK_SEQ_NO is : "+lsaackno);
//                            System.out.println("*******");
                            Packet lsa_ack = new Packet(Config.ROUTER_ID, "ACK_LSA", Config.Neighbors_table.get(recv.getId()).Dest,lsaackno);
                            sLSRP.sendPacket(lsa_ack);
                            Runnable lsadb = new LSADatabaseThread(recv);
                            new Thread(lsadb).start();
                            clntSock.close();
                            break;
                        case "RTT_ANALYSIS":
                            Packet rtt_ack = new Packet(Config.ROUTER_ID, "ACK_RTT", Config.Neighbors_table.get(recv.getId()).Dest,ack_seqno);
                            sLSRP.sendPacket(rtt_ack);
                            clntSock.close();
                            break;
                        case "ACK_RTT":
                            int ackrttno = recv.getSeqno() - 1;
//                            System.out.println("Received the ACK_RTT message from Router_"+ recv.getId()+" with seqno "+recv.getSeqno());
//                            System.out.println(recv.getSeqno() + " \t " + ackrttno);

                            PacketHistory rttcheckone = RTTAnalysis.rttsenthistory.get(ackrttno);
                            if(rttcheckone != null){
                                synchronized (RTTAnalysis.rttsenthistory){
                                    RTTAnalysis.rttsenthistory.get(ackrttno).setAck(true);
                                }
                                long curtime = System.currentTimeMillis();
                                long time = curtime - rttcheckone.getSendtime();
//                                System.out.println("ACK_RTT for seq_no "+ ackrttno +" received in "+time+" million seconds");
                                String updatekey = Config.ROUTER_ID+"_"+rttcheckone.getDest_id();
                                synchronized (sLSRP.links){
                                    double prev_cost = sLSRP.links.get(updatekey).cost;
                                    sLSRP.links.get(updatekey).cost = (prev_cost + time) / 2;
                                    sLSRP.links.get(updatekey).active = true;
                                    sLSRP.links.get(updatekey).last_update = System.currentTimeMillis();
                                }
                            }
                            clntSock.close();
                            break;
                        case "FAILURE_LSA":
                            LSAMessage receivefaillsa = recv.getLSA();
                            int ackfaillsa = receivefaillsa.getSeqno() + 1;
                            PacketHistory faillsacheckone = LSAThread.faillsasenthistory.get(ackfaillsa);
//                            System.out.println("Receive the LSA Failure Message from "+ recv.getId());
                            Packet faillsa_ack = new Packet(Config.ROUTER_ID, "ACK_FAIL_LSA", Config.Neighbors_table.get(recv.getId()).Dest,ackfaillsa);
                            sLSRP.sendPacket(faillsa_ack);
                            Runnable faillsadb = new LSADatabaseThread(recv);
                            new Thread(faillsadb).start();
                            clntSock.close();
                            break;
                        case "ACK_LSA":
                            int acklsano = recv.getSeqno() - 1;
//                            System.out.println("Server Receive ACK_LSA for "+acklsano + "!!!!!");
                            PacketHistory lsacheckone = LSAThread.lsasenthistory.get(acklsano);
                            if(lsacheckone != null){
                                synchronized (LSAThread.lsasenthistory){
                                    LSAThread.lsasenthistory.get(acklsano).setAck(true);
                                }
                            }
                            break;
                        case "ACK_FAIL_LSA":
                            int ackfaillsano = recv.getSeqno() - 1;
//                            System.out.println("Receive the ACK for Fail LSA for seqno "+ ackfaillsano);
                            PacketHistory faillsacheck = LSAThread.faillsasenthistory.get(ackfaillsano);
                            if(faillsacheck != null){
                                synchronized (LSAThread.faillsasenthistory){
                                    LSAThread.faillsasenthistory.get(ackfaillsano).setAck(true);
                                }
                            }
                            break;
                        case "FILE_TRANSFER_INIT":
                            System.out.println("Router "+ Config.ROUTER_ID+" Receive the FILE_TRANSFER_INIT packet");
                            //check whether this is the destination, if not, forward it according to routing table
                            DataMessage just_recv = recv.getFileMessage();
                            int final_dest_id = just_recv.getRecvId();
                            int sender_id = just_recv.getSenderId();
                            String appending_path = "_"+Config.ROUTER_ID;
                            just_recv.appendPath(appending_path);
                            if(Config.ROUTER_ID == final_dest_id){
//                                System.out.println("This is the final destination of the file transfer");
                                // This is the destination, send back the ACK Message
//                                Checksum tmp_checksum = new CRC32();
                                byte [] tmp_barray = new byte[0];
                                DataMessage ack_init = new DataMessage(Config.ROUTER_ID, sender_id, tmp_barray,just_recv.getSeqno()+1,0, "ACK_FILE_INIT");
                                ack_init.setPath(just_recv.getCrossed_Path());
                                Scanner sc_tf = new Scanner(System.in);
//                                System.out.println("Receive File Transfer Request, Please Input A File Name:");
//                                String savefile = sc_tf.nextLine();
                                String savefile = "new_receive.txt";
                                sLSRP.application = new EndSystem(Config.ROUTER_ID, sender_id, savefile);
                                Packet ack_file_init = new Packet(Config.ROUTER_ID, "ACK_FILE_INIT", Config.Neighbors_table.get(sLSRP.routing_table.get(sender_id)).Dest, ack_init);
                                sLSRP.sendPacket(ack_file_init);

                            }else{
                                System.out.println("Will forward the packet to next node "+ Config.Id_Host.get(sLSRP.routing_table.get(final_dest_id)));
                                // forward the message to next router
                                Packet forward = new Packet(Config.ROUTER_ID, "FILE_TRANSFER_INIT", Config.Neighbors_table.get(sLSRP.routing_table.get(final_dest_id)).Dest, just_recv);
                                sLSRP.sendPacket(forward);
                            }
                            break;
                        case "ACK_FILE_INIT":
                            DataMessage just_recv_ack = recv.getFileMessage();
                            int final_ack_dest_id = just_recv_ack.getRecvId();
                            just_recv_ack.appendPath("_"+Config.ROUTER_ID);
                            if(Config.ROUTER_ID == final_ack_dest_id){
                                System.out.println("Receive the ACK_FILE_INIT message.");
                                sLSRP.application.connection = true;
//                                System.out.println(just_recv_ack.getCrossed_Path());
                            }else{
//                                System.out.println("Will forward the packet to next node "+ Config.Neighbors_table.get(sLSRP.routing_table.get(final_ack_dest_id)).Dest);
                                // forward the message to next router
                                Packet forward = new Packet(Config.ROUTER_ID, "ACK_FILE_INIT", Config.Neighbors_table.get(sLSRP.routing_table.get(final_ack_dest_id)).Dest, just_recv_ack);
                                sLSRP.sendPacket(forward);
                            }
                            break;
                        case "FILE_TRANSFER":
                            DataMessage filerecv = recv.getFileMessage();
                            int file_dest_id = filerecv.getRecvId();
                            filerecv.appendPath("_"+Config.ROUTER_ID);
                            if(Config.ROUTER_ID == file_dest_id){
                                System.out.println("Receive the FILE_TRANSFER message.");
                                sLSRP.application.receive(filerecv);
                            }else{
                                // forward to next node
                                System.out.println("Will forward the FILE_TRANSFER to next node ");
                                Packet forward = new Packet(Config.ROUTER_ID, "FILE_TRANSFER", Config.Neighbors_table.get(sLSRP.routing_table.get(file_dest_id)).Dest, filerecv);
                                sLSRP.sendPacket(forward);
                            }
                            break;
                        case "ACK_FILE_TRANSFER":
                            DataMessage fileack = recv.getFileMessage();
                            int fileack_dest = fileack.getRecvId();
                            fileack.appendPath("_"+Config.ROUTER_ID);
                            if(Config.ROUTER_ID == fileack_dest){
                                sLSRP.application.sentdatahistory.put((fileack.getSeqno() - 1), true);
                                sLSRP.application.routing_records.put((fileack.getSeqno() - 1), fileack.getCrossed_Path());
//                                System.out.println("One data packet has been received " + fileack.getCrossed_Path());
                            }else{
                                Packet fileack_forward = new Packet(Config.ROUTER_ID, "ACK_FILE_TRANSFER", Config.Neighbors_table.get(sLSRP.routing_table.get(fileack_dest)).Dest, fileack);
                                sLSRP.sendPacket(fileack_forward);
                            }
                            break;
                        case "FILE_FIN":
                            DataMessage filefin = recv.getFileMessage();
                            int filefin_dest = filefin.getRecvId();
                            int filefin_sender = filefin.getSenderId();
                            filefin.appendPath("_"+Config.ROUTER_ID);
                            if(Config.ROUTER_ID == filefin_dest){
                                byte [] tmp_barray = new byte[0];
                                DataMessage ack_fin = new DataMessage(Config.ROUTER_ID, filefin_sender, tmp_barray,filefin.getSeqno()+1,0, "ACK_FILE_FIN");
                                ack_fin.setPath(filefin.getCrossed_Path());
                                Packet ack_file_init = new Packet(Config.ROUTER_ID, "ACK_FILE_FIN", Config.Neighbors_table.get(sLSRP.routing_table.get(filefin_sender)).Dest, ack_fin);
                                sLSRP.sendPacket(ack_file_init);
                                System.out.println("file transfer is done");
                            }else{
                                Packet filefin_forward = new Packet(Config.ROUTER_ID, "FILE_FIN", Config.Neighbors_table.get(sLSRP.routing_table.get(filefin_dest)).Dest, filefin);
                                sLSRP.sendPacket(filefin_forward);
                            }
                            break;
                        case "ACK_FILE_FIN":
                            DataMessage filefinack = recv.getFileMessage();
                            int filefinack_dest = filefinack.getRecvId();
                            filefinack.appendPath("_"+Config.ROUTER_ID);
                            if(Config.ROUTER_ID == filefinack_dest){
                                sLSRP.application.finnish = true;
                                System.out.println("file transfer is done");
                            }else{
                                Packet fileack_forward = new Packet(Config.ROUTER_ID, "ACK_FILE_FIN", Config.Neighbors_table.get(sLSRP.routing_table.get(filefinack_dest)).Dest, filefinack);
                                sLSRP.sendPacket(fileack_forward);
                            }
                            break;

                    }
                    inputstream.close();

                }catch (UnknownHostException ex) {
                    // ex.printStackTrace();
                } catch (ConnectException e) {
                    // e.printStackTrace();
                } catch (IOException e) {
                    // e.printStackTrace();
                }

            }
            servSock.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("Server is finished job");
    }

    public void kill(){
        running = false;
    }

    public void recover(){
        running = true;
    }
}
