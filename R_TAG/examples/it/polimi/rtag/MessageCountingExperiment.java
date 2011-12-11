/**
 * 
 */
package it.polimi.rtag;

import it.polimi.rtag.messaging.TupleMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class MessageCountingExperiment {
	
	
	public static String RED = "Red";
	public static String GREEN = "Green";
	public static String YELLOW = "Yellow";
	public static String BLUE = "Blue";

    private static final int NUMBER_OF_NODES = 20;
	
	int localPort=10001;
    
    String host = "localhost";
    
    ArrayList<Node> nodes = new ArrayList<Node>();
    ArrayList<String> urls = new ArrayList<String>();
    
    
    PrintWriter pw = null;
    
    public MessageCountingExperiment() {
    	File messagesFile = new File("MessageCountingExperiment.csv");
    	try {
			pw = new PrintWriter(messagesFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void setUp() {
    	for (int i = 0; i < NUMBER_OF_NODES/2; i++) {
			int port = localPort ++;
			Node node = new Node(host, port);
			node.start();
			node.joinGroup(RED);
			nodes.add(node);
			urls.add("reds-tcp:"+ host + ":" + port);
		}
    	
    	for (int i = 0; i < NUMBER_OF_NODES/2; i++) {
			int port = localPort ++;
			Node node = new Node(host, port);
			node.start();
			node.joinGroup(BLUE);
			nodes.add(node);
			urls.add("reds-tcp:"+ host + ":" + port);
		}
    	
    	createNetworkByAddingToTheLastAdded();
    	
    	for (int i = 0; i < NUMBER_OF_NODES; i++) {
			Node n = nodes.get(i);
			System.out.println("-----------------------------node: " + n.getNodeDescriptor());
			GroupCommunicationDispatcher disp = n.getGroupCommunicationDispatcher();
			for (GroupCommunicationManager manager: disp.getFollowedGroups()) {
				System.out.println(manager.getGroupDescriptor());
			}
			for (GroupCommunicationManager manager: disp.getLeadedGroups()) {
				System.out.println(manager.getGroupDescriptor());
			}
		}
    	
    	// 1 minute waiting
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	sendGroupcast(nodes.get(8), RED, "Hello" );
    }
    
    
    private void createNetworkByAddingToNode0() {
    	for (int i = 1; i < NUMBER_OF_NODES; i++) {
    		System.out.println("************Adding neighbor " + i + " to node 0");
    		try {
				nodes.get(0).getOverlay().addNeighbor(urls.get(i));
				Thread.sleep(2000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    
    private void createNetworkByAddingToTheLastAdded() {
    	for (int i = 1; i < NUMBER_OF_NODES; i++) {
    		System.out.println("************Adding neighbor " + i + " to node " + nodes.get(i-1));
    		try {
				nodes.get(i-1).getOverlay().addNeighbor(urls.get(i));
				Thread.sleep(1000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    
    private void resetCounters() {
    	for (Node node: nodes) {
    		MessageCountingGenericOverlay overlay = (MessageCountingGenericOverlay)node.getOverlay();
    		overlay.clearCounting();
    	}
    }
    
    private void writeToFile(String label) {
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
		
		pw.print("leaded app group1 size;");
		for (Node node: nodes) {
    		GroupCommunicationDispatcher dispatcher = node.getGroupCommunicationDispatcher();
    		GroupCommunicationManager manager = dispatcher.getLeadedGroupByFriendlyName(BLUE);
    		if (manager != null) {
    			pw.print(manager.getGroupDescriptor().getMembers().size() + ";");
    		} else {
    			pw.print("0;");
    		}
    	}
		pw.println("");
		

		
		pw.print("leaded app group2 size;");
		for (Node node: nodes) {
    		GroupCommunicationDispatcher dispatcher = node.getGroupCommunicationDispatcher();
    		GroupCommunicationManager manager = dispatcher.getLeadedGroupByFriendlyName(RED);
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
    
    private void tearDown() {
		for (Node node: nodes) {
			node.stop();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
    
    private void closeFile() {
    	pw.flush();
    	pw.close();
    	
    }
    
	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		MessageCountingExperiment exp = new MessageCountingExperiment();
		exp.setUp();
		// 1 minute waiting
		Thread.sleep(10000);
		exp.writeToFile("setUp");
		exp.closeFile();
		exp.tearDown();
	}

	private static void sendGroupcast(Node node, String color, String content) {
		GroupDescriptor group = node.getGroup(color);
    	if (group == null) {
    		return;
    	}
    	System.out.println(group);
    	
    	TupleMessage message = new ExampleMessage(group.getFriendlyName(), content, "HELLO");
        node.getTupleSpaceManager().storeAndSend(message);
	}

}

class ExampleMessage extends TupleMessage {
	private static final long serialVersionUID = -5146903837877861792L;
	
	public ExampleMessage(String recipient,
			Serializable content, String command) {
		super(Scope.HIERARCHY, recipient, content, command);
	}
	
	@Override
	public String getSubject() {
		return CUSTOM_MESSAGE;
	}
	
}