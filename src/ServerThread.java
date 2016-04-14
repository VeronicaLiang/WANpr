import java.net.*;
import java.io.*;
import java.io.ObjectInputStream;
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
//                  double random = ThreadLocalRandom.current().nextDouble(0, 1);
//                  if(random <= Config.DROP_RATE){
//                      inputstream.close();
//                      clntSock.close();
//                      continue;
//                  }
//                    System.out.println("Receive a Packet with seqno: "+recv.getSeqno() +" with type of "+recv.getType());
                    int ack_seqno = recv.getSeqno()+1;
//                    System.out.println("ACK_SEQ_NO is : "+ack_seqno);
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
                            if(AliveMessageThread.senthistory.containsKey(ackno)) {
                                synchronized (AliveMessageThread.senthistory) {
                                    AliveMessageThread.senthistory.get(ackno).setAck(true);
                                }
                            }
                            clntSock.close();
                            break;
                        case "LSA_MESSAGE":
//                          System.out.println("Receive LSA Message");
                            Runnable lsadb = new LSADatabaseThread(recv);
                            new Thread(lsadb).start();
                            clntSock.close();
                            break;
                        case "RTT_ANALYSIS":
                            Packet rtt_ack = new Packet(Config.ROUTER_ID, "ACK_RTT", Config.Neighbors_table.get(recv.getId()).Dest,ack_seqno);
                            sLSRP.sendPacket(rtt_ack);
                            clntSock.close();
//                          OutputStream out = clntSock.getOutputStream();
//                          String ack = "ACK_RTT";
//                          out.write(ack.getBytes());
//                          clntSock.close();
                            break;
                        case "ACK_RTT":
//                            System.out.println("Received the ACK_RTT message");
                            int ackrttno = recv.getSeqno() - 1;
//                            System.out.println(recv.getSeqno() + " \t " + ackrttno);

                            if(RTTAnalysis.senthistory.get(ackrttno) != null){
                                synchronized (RTTAnalysis.senthistory){
                                    RTTAnalysis.senthistory.get(ackrttno).setAck(true);
                                }
                                long curtime = System.currentTimeMillis();
                                long time = curtime - RTTAnalysis.senthistory.get(ackrttno).getSendtime();
                                synchronized (sLSRP.links){
                                    double prev_cost = sLSRP.links.get(Config.ROUTER_ID+"_"+RTTAnalysis.senthistory.get(ackrttno).getDest_id()).cost;
                                    sLSRP.links.get(Config.ROUTER_ID+"_"+RTTAnalysis.senthistory.get(ackrttno).getDest_id()).cost = (prev_cost + time) / 2;
                                    sLSRP.links.get(Config.ROUTER_ID+"_"+RTTAnalysis.senthistory.get(ackrttno).getDest_id()).active = true;
                                }
                            }
                            clntSock.close();
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
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
