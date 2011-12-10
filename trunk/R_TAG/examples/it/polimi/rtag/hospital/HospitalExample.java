package it.polimi.rtag.hospital;

import java.io.File;
import java.io.FileNotFoundException;
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

public class HospitalExample {
    
    private PrintWriter pw;
    
    public enum IntelligentJunction {
		ONE,TWO,THREE,FOUR,FIVE 
    }
	
	private Node node;
	
	/* Junction one is the main entrance, however, there are
	 * other auxiliary junctions
	 */
	private IntelligentJunction currentJunction = IntelligentJunction.ONE;
	
	
 

	public static void main(String[] args) throws Exception {
		int port = 0;
	    String parent = null;
	    String host = null;
		
	    for(String arg: args) {
	    	System.out.print(" ");
		    System.out.print(arg);
		    System.out.print(" ");
		
		    String splitted[] = arg.split("=", 2);
		    
		    if(splitted[0].equalsIgnoreCase("port"))
		    	port = Integer.parseInt(splitted[1]);
		    else if(splitted[0].equalsIgnoreCase("parent"))
		    	parent = splitted[1];
		    else if(splitted[0].equalsIgnoreCase("host"))
		    	host = splitted[1];
		    }
	    
		    
		    HospitalExample exp = new HospitalExample(
		    		host,
		    		port,
		    		parent,
		    		IntelligentJunction.ONE);
		     
		    System.out.println(" ");
		    Thread.sleep(5000);
		    exp.writeToFile("setUp");
		    exp.closeFile();
		    exp.stop();
    }
	
	

	 
	    
    public HospitalExample(
    		String host,
    		int port,
    		String parent,
    		IntelligentJunction currentJunction
    		) throws InterruptedException, AlreadyNeighborException, ConnectException, MalformedURLException, NotRunningException {
    	try {
			pw = new PrintWriter(new File("ColorExample" + port + ".cvs"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	node = new Node(host, port);
		node.start();
		setCurrentJunction(currentJunction);
		if (parent != null) {
			node.addNeighbor(parent);
		}
    }
    
  
    public void writeToFile(String label) {
    	HashSet<String> sentSubjects = new HashSet<String>();
    	HashSet<String> receivedSubjects = new HashSet<String>();
    	
    	HashMap<String, Integer> map = null;
		MessageCountingGenericOverlay overlay = (MessageCountingGenericOverlay)node.getOverlay();
		map = overlay.getSentMessages();
		for (String subject: map.keySet()) {
			sentSubjects.add(subject);
		}
		map = overlay.getReceivedMessages();
		for (String subject: map.keySet()) {
			receivedSubjects.add(subject);
		}
    	
    		
		pw.print("Key;");
		pw.print("Node" + node + ";");
		pw.println("");
		
		pw.println(label);
		pw.println("---------sent messages----------");
    	for (String key: sentSubjects) {
    		pw.print(key + ";");
			map = overlay.getSentMessages();
    		if (!map.containsKey(key)) {
    			pw.print("0;");
    		} else {
    			pw.print(map.get(key) + ";");
    		}
    	pw.println("");
    	}

    	pw.println("---------received messages----------");
    	for (String key: receivedSubjects) {
    		pw.print(key + ";");
			map = overlay.getReceivedMessages();
    		if (!map.containsKey(key)) {
    			pw.print("0;");
    		} else {
    			pw.print(map.get(key) + ";");
    		}
    		pw.println("");
    	}
    	
    	pw.println("---------Groups----------");
		pw.print("leaded groups;");
		GroupCommunicationDispatcher dispatcher = node.getGroupCommunicationDispatcher();
		pw.print(dispatcher.getLeadedGroups().size() + ";");
		pw.println("");
		
    	pw.print("followed groups;");
		pw.print(dispatcher.getFollowedGroups().size() + ";");
		pw.println("");

    	pw.print("leaded universe size;");
    	GroupCommunicationManager manager = dispatcher.getLeadedGroupByFriendlyName(GroupDescriptor.UNIVERSE);
		if (manager != null) {
			pw.print(manager.getGroupDescriptor().getMembers().size() + ";");
		} else {
			pw.print("0;");
		}
	
		pw.println("");

		pw.print("Total active connections;");
		pw.print(node.getTopologyManager().getNumberOfNeighbors() + ";");
    	pw.println("");
		
    	pw.print("Application active connections;");
		pw.print(node.getTopologyManager().getApplicationConnectionCount() + ";");
		pw.println("");
		
		pw.print("Middleware active connections;");
		pw.print(node.getTopologyManager().getMiddlewareConnectionCount() + ";");
    	pw.println("");
		
		pw.println("");
    	pw.flush();
    }

	private void closeFile() {
    	pw.flush();
    	pw.close();
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    		
	
	public void stop() {
		node.stop();
		
	}
		
}
