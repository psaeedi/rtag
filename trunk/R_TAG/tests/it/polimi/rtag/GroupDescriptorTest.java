/**
 * 
 */
package it.polimi.rtag;

import static org.junit.Assert.*;

import java.util.UUID;

import junit.framework.Assert;
import lights.Tuple;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class GroupDescriptorTest {

	int localPort1 = 10001;
    int localPort2 = 10002;
    int localPort3 = 10003;
    int localPort4 = 10004;
    
    String host = "localhost";
    
    Node node1;
    Node node2;
    Node node3;
    Node node4;
    
    Tuple o;
	GroupDescriptor pop;
	GroupDescriptor top;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	    node1 = new Node(host, localPort1);
	    node2 = new Node(host, localPort2);
	    node3 = new Node(host, localPort3);
	    node4 = new Node(host, localPort4);
	    

		pop = new GroupDescriptor(UUID.randomUUID(), "Bob", node1.getID(), o);
		top = new GroupDescriptor(UUID.randomUUID(), "Top", node1.getID(), o);
	}

	/**
	 * Test method for {@link it.polimi.rtag.GroupDescriptor#isLeader(polimi.reds.NodeDescriptor)}.
	 */
	@Test
	public void testIsLeader() {
		Assert.assertTrue(pop.isLeader(node1.getID()));
	}

	/**
	 * Test method for {@link it.polimi.rtag.GroupDescriptor#isFollower(polimi.reds.NodeDescriptor)}.
	 */
	@Test
	public void testIsFollower() {
		pop.addFollower(node4.getID());
		pop.addFollower(node2.getID());
		
		Assert.assertFalse(pop.isFollower(node1.getID()));
		Assert.assertEquals(3, pop.getMembers().size());
		Assert.assertEquals(1, top.getMembers().size());
		Assert.assertTrue(pop.isFollower(node4.getID()));
	}

}
