package it.polimi.rtag.hospital;

import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import polimi.reds.broker.overlay.AlreadyNeighborException;
import polimi.reds.broker.overlay.NotRunningException;
import it.polimi.rtag.GroupCommunicationDispatcher;
import it.polimi.rtag.GroupCommunicationManager;
import it.polimi.rtag.GroupDescriptor;
import it.polimi.rtag.MessageCountingGenericOverlay;
import it.polimi.rtag.Node;


public class HospitalJunction {
	
	
	public enum IntelligentJunction {
		ONE,TWO,THREE,FOUR,FIVE 
    }
	
	private Node node;

	
	ArrayList<Node> nodes = new ArrayList<Node>();
	
	/* Junction one is the main entrance, however, there are
	 * other auxiliary junctions
	 */
	private IntelligentJunction currentJunction = IntelligentJunction.ONE;
	
	
	
	public HospitalJunction(String host, int port, IntelligentJunction currentJunction) {
	    node = new Node(host, port);
		node.start();
		setCurrentJunction(currentJunction);
	}

	public IntelligentJunction getCurrentJunction() {
		return currentJunction;
	}

	public void setCurrentJunction(IntelligentJunction currentJunction) {
		
		this.currentJunction = currentJunction;
	}
	
	protected void joinGroup(IntelligentJunction currentJunction){
		node.joinGroup(currentJunction.toString());			
	}
	
	protected void leaveGroup(IntelligentJunction currentJunction){
		node.leaveGroup(currentJunction.toString());	
	}

	public void connectNeighbor(String urls) {
		try {
			node.addNeighbor(urls);
		} catch (AlreadyNeighborException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConnectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotRunningException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	 public void writeToFile(String label, PrintWriter pw) {
	    	HashSet<String> sentSubjects = new HashSet<String>();
	    	HashSet<String> receivedSubjects = new HashSet<String>();
	    	
	    	HashMap<String, Integer> map = null;
	    	for (Node node: nodes) {
	    		MessageCountingGenericOverlay overlay = (MessageCountingGenericOverlay)node.getOverlay();
	    		map = overlay.getSentMessages();
	    		for (String subject: map.keySet()) {
	    			sentSubjects.add(subject);
	    		}
	    		map = overlay.getReceivedMessages();
	    		for (String subject: map.keySet()) {
	    			receivedSubjects.add(subject);
	    		}
	    	}
	    	
	    		
			pw.print("Key;");
			for (int i = 0; i < nodes.size(); i++) {
	   			pw.print("Node" + i + ";");
	    	}
			pw.println("");
			
			pw.println(label);
			pw.println("---------sent messages----------");
	    	for (String key: sentSubjects) {
	    		pw.print(key + ";");
	    		for (Node node: nodes) {
	        		MessageCountingGenericOverlay overlay = (MessageCountingGenericOverlay)node.getOverlay();
	        		map = overlay.getSentMessages();
	        		if (!map.containsKey(key)) {
	        			pw.print("0;");
	        		} else {
	        			pw.print(map.get(key) + ";");
	        		}
	        	}
	    		pw.println("");
	    	}

	    	pw.println("---------received messages----------");
	    	for (String key: receivedSubjects) {
	    		pw.print(key + ";");
	    		for (Node node: nodes) {
	        		MessageCountingGenericOverlay overlay = (MessageCountingGenericOverlay)node.getOverlay();
	        		map = overlay.getReceivedMessages();
	        		if (!map.containsKey(key)) {
	        			pw.print("0;");
	        		} else {
	        			pw.print(map.get(key) + ";");
	        		}
	        	}
	    		pw.println("");
	    	}
	    	
	    	pw.println("---------Groups----------");
			pw.print("leaded groups;");
			for (Node node: nodes) {
	    		GroupCommunicationDispatcher dispatcher = node.getGroupCommunicationDispatcher();
	    		pw.print(dispatcher.getLeadedGroups().size() + ";");
	    	}
			pw.println("");
			
	    	pw.print("followed groups;");
			for (Node node: nodes) {
	    		GroupCommunicationDispatcher dispatcher = node.getGroupCommunicationDispatcher();
	    		pw.print(dispatcher.getFollowedGroups().size() + ";");
	    	}
			pw.println("");

	    	pw.print("leaded universe size;");
			for (Node node: nodes) {
	    		GroupCommunicationDispatcher dispatcher = node.getGroupCommunicationDispatcher();
	    		GroupCommunicationManager manager = dispatcher.getLeadedGroupByFriendlyName(GroupDescriptor.UNIVERSE);
	    		if (manager != null) {
	    			pw.print(manager.getGroupDescriptor().getMembers().size() + ";");
	    		} else {
	    			pw.print("0;");
	    		}
	    	}
			pw.println("");

			pw.print("Total active connections;");
			for (Node node: nodes) {
	    		pw.print(node.getTopologyManager().getNumberOfNeighbors() + ";");
	    	}
			pw.println("");
			
	    	pw.print("Application active connections;");
			for (Node node: nodes) {
	    		pw.print(node.getTopologyManager().getApplicationConnectionCount() + ";");
	    	}
			pw.println("");
			
			pw.print("Middleware active connections;");
			for (Node node: nodes) {
	    		pw.print(node.getTopologyManager().getMiddlewareConnectionCount() + ";");
	    	}
			pw.println("");
			
			pw.println("");
	    	pw.flush();
	    }
    	
    		
	
	public void tearDown() {
		node.stop();
		
	}
	
	
}

		