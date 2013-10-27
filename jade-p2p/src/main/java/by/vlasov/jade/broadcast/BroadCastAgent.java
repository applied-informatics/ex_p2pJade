package by.vlasov.jade.broadcast;

import java.util.LinkedList;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

public class BroadCastAgent extends Agent {
    private LinkedList<Integer> neigh; // ids of neighbours
    private int [] mCount; // number of messages over branch
    private boolean [] mWas;
    private final int target = 0;
    private int recieved = 1;
    public void setup () {
        neigh = (LinkedList<Integer>)getArguments()[0];
        setEnabledO2ACommunication (true, 1);
        Behaviour fetchBehaviour = new Behaviour () { // TODO to separate class
            private boolean fetched = false;
            public void action() {
                mCount = (int [])getO2AObject();
                if (mCount != null)
                {   
                    fetched = true;
                    mWas = new boolean [mCount.length];
                    for (int i = 0 ; i<mWas.length; i++)
                        mWas[i] = false;
                    mWas[Integer.parseInt(myAgent.getName().split("@",0)[0])] = true;
                    myAgent.addBehaviour (new OneShotBehaviour () {
                        public void action () {
                            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                            msg.setLanguage ("English");
                            msg.setOntology ("Node");
                            msg.setContent (""+target);
                            msg.setSender (myAgent.getAID());
                            msg.setConversationId (myAgent.getAID().getName().split("@",0)[0]);
                            for (Integer i : neigh) {
                                msg.addReceiver(new AID(""+i, AID.ISLOCALNAME));
                                mCount[i.intValue()] ++;
                            }
                            myAgent.send(msg);
                            System.out.println (myAgent.getName () + " send message.");
                        }
                    });
                    myAgent.addBehaviour (new CyclicBehaviour () {
                        public void action () {
                            ACLMessage msg = myAgent.receive();
                            if (msg != null) {
                                int me = Integer.parseInt (myAgent.getName().split("@",0)[0]);
                                int mt = Integer.parseInt (msg.getContent ());
                                int from = Integer.parseInt (msg.getConversationId());
                                int edge = Integer.parseInt (msg.getSender().getName().split("@",0)[0]);
                                System.out.println (myAgent.getName() + " received message from "+from);
                                mCount[edge] ++;
                                if (!mWas[from]) {
                                    mWas[from] = true;
                                    if ( me != target) {
                                        msg.setSender (myAgent.getAID());
                                        msg.clearAllReceiver ();
                                        for (Integer i : neigh) {
                                            if (i.intValue() != edge)
                                                msg.addReceiver(new AID (""+i, AID.ISLOCALNAME));
                                        }
                                        myAgent.send(msg);
                                        System.out.println (myAgent.getName() + " resend message from "+from);

                                    }
                                }
                                boolean exit = true;
                                for (boolean was : mWas)
                                    if (!was) exit = false;
                                if (exit) { 
                                    //System.out.println (myAgent.getName() + " is dying.");
                                    //doDelete();
                                }
                            }
                            else block();
                        } 
                    });
                } else block();
            }
            public boolean done () {
                return fetched;
            }
        };
        addBehaviour (fetchBehaviour);
        setO2AManager (fetchBehaviour);
    }
}
