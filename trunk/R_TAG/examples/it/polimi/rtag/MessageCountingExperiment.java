/**
 * 
 */
package it.polimi.rtag;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class MessageCountingExperiment {

    private static final int NUMBER_OF_NODES = 100;

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
    	for (int i = 0; i < NUMBER_OF_NODES; i++) {
			int port = localPort ++;
			Node node = new Node(host, port);
			node.start();
			nodes.add(node);
			urls.add("reds-tcp:"+ host + ":" + port);
		}
    	
    	for (int i = 1; i < NUMBER_OF_NODES; i++) {
    		int randomNode = (int)Math.floor(Math.random() * i);
    		System.out.println("************Adding neighbor " + i + " to node " + randomNode);
    		try {
				nodes.get(randomNode).getOverlay().addNeighbor(urls.get(i));
				Thread.sleep(1500);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    private void resetCounters() {
    	for (Node node: nodes) {
    		MessageCountingGenericOverlay overlay = (MessageCountingGenericOverlay)node.getOverlay();
    		overlay.getSentMessages().clear();
    		overlay.getReceivedMessages().clear();
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
		
    	pw.flush();
    }
    
    private void tearDown() {
		for (Node node: nodes) {
			node.stop();
			try {
				Thread.sleep(500);
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
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MessageCountingExperiment exp = new MessageCountingExperiment();
		exp.setUp();
		exp.writeToFile("setUp");
		exp.resetCounters();
		exp.tearDown();
		exp.writeToFile("tearDown");
		exp.resetCounters();
		exp.closeFile();
	}

}