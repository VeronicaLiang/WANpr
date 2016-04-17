/**
 * The LSA Database Object.
 */
public class LSADatabase {
    public String linkid;
    public int adv_router;
    public int age;
    public int seqno;
    public long checksum;
    public int linkcounts;
    public long createdtime;
    private boolean adv_router_reachable = true;

    public void fromLSAMessage(LSAMessage a){
        this.linkid = a.getLinkID();
        this.adv_router = a.getAdvertising_Id();
        this.linkcounts = a.getLinkArray().size();
        this.seqno = a.getSeqno();
        this.createdtime = a.getTime_created();
        this.linkcounts = a.getLinkCount();
    }

    public void changeReachable (boolean val ){
        this.adv_router_reachable = val;
    }

}
