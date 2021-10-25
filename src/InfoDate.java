package src;

import java.time.*;

public class InfoDate{
    public LocalDate date;
    public int numEdges;
    public int node;
    public long[] graphlets;
    public InfoDate(LocalDate date, int node, int numEdges, long[] graphlets){
        this.date = date;
        this.node = node;
        this.numEdges = numEdges;
        this.graphlets = new long[30];
        for(int i = 0 ; i < 30; i++){
            this.graphlets[i] = graphlets[i];
        }
    }
}