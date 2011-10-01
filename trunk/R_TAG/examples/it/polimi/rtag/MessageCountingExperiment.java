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

    private static final int NUMBER_OF_NODES = 200;

	int localPort=10001;
    
    String host = "localhost";
    
    ArrayList<Node> nodes = new ArrayList<Node>();
    ArrayList<String> urls = new ArrayList<String>();
	
    private void setUp() {
    	for (int i = 0; i < NUMBER_OF_NODES; i++) {
			int port = localPort ++;
			Node node = new Node(host, port);
			node.start();
			nodes.add(node);
			urls.add("reds-tcp:"+ host + ":" + port);
		}
    }
    
    private void writeToFile() {
    	File sentMessagesFile = new File("MessageCountingExperiment_sentMessages.csv");
    	File receivedMessagesFile = new File("MessageCountingExperiment_receivedMessages.csv");
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
    	
    	PrintWriter pw = null;
    	try {
			pw = new PrintWriter(sentMessagesFile);
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
	    	pw.flush();
	    	pw.close();
    	} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	try {
			pw = new PrintWriter(receivedMessagesFile);
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
	    	pw.flush();
	    	pw.close();
    	} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    private void tearDown() {
		for (Node node: nodes) {
			node.stop();
		}
	}
    
    private void doExperiment() {
    	for (int i = 1; i < NUMBER_OF_NODES; i++) {
    		int randomNode = (int)Math.floor(Math.random() * i);
    		System.out.println("Adding neighbor to node " + randomNode);
    		try {
				nodes.get(randomNode).getOverlay().addNeighbor(urls.get(i));
				Thread.sleep(1000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }

    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MessageCountingExperiment exp = new MessageCountingExperiment();
		exp.setUp();
		exp.doExperiment();
		exp.tearDown();
		exp.writeToFile();
	}

}
