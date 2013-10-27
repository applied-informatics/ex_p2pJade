package by.vlasov.jade.util;

import java.util.LinkedList;

public class AgentParams {
    private LinkedList<Integer> conns;
    private int[] mCount;
    public AgentParams (LinkedList<Integer> c, int[] m) {
        conns = c; mCount = m;
    }
    public LinkedList<Integer> getConnections () {
        return conns;
    }
    public int[] getMessageCount () {
        return mCount;
    } 
}
