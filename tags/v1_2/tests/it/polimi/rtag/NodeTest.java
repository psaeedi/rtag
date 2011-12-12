/**
 * 
 */
package it.polimi.rtag;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import polimi.reds.NodeDescriptor;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class NodeTest {

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
			nodesById.put(node.getNodeDescriptor(), node);
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
	public void testNodesJoinUniversUponCreation() throws InterruptedException {
		for (int i = 1; i < NUMBER_OF_NODES; i++) {
			Node node = nodes.get(i);
			GroupDescriptor groupDescriptor1 =
					node.getGroup(GroupDescriptor.UNIVERSE);
			Assert.assertNotNull(groupDescriptor1);
			GroupDescriptor groupDescriptor2 =
					node.getTupleSpaceManager().getLeaderForHierarchy(GroupDescriptor.UNIVERSE);
			Assert.assertNotNull(groupDescriptor2);
			Assert.assertEquals(groupDescriptor1, groupDescriptor2);
		}
	}
	
	@Test
	public void testDeleteGroup() throws Exception {
		for (int i = 1; i < NUMBER_OF_NODES; i++) {
			nodes.get(0).addNeighbor(urls.get(i));
			Thread.sleep(300);
		}
		
		Set<String> universes = new HashSet<String>();
		for (int i = 0; i < NUMBER_OF_NODES; i++) {
			GroupDescriptor universe = nodes.get(i).getGroupCommunicationDispatcher().getLocalUniverse();
			Assert.assertNotNull(universe);
			System.out.println(universe);
			for (NodeDescriptor n: universe.getMembers()) {
				universes.add(n.getID());
			}
		}
		Assert.assertEquals(NUMBER_OF_NODES, universes.size());
		
		String friendlyName = "RED";
		
		for (int i = 0; i < NUMBER_OF_NODES; i++) {
			nodes.get(i).joinGroup(friendlyName);
			Thread.sleep(300);
		}
		
		Set<String> reds = new HashSet<String>();
		for (int i = 0; i < NUMBER_OF_NODES; i++) {
			GroupDescriptor red = nodes.get(i).getGroup(friendlyName);
			Assert.assertNotNull(red);
			System.out.println(red);
			for (NodeDescriptor n: red.getMembers()) {
				reds.add(n.getID());
			}
		}
		
		Assert.assertEquals(NUMBER_OF_NODES, reds.size());
		
		System.err.println("<<<<<<\n" + nodes.get(0).getGroupCommunicationDispatcher().getFollowedGroupByFriendlyName(friendlyName));
		nodes.get(0).deleteGroup(friendlyName);
		Thread.sleep(1000);
		
		for (int i = 0; i < NUMBER_OF_NODES; i++) {
			GroupDescriptor red = nodes.get(i).getGroup(friendlyName);
			System.out.println(red);
			Assert.assertNull(red);
		}
	}
}
