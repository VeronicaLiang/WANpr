import java.io.Serializable;

/**
 * recording links of each router.
 */
public class Links implements Serializable{
    private static final long serialVerisonUID = 1L;
    public int source;
    public int destination;
    public double cost = 0;
    public boolean active;

    public Links(int source, int destination, double cost){
        this.source = source;
        this.destination = destination;
        this.cost = cost;
    }

//    public void setActive (boolean val){
//        this.active = val;
//    }

    public String toString(){
        return source + " " + destination + " " + cost;
    }
}
