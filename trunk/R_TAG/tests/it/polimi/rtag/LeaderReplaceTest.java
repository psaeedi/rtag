package it.polimi.rtag;


import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.ArrayList;
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

    private static final int NUMBER_OF_NODES = 5;

	int localPort=10001;
    
	
	String host = "localhost";
	
	ArrayList<Node> nodes = new ArrayList<Node>();
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
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		for (Node node: nodes) {
			node.stop();
			Thread.sleep(100);
		}
	}
	
	
	@Test
	public void testLeaderCollapse()
			throws AlreadyNeighborException, ConnectException, 
			MalformedURLException, NotRunningException, 
			InterruptedException {
		nodes.get(0).getOverlay().addNeighbor(urls.get(1));
		nodes.get(2).getOverlay().addNeighbor(urls.get(3));
		
		Thread.sleep(1000);
		
		// Node 1 and 3 should be leaders
		// (1,2) (3,4) are in the same universes
		GroupDescriptor universe1 = 
				nodes.get(0).getGroupCommunicationDispatcher().
				getLocalUniverse();
		GroupDescriptor universe2 = 
			    nodes.get(1).getGroupCommunicationDispatcher().
				getLocalUniverse();
		GroupDescriptor universe3 = 
			    nodes.get(2).getGroupCommunicationDispatcher().
				getLocalUniverse();
		GroupDescriptor universe4 = 
			    nodes.get(3).getGroupCommunicationDispatcher().
				getLocalUniverse();
		
		Assert.assertEquals(universe1, universe2);
		Assert.assertEquals(universe3, universe4);
		
		NodeDescriptor leaderA = universe1.getLeader();
		NodeDescriptor leaderB = universe3.getLeader();
		
		Node nodeLeaderA = (leaderA == nodes.get(0).getID()) ? nodes.get(0) : nodes.get(1);
		Node nodeLeaderB = (leaderB == nodes.get(2).getID()) ? nodes.get(2) : nodes.get(3);
		Node nodeFollowerB = (leaderB == nodes.get(2).getID()) ? nodes.get(3) : nodes.get(2);
		String urlB = (leaderB == nodes.get(2).getID()) ? urls.get(2) : urls.get(3);
		
		// We add the two leaders together
		nodeLeaderA.getOverlay().addNeighbor(urlB);
		Thread.sleep(1000);
		nodeLeaderB.getOverlay().addNeighbor(urls.get(4));
		Thread.sleep(1000);
		
		// TODO this works 50% of the time fix it
		GroupDescriptor universeB = nodeLeaderB.
				getGroupCommunicationDispatcher().getLocalUniverse();
		Assert.assertTrue(universeB.getParentLeader().equals(leaderA));
		Assert.assertTrue(universeB.isFollower(nodeFollowerB.getID()));
	
		
		System.out.println("Stopping: " + leaderB);
		nodeLeaderB.stop();
		Thread.sleep(500);
		
		// The orphan group should be adopted
		GroupDescriptor adoptedUniverse = nodeFollowerB.
				getGroupCommunicationDispatcher().getLocalUniverse();
		
		System.out.println(adoptedUniverse);
		
		Assert.assertTrue(!adoptedUniverse.isMember(leaderB));
		Assert.assertEquals(leaderA, adoptedUniverse.getParentLeader());
		
		GroupDescriptor parentUniverse = nodeLeaderA.
				getGroupCommunicationDispatcher().getLocalUniverse();
		Assert.assertTrue(parentUniverse.isFollower(adoptedUniverse.getLeader()));
		
		Thread.sleep(500);
	}
}
