import java.io.Serializable;

public class DirectedEdge implements Serializable, Comparable<DirectedEdge>{
    private int src;
    private int dst;

    public DirectedEdge(int src, int dst){
        this.src = src;
        this.dst = dst;
    }

    public int getDestination(){
        return this.dst;
    }

    public int getSource(){
        return this.src;
    }

    @Override
    public String toString() {
        return this.src + " "  + this.dst;
    }

    @Override
    public int compareTo(DirectedEdge o){
        if (src <o.src){
            return -1;
        }
        else if (src == o.src){
            if (dst < o.dst){
                return -1;
            }
            else if (dst == o.dst){
                return 0;
            }
        }
        return 1;
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result + dst;
        result = prime * result + src;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        DirectedEdge other = (DirectedEdge) obj;
        if (dst != other.dst) return false;
        if (src != other.src) return false;
        return true;
    }

    public long edge2long(){
        long result = (((long)src) << 32) | (dst & 0xffffffffL);
        return result;
    }

}