/**
 * 
 */
package it.polimi.rtag.app.operator;

import it.polimi.rtag.Node;

import java.util.ArrayList;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 * Simulates the network by creating several nodes o different ports.
 */
public class Launcher {

	private int numNodes;
	private int localPort;
	private String host;
	
	private ArrayList<Node> nodes = new ArrayList<Node>();
	ArrayList<String> urls = new ArrayList<String>();
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Launcher launcher = new Launcher(10, 40000, "localhost");
		launcher.initialize("RED");
	}
	
	private Launcher(int numNodes, int initialPort, String host) {
		this.numNodes = numNodes;
		this.localPort = initialPort;
		this.host = host;
		
	}
	
	private void initialize(String groupName) {
		for (int i = 0; i < numNodes; i++) {
			ExampleRedNode node = new ExampleRedNode(host, localPort);
			nodes.add(node);
			urls.add("reds-tcp:"+ host + ":" + localPort);
			localPort++;
			if (i == 0) {
				node.setMaster();
			}
			node.setBehaviors(true, true);
			node.start();
		}
		createNetworkByAddingToTheLastAdded();
	}
	
	private void createNetworkByAddingToTheLastAdded() {
    	for (int i = 1; i < numNodes; i++) {
    		System.out.println("************Adding neighbor " + i + " to node " + nodes.get(i-1));
    		try {
				nodes.get(i-1).addNeighbor(urls.get(i));
				Thread.sleep(1500);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    }
	
}
