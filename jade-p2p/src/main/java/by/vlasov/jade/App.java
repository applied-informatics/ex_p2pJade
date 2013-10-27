package by.vlasov.jade;

import java.util.LinkedList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;
import by.vlasov.jade.util.AgentParams;
import java.util.Arrays;

public class App 
{
    public static void main( String[] args ) throws ClassNotFoundException, InterruptedException
    {
        Class.forName("org.sqlite.JDBC");
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:../graph.db");
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM graphs");
            rs.next();
            String[] nodes = rs.getString("nodes").split(";",0);
            int id = rs.getInt("id");
            AgentParams [] params = genArgs (rs.getString("edges"), nodes.length);
            AgentController [] agents = new AgentController [nodes.length];
            Runtime rt = Runtime.instance();
            rt.setCloseVM(true);
            Profile p = new ProfileImpl();
            ContainerController cc = rt.createMainContainer(p);
            try {
                for (int i = 0 ;i<params.length; i++) {
                    Object [] arg = new Object[1];
                    arg[0] = params[i].getConnections();
                    agents[i] = cc.createNewAgent(nodes[i].split(",",0)[0], "by.vlasov.jade.broadcast.BroadCastAgent", arg);
                    agents[i].start();    
                }
                for (int i = 0; i<params.length; i++) {
                    agents[i].putO2AObject(params[i].getMessageCount(), false);
                }
                boolean weAreOnTheWay = true;
                while (weAreOnTheWay) {
                    Thread.sleep(1000);
                    weAreOnTheWay = false;
                    for (AgentController a : agents)
                        try {
                        if (a.getState().getCode() ==  AgentState.cAGENT_STATE_ACTIVE)
                            weAreOnTheWay = true;
                        } catch (StaleProxyException e) {
                        }
                }
                Thread.sleep(1000);
                for (int i = 0; i < params.length; i++)
                    System.out.println(Arrays.toString(params[i].getMessageCount()));
                recordMessageCount (conn, params, id);
                rt.shutDown();
                cc.kill();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }
    private static AgentParams [] genArgs (String es, int n) {
        String [] edges = es.split(";",0);
        LinkedList<Integer> [] conns = 
                (LinkedList<Integer>[]) new LinkedList[n];
        for (int i = 0; i< n; i++)
            conns[i] = new LinkedList<Integer>();
        AgentParams [] args = new AgentParams [n];
        int [][] mCount = new int [n][n];
        for (int i = 0; i < edges.length; i++) {
            String[] e = edges[i].split(",", 0);
            int [] edge = new int [e.length];
            for (int j = 0 ; j < e.length; j++)
                edge[j] = Integer.parseInt (e[j]);
            conns[edge[0]].add(edge[1]);
            conns[edge[1]].add(edge[0]);
            mCount[edge[0]][edge[1]] = edge[2];
            mCount[edge[1]][edge[0]] = edge[2];
        }
        for (int i = 0; i< n; i++)
            args[i] = new AgentParams (conns[i], mCount[i]);
        return args;
    }
    private static void recordMessageCount (Connection conn, AgentParams [] params, int id) throws SQLException {
        String nodes="";
        String edges="";
        for (int i = 0; i < params.length; i++) {
            nodes += ""+i+","+i+";";
            int [] pars = params[i].getMessageCount();
            for (int j = i; j < pars.length; j++){
                if (pars[j] != 0)
                    edges += ""+ i +","+j+","+pars[j]+";";
            }
        }
        Statement s = conn.createStatement();
        s.executeUpdate ("INSERT INTO tests (test, nodes, edges, graph) VALUES (\"broadcast\",\""+nodes.substring(0, nodes.length()-1)+"\", \""+edges.substring(0,edges.length()-1)+"\","+id+");");
    }
}
