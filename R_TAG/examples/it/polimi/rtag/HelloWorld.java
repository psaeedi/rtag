package it.polimi.rtag;

import polimi.reds.NodeDescriptor;
import it.polimi.rtag.filters.*;
import it.polimi.rtag.messaging.*;

public class HelloWorld {

	  public static void main(String args[]) throws Exception {
		    int localPort1 = 10001;
		    int localPort2 = 10002;
		    int localPort3 = 10003;
		    String host = "localhost";

		    // start
		    Node node1 = new Node(host, localPort1);
		    Node node2 = new Node(host, localPort2);
		    Node node3= new Node(host, localPort3);
		    
		    node1.start();
		    node2.start();
		    node3.start();
		    
		    node1.getOverlay().addNeighbor("reds-tcp:"+ host + ":" + localPort2);
		    node1.getOverlay().addNeighbor("reds-tcp:"+ host + ":" + localPort3);
		    
		    System.out.println("Going to sleep");
		    // wait forever
		    synchronized(HelloWorld.class) {
		    	HelloWorld.class.wait();
		    }
	  }
}
     








