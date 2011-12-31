/**
 * 
 */
package it.polimi.rtag;

import it.polimi.rtag.messaging.TupleMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;

import polimi.reds.broker.overlay.AlreadyNeighborException;
import polimi.reds.broker.overlay.NotRunningException;


/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class MessageCountingExperiment {
	
	
	public static String RED = "Red";
	public static String GREEN = "Green";
	public static String YELLOW = "Yellow";
	public static String BLUE = "Blue";
	public static String ORANGE = "Orange";
	public static String PURPLE = "Purple";
	public static String PINK = "Pink";
	public static String WHITE = "White";

	public static final int NUM_GROUPCASTS = 1;

    private static final int NUMBER_OF_NODES = 10;
	
	int localPort = 20001;
    
    String host = "192.168.0.4";
    
    static String parent;
    
    ArrayList<Node> nodes = new ArrayList<Node>();
    ArrayList<String> urls = new ArrayList<String>();
    
    String parentUrl;
    
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
			for (String url: node.getNodeDescriptor().getUrls()) {
				System.out.println(url);
			}
			if (i%2 == 0) {
				node.joinGroup(RED);
			} else {
				node.joinGroup(BLUE);
			}
			nodes.add(node);
			urls.add("reds-tcp:"+ host + ":" + port);
		}
    	
    	/*for (int i = 0; i < NUMBER_OF_NODES/8; i++) {
			int port = localPort ++;
			Node node = new Node(host, port);
			node.start();
			node.joinGroup(YELLOW);
			nodes.add(node);
			urls.add("reds-tcp:"+ host + ":" + port);
    	}
    	
    	for (int i = 0; i < NUMBER_OF_NODES/8; i++) {
			int port = localPort ++;
			Node node = new Node(host, port);
			node.start();
			node.joinGroup(GREEN);
			nodes.add(node);
			urls.add("reds-tcp:"+ host + ":" + port);
    	}
    	
    	for (int i = 0; i < NUMBER_OF_NODES/8; i++) {
			int port = localPort ++;
			Node node = new Node(host, port);
			node.start();
			node.joinGroup(ORANGE);
			nodes.add(node);
			urls.add("reds-tcp:"+ host + ":" + port);
			
    	}
    	
    	for (int i = 0; i < NUMBER_OF_NODES/8; i++) {
			int port = localPort ++;
			Node node = new Node(host, port);
			node.start();
			node.joinGroup(PURPLE);
			nodes.add(node);
			urls.add("reds-tcp:"+ host + ":" + port);
			
    	}
    	for (int i = 0; i < NUMBER_OF_NODES/8; i++) {
			int port = localPort ++;
			Node node = new Node(host, port);
			node.start();
			node.joinGroup(PINK);
			nodes.add(node);
			urls.add("reds-tcp:"+ host + ":" + port);
			
    	}
    	for (int i = 0; i < NUMBER_OF_NODES/8; i++) {
			int port = localPort ++;
			Node node = new Node(host, port);
			node.start();
			node.joinGroup(WHITE);
			nodes.add(node);
			urls.add("reds-tcp:"+ host + ":" + port);
			
    	}*/
    	if (parentUrl != null) {
    		connectToRemoteNode(parentUrl);
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
    	
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    
    	 
    	//sendGroupcast(nodes.get(3), RED, "Hello" );
    	
    	
    	//sendGroupcast(nodes.get(19), BLUE, "Hello" );
    	//Thread.sleep(2500);
    	//sendGroupcast(nodes.get(18), GREEN, "Hello" );
    	//Thread.sleep(2500);
    }
    
    public void sendGroupcast() {
    	for (int i = 0; i < NUM_GROUPCASTS; i++) {
    		for (int j = 0; j < NUMBER_OF_NODES; j++) {
    			if (j%2 == 0) {
    				sendGroupcast(nodes.get(j), RED, "Hi" + i + " from node " + j);
    			} else {
    				sendGroupcast(nodes.get(j), BLUE, "Hi" + i + " from node " + j);
    			}
    		}
    	}
    }
    
    private void createNetworkByAddingToNode0() {
    	for (int i = 1; i < NUMBER_OF_NODES; i++) {
    		System.out.println("************Adding neighbor " + i + " to node 0");
    		try {
				nodes.get(0).getOverlay().addNeighbor(urls.get(i));
				Thread.sleep(2500);
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
		
		pw.print("leaded app group4 size;");
		for (Node node: nodes) {
    		GroupCommunicationDispatcher dispatcher = node.getGroupCommunicationDispatcher();
    		GroupCommunicationManager manager = dispatcher.getLeadedGroupByFriendlyName(GREEN);
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
				Thread.sleep(2000);
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
		try {
			  InetAddress localhost = InetAddress.getLocalHost();
			  System.out.println(" IP Addr: " + localhost.getHostAddress());
			  // Just in case this host has multiple IP addresses....
			  InetAddress[] allMyIps = InetAddress.getAllByName(localhost.getCanonicalHostName());
			  if (allMyIps != null && allMyIps.length > 1) {
				  System.out.println(" Full list of IP addresses:");
			    for (int i = 0; i < allMyIps.length; i++) {
			    	System.out.println("    " + allMyIps[i]);
			    }
			  }
			} catch (UnknownHostException e) {
				System.out.println(" (error retrieving server host name)");
			}

			try {
				System.out.println("Full list of Network Interfaces:");
			  for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
			    NetworkInterface intf = en.nextElement();
			    System.out.println("    " + intf.getName() + " " + intf.getDisplayName());
			    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
			    	System.out.println("        " + enumIpAddr.nextElement().toString());
			    }
			  }
			} catch (SocketException e) {
				System.out.println(" (error retrieving network interface list)");
			}

		
		
		// TODO Auto-generated method stub
		MessageCountingExperiment exp = new MessageCountingExperiment();
		if (args.length > 0) {
			exp.setParent(args[0]);
		}
		exp.setUp();
		// 1 minute waiting
		Thread.sleep(2000);
		
		/*
		exp.writeToFile("setUp");
		exp.resetCounters();
		exp.addNode();
		exp.writeToFile("addNode");
		*/
		
		
		
		/*
		exp.closeFile();
		exp.tearDown();
		*/
		
		readCommand(exp);
	}
	
	
	static void readCommand(MessageCountingExperiment exp)
	{
		try {
			java.io.BufferedReader stdin = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
			while (true) {
				String line = stdin.readLine();
		        if ("quit".equals(line)) {
		        	exp.writeToFile("quit");
		        	exp.closeFile();
		    		exp.tearDown();
		        } else if ("groupcast".equals(line)) {
		        	exp.sendGroupcast();
		        }
			}
		}
		catch (java.io.IOException e) { System.out.println(e); }
		catch (NumberFormatException e) { System.out.println(e); }
	}

	public void connectToRemoteNode(String url) {
		try {
			nodes.get(0).addNeighbor(url);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void setParent(String url) {
		this.parentUrl = url;
	}

	public void addNode() throws InterruptedException{
		int port = localPort++;
		Node node = new Node(host, port);
		node.start();
		node.joinGroup(RED);
		nodes.add(node);
		String url="reds-tcp:"+ host + ":" + port;
		try {
			nodes.get(0).addNeighbor(url);
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
		Thread.sleep(2000);
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