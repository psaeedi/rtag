/**
 * 
 */
package it.polimi.rtag;


import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.ArrayList;

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
public class JoinAndMergeGroupsTest {

    int localPort1 = 10001;
    int localPort2 = 10002;
    int localPort3 = 10003;
    int localPort4 = 10004;
    
    String host = "localhost";
    
    Node node1;
    Node node2;
    Node node3;
    Node node4;
    
    String node1Url = "reds-tcp:"+ host + ":" + localPort1;
    String node2Url = "reds-tcp:"+ host + ":" + localPort2;
    String node3Url = "reds-tcp:"+ host + ":" + localPort3;
    String node4Url = "reds-tcp:"+ host + ":" + localPort4;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	    node1 = new Node(host, localPort1);
	    node2 = new Node(host, localPort2);
	    node3 = new Node(host, localPort3);
	    node4 = new Node(host, localPort4);
	    
	    node1.start();
	    node2.start();
	    node3.start();
	    node4.start();
	    //node1.getOverlay().addNeighbor("reds-tcp:"+ host + ":" + localPort2);
	    //node1.getOverlay().addNeighbor("reds-tcp:"+ host + ":" + localPort3);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		node1.stop();
		node2.stop();
		node3.stop();
		node4.stop();
	}

	@Test
	public void testEachNodeIsInAUniverseAfterCreation() {
		innerTestEachNodeIsInAUniverseAfterCreation(node1);
		innerTestEachNodeIsInAUniverseAfterCreation(node2);
		innerTestEachNodeIsInAUniverseAfterCreation(node3);
	}
	
	private void innerTestEachNodeIsInAUniverseAfterCreation(Node node) {
		ArrayList<GroupCommunicationManager> managers = null;
		GroupCommunicationManager universeManager = null;
		GroupDescriptor groupDescriptor = null;
		
		managers = node.getGroupCommunicationDispatcher().getLeadedGroups();
		Assert.assertEquals(1, managers.size());
		universeManager = managers.get(0);
		groupDescriptor = universeManager.getGroupDescriptor();
		Assert.assertTrue(groupDescriptor.isUniverse());
		Assert.assertEquals(GroupDescriptor.UNIVERSE, groupDescriptor.getFriendlyName());
	}
	
	@Test
	public void testTwoUniverseWithNoFollowersBecomeOne() 
			throws AlreadyNeighborException, ConnectException, 
					MalformedURLException, NotRunningException, 
					InterruptedException {
		node1.getOverlay().addNeighbor(node2Url);
		
		Thread.sleep(1000);
		
		GroupDescriptor universe1 = node1.getGroupCommunicationDispatcher().
				getGroupMatching(GroupDescriptor.UNIVERSE);
		GroupDescriptor universe2 = node2.getGroupCommunicationDispatcher().
				getGroupMatching(GroupDescriptor.UNIVERSE);
		
		Assert.assertEquals(universe1.toString(), universe2.toString());
		
		Assert.assertEquals(2, universe1.getMembers().size());
		Assert.assertEquals(2, universe2.getMembers().size());
		Assert.assertEquals(universe1.getLeader(), universe2.getLeader());
		Assert.assertTrue( node1.getOverlay().isNeighborOf(node2.getID()));
	}
	
	@Test
	public void testUniverseComposition()
			throws AlreadyNeighborException, ConnectException,
					MalformedURLException, NotRunningException,
					InterruptedException {
		node1.getOverlay().addNeighbor(node2Url);
		node3.getOverlay().addNeighbor(node4Url);
		Thread.sleep(1000);
		
		// Node 1 and 3 should be leaders
		// (1,2) (3,4) are in the same universes
		GroupDescriptor universe1 = 
				node1.getGroupCommunicationDispatcher().
				getLocalUniverse();
		GroupDescriptor universe2 = 
				node2.getGroupCommunicationDispatcher().
				getLocalUniverse();
		GroupDescriptor universe3 = 
				node3.getGroupCommunicationDispatcher().
				getLocalUniverse();
		GroupDescriptor universe4 = 
				node4.getGroupCommunicationDispatcher().
				getLocalUniverse();
		
		Assert.assertEquals(universe1.toString(), universe2.toString());
		Assert.assertEquals(universe3.toString(), universe4.toString());
		
		NodeDescriptor leaderA = universe1.getLeader();
		NodeDescriptor leaderB = universe3.getLeader();
		
		Node nodeLeaderA = leaderA == node1.getID() ? node1 : node2;
		String urlB = leaderB == node3.getID() ? node3Url : node4Url;
		
		// We add the two leaders together
		nodeLeaderA.getOverlay().addNeighbor(urlB);
		Thread.sleep(1000);
		
		universe1 = 
			node1.getGroupCommunicationDispatcher().
			getLocalUniverse();
		universe2 = 
			node2.getGroupCommunicationDispatcher().
			getLocalUniverse();
		universe3 = 
			node3.getGroupCommunicationDispatcher().
			getLocalUniverse();
		universe4 = 
			node4.getGroupCommunicationDispatcher().
			getLocalUniverse();
		
		Assert.assertEquals(universe1.toString(), universe2.toString());
		Assert.assertEquals(universe3.toString(), universe4.toString());
		
		if (universe1.getParentLeader() == null) {
			Assert.assertEquals(universe3.getParentLeader(), leaderA);
			Assert.assertTrue(universe1.isFollower(leaderB));
		} else {
			Assert.assertEquals(universe1.getParentLeader(), leaderB);
			Assert.assertTrue(universe3.isFollower(leaderA));
		}
		
		System.out.println(universe1.acceptVisitor(new GroupToStringVisitor()));
	}
	
}
