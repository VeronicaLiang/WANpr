/**
 * The LSA Database Object.
 */
public class LSADatabase {
    public String linkid;
    public int adv_router;
    public int age;
    public long seqno;
    public long checksum;
    public int linkcounts;
    public long createdtime;

    public void fromLSAMessage(LSAMessage a){
        this.linkid = a.getLinkID();
        this.adv_router = a.getAdvertising_Id();
        this.linkcounts = a.getLinkArray().size();
        this.seqno = a.getSeqno();
        this.createdtime = a.getTime_created();
    }


}
