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
		}
	}
	
	
	@Test
	public void testLeaderCollapse()
			throws AlreadyNeighborException, ConnectException, 
			MalformedURLException, NotRunningException, 
			InterruptedException {
		nodes.get(0).getOverlay().addNeighbor(urls.get(1));
		
		Thread.sleep(1000);
		// Node 1 is the leaders
		// (1,2,3,4,5) are in the same universe
		
		GroupDescriptor universe1 = nodes.get(0).getGroupCommunicationDispatcher().
				getGroupWithName(GroupDescriptor.UNIVERSE);
		Node leader = universe1.getLeader() == nodes.get(0).getID() ? nodes.get(0) : nodes.get(1);
		
		for (int i = 2; i < NUMBER_OF_NODE; i++) {			
			leader.getOverlay().addNeighbor(urls.get(i));
			Thread.sleep(1000);
		}
		
		Assert.assertTrue(universe1.getFollowers().size()>1);
		leader.stop();
		Thread.sleep(2000);

		UUID universeId = universe1.getUniqueId();
		
		ArrayList<GroupDescriptor> descriptors = new ArrayList<GroupDescriptor>();
		for (Node node: nodes) {			
			if (node.getOverlay().isRunning()) {
				GroupDescriptor universe = node.getGroupCommunicationDispatcher().
						getGroupWithName(GroupDescriptor.UNIVERSE);
				if (!universe.getUniqueId().equals(universeId)) {
					continue;
				}
				Assert.assertTrue(universe.getLeader() != leader.getID());
				Assert.assertNotNull(universe.getLeader());
				descriptors.add(universe);
			}
		}
		Assert.assertTrue(descriptors.size() >= 2);
		GroupDescriptor desc = descriptors.get(0);
		for (GroupDescriptor descriptor: descriptors) {
			Assert.assertEquals(desc.toString(), descriptor.toString());
		}
	}
}
