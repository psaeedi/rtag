/**
 * 
 */
package it.polimi.rtag.app.operator;



import java.util.ArrayList;
import java.util.HashMap;

import it.polimi.rtag.Node;

/**
 * @author pani
 *
 */
public class AppNode extends Node{
	
	ArrayList<AppNode> nodes = new ArrayList<AppNode>();
	ArrayList<String> urls = new ArrayList<String>();
	
	HashMap<String, AppNode> masterofexistingGroups = new HashMap<String, AppNode>();
	HashMap<String, AppNode> memebrofexistingGroups = new HashMap<String, AppNode>();
	
	private int NUMBER_OF_NODES;
	String localhost = "localhost";
	int localPort= 2012;
	
	private AppNode node;
	private MasterBehaviors masterBehaviors; 
	private SlaveBehaviors slaveBehaviors; 
	
    public AppNode(String host, int port) {
		super(host, port);
		this.node = node;
	}

	//send the host and port, till the node starts
	//then what group should it join!
	
	//do we need to send the port or can it be store here??
	public void setUp(String host , int port, String groupFriendlyName){
		if(nodes.isEmpty()){
			NUMBER_OF_NODES = -1;
		}
		AppNode node = new AppNode(host, port);
		node.start();
		node.joinGroup(groupFriendlyName);
		//store the nodes to create a network
		nodes.add(node);
		memebrofexistingGroups.put(groupFriendlyName, node);
		urls.add("reds-tcp:"+ host + ":" + port);
		NUMBER_OF_NODES ++;
		createNetworkByAddingToNewAddedNode();
	}
	
	//automatically give port and local host
	public void setUp(String groupFriendlyName){
		if(nodes.isEmpty()){
			NUMBER_OF_NODES = -1;
		}
			AppNode node = new AppNode(localhost, localPort);
			node.start();
			localPort ++;
			node.joinGroup(groupFriendlyName);
			nodes.add(node);
			urls.add("reds-tcp:"+ localhost + ":" + localPort);
			NUMBER_OF_NODES ++;
			createNetworkByAddingToNewAddedNode();
	}

	
	private void createNetworkByAddingToNewAddedNode() {
		for (int i = 0; i < NUMBER_OF_NODES; i++) {
    		System.out.println("***Adding neighbor :" + NUMBER_OF_NODES + 
    				" to node " + nodes.get(i-1));
    		try {
    			//is adding both way??
				nodes.get(i).getOverlay().addNeighbor(urls.get(NUMBER_OF_NODES));
				Thread.sleep(1500);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
	
	public void removeFromGroup(String groupFriendlyName){
		node.leaveGroup(groupFriendlyName);
	}
	
	public void joinToNewGroup(String groupFriendlyName){
		memebrofexistingGroups.put(groupFriendlyName, node);
		node.joinGroup(groupFriendlyName);
	}
	
	public AppNode getMaster(String friendlyName) {
		AppNode masternode = masterofexistingGroups.get(friendlyName);
		return masternode;
	}

	
	public void setMaster(String friendlyName) {
		masterofexistingGroups.put(friendlyName, node);
	}
	
    public void getBehaviors(String groupFriendlyName, boolean activemaster, 
    		boolean activeslave){
    	
    	if(activemaster == true){
    	masterBehaviors.setBehavior();
    	}
    	else if(node.isMaster(groupFriendlyName)){
    		throw new RuntimeException("the node is set to be a master " +
    				"but its behavior is deactivated" );
    	}
    	
    	if(activeslave == true){
    	slaveBehaviors.setBehavior();
    	}
    	
    	else if(!node.isMaster(groupFriendlyName)){
    		throw new RuntimeException("the node is set to be a slave " +
    				"but its behavior is deactivated" );
    	}
	}

	private boolean isMaster(String groupFriendlyName) {
		AppNode master =  getMaster(groupFriendlyName);
		if(node == master){
		   return true;
		}
		return false;
	}
	
	public int getNumberofNodes(String groupFriendlyName){
		
		return memebrofexistingGroups.size()-1;
	}
	
	
}
