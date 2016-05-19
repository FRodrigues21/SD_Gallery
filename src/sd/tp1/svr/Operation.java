package sd.tp1.svr;

/**
 * Created by franciscorodrigues on 19/05/16.
 */
public class Operation implements Comparable<Operation> {

    private long id;
    private long cnt;
    private String event;

    public Operation(long cnt, long id, String event) {
        this.cnt = cnt;
        this.id = id;
        this.event = event;
    }

    public String getEvent() {
        return event;
    }

    public long getCnt() {
        return cnt;
    }

    public long getId() {
        return id;
    }

    @Override
    public int compareTo(Operation o) {
        if(this.getCnt() < o.getCnt() || (this.getCnt() == o.getCnt() && this.getId() < this.getId()))
            return 1;
        else if(this.getCnt() > o.getCnt())
            return -1;
        return 0;
    }
}
