package it.polimi.rtag;


import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import polimi.reds.NodeDescriptor;
import polimi.reds.broker.overlay.AlreadyNeighborException;
import polimi.reds.broker.overlay.NotRunningException;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class LeaderReplaceTest {

    private static final int NUMBER_OF_NODES = 6;

	int localPort=10001;
    
	
	String host = "localhost";
	
	ArrayList<Node> nodes = new ArrayList<Node>();
	Map<NodeDescriptor, Node> nodesById = new HashMap<NodeDescriptor, Node>();
	ArrayList<String> urls = new ArrayList<String>();
	
		
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		for (int i = 0; i < NUMBER_OF_NODES; i++) {
			int port = localPort ++;
			Node node = new Node(host, port);
			node.start();
			nodes.add(node);
			urls.add("reds-tcp:"+ host + ":" + port);
			nodesById.put(node.getID(), node);
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		for (Node node: nodes) {
			node.stop();
		}
	}
	
	
	@Test
	public void testLeaderCollapse()
			throws AlreadyNeighborException, ConnectException, 
			MalformedURLException, NotRunningException, 
			InterruptedException {
		nodes.get(0).getOverlay().addNeighbor(urls.get(1));
		nodes.get(0).getOverlay().addNeighbor(urls.get(2));
		nodes.get(3).getOverlay().addNeighbor(urls.get(4));
		nodes.get(3).getOverlay().addNeighbor(urls.get(5));
		
		nodes.get(0).getOverlay().addNeighbor(urls.get(3));
		Thread.sleep(500);
		
		GroupDescriptor universe0 = nodes.get(0).getGroupCommunicationDispatcher().getLocalUniverse();
		GroupDescriptor universe1 = nodes.get(3).getGroupCommunicationDispatcher().getLocalUniverse();
		
		GroupDescriptor parentUniverse = null;
		GroupDescriptor childUniverse = null;
		if (universe0.getParentLeader() != null) {
			parentUniverse = universe1;
			childUniverse = universe0;
		} else {
			parentUniverse = universe0;
			childUniverse = universe1;
		}
		Assert.assertNotNull(childUniverse.getParentLeader());
		Assert.assertEquals(parentUniverse.getLeader(), childUniverse.getParentLeader());
		
		Node childLeaderNode = nodesById.get(childUniverse.getLeader());
		Node aChildFollower = nodesById.get(childUniverse.getFollowers().get(0));
		childLeaderNode.stop();
		Thread.sleep(500);
		childUniverse = aChildFollower.getGroupCommunicationDispatcher().getLocalUniverse();
		
		Assert.assertEquals(parentUniverse.getLeader(), childUniverse.getParentLeader());
		Assert.assertNotNull(childUniverse.getLeader());
		Assert.assertFalse(childUniverse.isMember(childLeaderNode.getID()));
	}
}
