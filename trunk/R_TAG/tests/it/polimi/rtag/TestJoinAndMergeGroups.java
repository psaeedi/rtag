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

import polimi.reds.broker.overlay.AlreadyNeighborException;
import polimi.reds.broker.overlay.NotRunningException;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class TestJoinAndMergeGroups {

    int localPort1 = 10001;
    int localPort2 = 10002;
    int localPort3 = 10003;
    
    String host = "localhost";
    
    Node node1;
    Node node2;
    Node node3;
    
    String node1Url = "reds-tcp:"+ host + ":" + localPort1;
    String node2Url = "reds-tcp:"+ host + ":" + localPort2;
    String node3Url = "reds-tcp:"+ host + ":" + localPort3;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	    node1 = new Node(host, localPort1);
	    node2 = new Node(host, localPort2);
	    node3 = new Node(host, localPort3);
	    
	    node1.start();
	    node2.start();
	    node3.start();	    
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
		
		managers = node.getGroupCommunicationManagers();
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
		GroupCommunicationManager universeManager1 = 
				node1.getGroupCommunicationManagers().get(0);
		GroupCommunicationManager universeManager2 = 
			node2.getGroupCommunicationManagers().get(0);
		
		GroupDescriptor universe1 = universeManager1.getGroupDescriptor();
		GroupDescriptor universe2 = universeManager2.getGroupDescriptor();
		
		Assert.assertEquals(2, universe1.getMembers().size());
		Assert.assertEquals(2, universe2.getMembers().size());
		Assert.assertEquals(universe1.getLeader(), universe2.getLeader());
	}
	
}
