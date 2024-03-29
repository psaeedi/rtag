/**
 * 
 */
package it.polimi.rtag;


import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
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
public class JoinAndMergeGroupsTest {

    private static final int NUMBER_OF_NODE = 5;

	int localPort=10001;
    
    String host = "localhost";
    
    ArrayList<Node> nodes = new ArrayList<Node>();
    ArrayList<String> urls = new ArrayList<String>();
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		for (int i = 0; i < NUMBER_OF_NODE; i++) {
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
			Thread.sleep(500);
		}
	}

	@Test
	public void testEachNodeIsInAUniverseAfterCreation() {
		innerTestEachNodeIsInAUniverseAfterCreation(nodes.get(0));
		innerTestEachNodeIsInAUniverseAfterCreation(nodes.get(1));
		innerTestEachNodeIsInAUniverseAfterCreation(nodes.get(2));
	}
	
	private void innerTestEachNodeIsInAUniverseAfterCreation(Node node) {
		ArrayList<GroupCommunicationManager> managers = null;
		GroupCommunicationManager universeManager = null;
		GroupDescriptor groupDescriptor = null;
		
		managers = node.getGroupCommunicationDispatcher().getLeadedGroups();
		Assert.assertEquals(1, managers.size());
		universeManager = managers.get(0);
		groupDescriptor = universeManager.getGroupDescriptor();
		System.out.println(groupDescriptor);
		System.out.println(groupDescriptor.isUniverse());
		Assert.assertTrue(groupDescriptor.isUniverse());
		Assert.assertEquals(GroupDescriptor.UNIVERSE, groupDescriptor.getFriendlyName());
	}
	
	@Test
	public void testTwoUniverseWithNoFollowersBecomeOne() 
			throws AlreadyNeighborException, ConnectException, 
					MalformedURLException, NotRunningException, 
					InterruptedException {
		nodes.get(0).addNeighbor(urls.get(1));
		Thread.sleep(200);
		
		GroupDescriptor universe1 = nodes.get(0).getGroupCommunicationDispatcher().
				getGroupForHierarchy(GroupDescriptor.UNIVERSE);
		GroupDescriptor universe2 = nodes.get(1).getGroupCommunicationDispatcher().
				getGroupForHierarchy(GroupDescriptor.UNIVERSE);
		
		Assert.assertEquals(universe1, universe2);
		
		Assert.assertEquals(2, universe1.getMembers().size());
		Assert.assertTrue(nodes.get(0).getOverlay().isNeighborOf(nodes.get(1).getNodeDescriptor()));
		
		Node leader = (universe1.getLeader() == nodes.get(0).getNodeDescriptor()) ? nodes.get(0) : nodes.get(1);
		Node follower = (universe1.getLeader() == nodes.get(0).getNodeDescriptor()) ? nodes.get(1) : nodes.get(0);
		
		
		Assert.assertEquals(1, leader.getGroupCommunicationDispatcher().getLeadedGroups().size());
		Assert.assertEquals(0, leader.getGroupCommunicationDispatcher().getFollowedGroups().size());
		Assert.assertEquals(0, follower.getGroupCommunicationDispatcher().getLeadedGroups().size());
		Assert.assertEquals(1, follower.getGroupCommunicationDispatcher().getFollowedGroups().size());
	}
	
	@Test
	public void testUniverseComposition()
			throws AlreadyNeighborException, ConnectException,
					MalformedURLException, NotRunningException,
					InterruptedException {
		nodes.get(0).addNeighbor(urls.get(1));
		
		nodes.get(2).addNeighbor(urls.get(3));
		Thread.sleep(1000);
		
		// Node 1 and 3 should be leaders
		// (1,2) (3,4) are in the same universes
		GroupDescriptor universe0 = 
				nodes.get(0).getGroupCommunicationDispatcher().
				getLocalUniverse();
		GroupDescriptor universe1 = 
			    nodes.get(1).getGroupCommunicationDispatcher().
				getLocalUniverse();
		GroupDescriptor universe2 = 
			    nodes.get(2).getGroupCommunicationDispatcher().
				getLocalUniverse();
		GroupDescriptor universe3 = 
			    nodes.get(3).getGroupCommunicationDispatcher().
				getLocalUniverse();
		
		Assert.assertEquals(universe0, universe1);
		Assert.assertEquals(universe2, universe3);
		
		NodeDescriptor leaderA = universe0.getLeader();
		NodeDescriptor leaderB = universe2.getLeader();
		
		Node nodeLeaderA = leaderA == nodes.get(0).getNodeDescriptor() ? nodes.get(0) : nodes.get(1);
		String urlB = leaderB == nodes.get(2).getNodeDescriptor() ? urls.get(2) : urls.get(3);
		
		// We add the two leaders together
		nodeLeaderA.addNeighbor(urlB);
		Thread.sleep(1000);
		
		universe0 = 
			nodes.get(0).getGroupCommunicationDispatcher().
			getLocalUniverse();
		universe1 = 
			nodes.get(1).getGroupCommunicationDispatcher().
			getLocalUniverse();
		universe2 = 
			nodes.get(2).getGroupCommunicationDispatcher().
			getLocalUniverse();
		universe3 = 
			nodes.get(3).getGroupCommunicationDispatcher().
			getLocalUniverse();
		
		Assert.assertEquals(universe0, universe1);
		Assert.assertEquals(universe2, universe3);
		
		if (universe0.getParentLeader() == null) {
			Assert.assertEquals(universe2.getParentLeader(), leaderA);
			Assert.assertTrue(universe0.isFollower(leaderB));
		} else {
			Assert.assertEquals(universe0.getParentLeader(), leaderB);
			Assert.assertTrue(universe2.isFollower(leaderA));
		}
		
		System.out.println(universe0.acceptVisitor(new GroupToStringVisitor()));
	}
	
	@Test
	public void testMultipleFollowersJoinGroup() 
			throws AlreadyNeighborException, ConnectException, 
					MalformedURLException, NotRunningException, 
					InterruptedException {
		
		for (int i = 1; i < NUMBER_OF_NODE; i++) {
			nodes.get(0).addNeighbor(urls.get(i));
			Thread.sleep(500);
		}
		
		HashSet<UUID> groups = new HashSet<UUID>();
		int rootGroups = 0;
		for (int i = 0; i < NUMBER_OF_NODE; i++) {
			GroupDescriptor universe = nodes.get(i)
					.getGroupCommunicationDispatcher().getLocalUniverse();
			if (!groups.contains(universe.getUniqueId())) {
				groups.add(universe.getUniqueId());
				if (universe.getParentLeader() == null) {
					rootGroups += 1;
				}
			}
			System.out.println("Node: " + i + "\n" + universe);
		}
		
		// TODO here we should check if the whole hierarchy size is 5
		Assert.assertEquals(1, rootGroups);
		
	}
	
}
