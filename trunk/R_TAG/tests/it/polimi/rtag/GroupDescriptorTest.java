/**
 * 
 */
package it.polimi.rtag;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import polimi.reds.NodeDescriptor;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class GroupDescriptorTest {

	GroupDescriptor localGroup;
	GroupDescriptor remoteGroup;
	
	NodeDescriptor localLeader;
	NodeDescriptor remoteLeader;
	
	ArrayList<NodeDescriptor> otherNodes;
	
	String friendlyGroupName = "__ASD__";
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {	
		localLeader = new NodeDescriptor(false);
		remoteLeader = new NodeDescriptor(false);
		
		localGroup = new GroupDescriptor(UUID.randomUUID(), 
				friendlyGroupName, localLeader, null);
		remoteGroup = new GroupDescriptor(UUID.randomUUID(),
				friendlyGroupName, remoteLeader, null);
		
		otherNodes = new ArrayList<NodeDescriptor>();
		for (int i = 0; i < 50; i++) {
			otherNodes.add(new NodeDescriptor(false));
		}
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link it.polimi.rtag.GroupDescriptor#isLeader(polimi.reds.NodeDescriptor)}.
	 */
	@Test
	public void testIsLeader() {
		Assert.assertTrue(localGroup.isLeader(localLeader));
		Assert.assertFalse(localGroup.isLeader(remoteLeader));
		Assert.assertFalse(remoteGroup.isLeader(localLeader));

	}

	/**
	 * Test method for {@link it.polimi.rtag.GroupDescriptor#isMember(polimi.reds.NodeDescriptor)}.
	 */
	@Test
	public void testIsMember() {
		Assert.assertTrue(localGroup.isMember(localLeader));
		NodeDescriptor otherNode0 = otherNodes.get(0);
		NodeDescriptor otherNode1 = otherNodes.get(1);
		Assert.assertFalse(localGroup.isMember(otherNode0));
		localGroup.addFollower(otherNode0);
		Assert.assertTrue(localGroup.isMember(otherNode0));
		Assert.assertFalse(localGroup.isMember(otherNode1));
	}

	/**
	 * Test method for {@link it.polimi.rtag.GroupDescriptor#isFollower(polimi.reds.NodeDescriptor)}.
	 */
	@Test
	public void testIsFollower() {
		Assert.assertFalse(localGroup.isFollower(localLeader));
		NodeDescriptor otherNode0 = otherNodes.get(0);
		NodeDescriptor otherNode1 = otherNodes.get(1);
		Assert.assertFalse(localGroup.isFollower(otherNode0));
		localGroup.addFollower(otherNode0);
		Assert.assertTrue(localGroup.isFollower(otherNode0));
		Assert.assertFalse(localGroup.isFollower(otherNode1));
	}

	/**
	 * Test method for {@link it.polimi.rtag.GroupDescriptor#isUniverse()}.
	 */
	@Test
	public void testIsUniverse() {
		Assert.assertFalse(localGroup.isUniverse());
	}

	/**
	 * Test method for {@link it.polimi.rtag.GroupDescriptor#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject_simmetry() {
		Assert.assertTrue(localGroup.equals(localGroup));
		Assert.assertFalse(localGroup.equals(remoteGroup));

		GroupDescriptor localClone = new GroupDescriptor(localGroup.getUniqueId(), 
				localGroup.getFriendlyName(), localGroup.getLeader());
		
		// Simmetry
		Assert.assertTrue(localGroup.equals(localClone));
		Assert.assertTrue(localClone.equals(localGroup));
	}
	
	/**
	 * Test method for {@link it.polimi.rtag.GroupDescriptor#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject_followers() {
		GroupDescriptor localClone = new GroupDescriptor(localGroup.getUniqueId(), 
				localGroup.getFriendlyName(), localGroup.getLeader());
		
		// Followers
		localClone.addFollower(otherNodes.get(0));
		Assert.assertFalse(localGroup.equals(localClone));
		localGroup.addFollower(otherNodes.get(0));
		Assert.assertTrue(localGroup.equals(localClone));
		localClone.removeFollower(otherNodes.get(0));
		localClone.addFollower(otherNodes.get(1));
		Assert.assertFalse(localGroup.equals(localClone));		
	}
	
	/**
	 * Test method for {@link it.polimi.rtag.GroupDescriptor#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject_leaders() {
		GroupDescriptor localClone = new GroupDescriptor(localGroup.getUniqueId(), 
				localGroup.getFriendlyName(), localGroup.getLeader());

		// Leaders
		localClone.setLeader(null);
		Assert.assertFalse(localGroup.equals(localClone));
		Assert.assertFalse(localClone.equals(localGroup));
		localGroup.setLeader(null);
		Assert.assertTrue(localGroup.equals(localClone));		
	}

	/**
	 * Test method for {@link it.polimi.rtag.GroupDescriptor#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject_parents() {
		GroupDescriptor localClone = new GroupDescriptor(localGroup.getUniqueId(), 
				localGroup.getFriendlyName(), localGroup.getLeader());
		
		localClone.setParentLeader(remoteLeader);
		Assert.assertFalse(localClone.equals(localGroup));
		Assert.assertFalse(localGroup.equals(localClone));
		localGroup.setParentLeader(remoteLeader);
		Assert.assertTrue(localGroup.equals(localClone));
	}
	
	/**
	 * Test method for {@link it.polimi.rtag.GroupDescriptor#hasSameName(it.polimi.rtag.GroupDescriptor)}.
	 */
	@Test
	public void testHasSameName() {
		Assert.assertTrue(localGroup.hasSameName(remoteGroup));
	}

	/**
	 * Test method for {@link it.polimi.rtag.GroupDescriptor#addFollower(polimi.reds.NodeDescriptor)}.
	 */
	@Test
	public void testAddFollower() {
		NodeDescriptor otherNode0 = otherNodes.get(0);
		Assert.assertFalse(localGroup.isFollower(otherNode0));
		localGroup.addFollower(otherNode0);
		Assert.assertTrue(localGroup.isFollower(otherNode0));
	}

	/**
	 * Test method for {@link it.polimi.rtag.GroupDescriptor#removeFollower(polimi.reds.NodeDescriptor)}.
	 */
	@Test
	public void testRemoveFollower() {
		NodeDescriptor otherNode0 = otherNodes.get(0);
		Assert.assertFalse(localGroup.isFollower(otherNode0));
		localGroup.addFollower(otherNode0);
		Assert.assertTrue(localGroup.isFollower(otherNode0));
		localGroup.removeFollower(otherNode0);
		Assert.assertFalse(localGroup.isFollower(otherNode0));
	}

}
