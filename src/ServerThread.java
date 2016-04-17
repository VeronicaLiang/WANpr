import java.net.*;
import java.io.*;
import java.io.ObjectInputStream;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The server side of the router.
 */

// todo send back acknowledgement .
public class ServerThread implements Runnable {
    private static final int BUFSIZE = 32;
    private static final int WINDOWSIZE = 10;

    public void run() {

        try {
            ServerSocket servSock = new ServerSocket(Config.SERV_PORT);
            int recvMsgSize;   // Size of received message
            byte[] byteBuffer = new byte[BUFSIZE];  // Receive buffer

            while(!sLSRP.Failure) { // Run forever, accepting and servicing connections
                Socket clntSock = servSock.accept();     // Get client connection

//                System.out.println("Handling client at " +
//                        clntSock.getInetAddress().getHostAddress() + " on port " +
//                        clntSock.getPort());

                try {
                    ObjectInputStream inputstream = new ObjectInputStream(clntSock.getInputStream());
                    Packet recv = (Packet) inputstream.readObject();
                    String packet_type = recv.getType();

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
//                          System.out.println("NEIGHBOR REQUEST RECEIVED");
//                            Runnable ser = new NeighborRequACKThread(recv);
//                            new Thread(ser).start();
                            int request_id = recv.getId();
                            Neighbors check = Config.Neighbors_table.get(request_id);
                            if(check != null){
                                Packet acc = new Packet(Config.ROUTER_ID,"ACK_NEIGH", check.Dest,ack_seqno);
                                sLSRP.sendPacket(acc);
                            }
                            clntSock.close();
                            break;
                        case "ACK_NEIGH":
//                          System.out.println("Establish Neighbor Relationship");
                            if (!Config.Established_Connect.containsKey(recv.getId())) {
                                synchronized (Config.Established_Connect) {
                                    Config.Established_Connect.put(recv.getId(), 0);
                                }
                            }
                            String link_key = Config.ROUTER_ID + "_" + recv.getId();
                            // Temporarily the directed connected neighbors have cost of 1
                            Links tmp = new Links(Config.ROUTER_ID, recv.getId(), 1);
                            synchronized (sLSRP.links) {
                                sLSRP.links.put(link_key, tmp);
                            }
                            if(Config.Established_Connect.size() == Config.Neighbors_table.size()){
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
                            System.out.println("Receive the LSA Failure Message from "+ recv.getId());
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
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
