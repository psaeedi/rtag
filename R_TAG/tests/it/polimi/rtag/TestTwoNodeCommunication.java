package it.polimi.rtag;


import it.polimi.rtag.messaging.TupleMessage;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;

import junit.framework.Assert;
import lights.Tuple;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import polimi.reds.Filter;
import polimi.reds.NodeDescriptor;
import polimi.reds.broker.overlay.AlreadyNeighborException;
import polimi.reds.broker.overlay.NotRunningException;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class TestTwoNodeCommunication {

   
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
	public void testCreatGroupAndCheckLeader() {
	
		Tuple o = null;
		GroupDescriptor pop = node1.createGroupAndNotifyUniverse(node1Url,"bob",o);
		GroupDescriptor top = node2.createGroupAndNotifyUniverse(node2Url,"jack",o);
	
		 pop.getFollowers().add(node4.currentDescriptor);
		 pop.getFollowers().add(node2.currentDescriptor);
		
		  
	
		Assert.assertTrue(pop.isLeader(node1.currentDescriptor));
		Assert.assertFalse(pop.isFollower(node1.currentDescriptor));
		Assert.assertEquals(3, pop.getMembers().size());
		Assert.assertEquals(1, top.getMembers().size());
		Assert.assertTrue(pop.isFollower(node4.currentDescriptor));
	}
	

	
	
}
