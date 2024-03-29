/**
 * 
 */
package it.polimi.rtag;

import java.util.ArrayList;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy.*;


import polimi.reds.NodeDescriptor;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class ProbabilisticLoadBalancingGroupCoordinationStrategyTest {

	GroupDescriptor localGroup;
	GroupDescriptor remoteGroup;
	
	NodeDescriptor localLeader;
	NodeDescriptor remoteLeader;
	
	ArrayList<NodeDescriptor> otherNodes;
	
	String friendlyGroupName = "__ASD__";
	
	GroupCoordinationStrategy localStrategy;
	GroupCoordinationStrategy remoteStrategy;
	
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
		
		// The number of followers with which the strategy 
		// behavior will be deterministic
		int deterministicThreshold = 1 + (int)Math.round(1.0 / 
				Math.min(FOLLOWER_MIGRATION_PROBABILITY, CONGESTED_GROUP_PROBABILITY));
		
		for (int i = 0; i < deterministicThreshold; i++) {
			otherNodes.add(new NodeDescriptor(false));
		}
		
		localStrategy = new ProbabilisticLoadBalancingGroupCoordinationStrategy(
				localGroup);
		remoteStrategy = new ProbabilisticLoadBalancingGroupCoordinationStrategy(
				remoteGroup);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	
	/**
	 * If one of the two groups is empty they should not merge.
	 * Instead the empty one should join the other.
	 * 
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldRequestToJoin(it.polimi.rtag.GroupDescriptor)}.
	 */
	@Test
	public void testShouldInviteToMerge_atLeastOneEmpty() {
		if (localLeader.getID().compareTo(remoteLeader.getID()) > 1) {
			Assert.assertTrue(localStrategy.shouldRequestToJoin(remoteGroup));
			Assert.assertFalse(remoteStrategy.shouldRequestToJoin(localGroup));
		} else {
			Assert.assertFalse(localStrategy.shouldRequestToJoin(remoteGroup));
			Assert.assertTrue(remoteStrategy.shouldRequestToJoin(localGroup));
		}
	}
	
	/**
	 * Even if empty if they are part of a hierarchy they do not merge
	 * unless they have the same parent.
	 * 
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldRequestToJoin(it.polimi.rtag.GroupDescriptor)}.
	 */
	@Test
	public void testShouldInviteToMerge_bothWithParent() {
		remoteGroup.setParentLeader(otherNodes.get(0));
		localGroup.setParentLeader(otherNodes.get(1));
		
		Assert.assertFalse(localStrategy.shouldRequestToJoin(remoteGroup));
		Assert.assertFalse(remoteStrategy.shouldRequestToJoin(localGroup));
		
		localGroup.setParentLeader(otherNodes.get(0));
		Assert.assertEquals(localStrategy.shouldRequestToJoin(remoteGroup),
				!remoteStrategy.shouldRequestToJoin(localGroup));
	}
	
	/**
	 * If only one has a parent the one which is not in a hierarchy
	 * should join the other.
	 * 
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldRequestToJoin(it.polimi.rtag.GroupDescriptor)}.
	 */
	@Test
	public void testShouldInviteToMerge_oneWithParent() {
		remoteGroup.setParentLeader(otherNodes.get(0));
		
		Assert.assertFalse(localStrategy.shouldRequestToJoin(remoteGroup));
		Assert.assertTrue(remoteStrategy.shouldRequestToJoin(localGroup));
	}
	
	/**
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldAcceptJoinRequest(it.polimi.rtag.GroupDescriptor)}.
	 */
	@Test
	public void testShouldAcceptToMerge() {
		Assert.assertTrue(localStrategy.shouldAcceptJoinRequest(remoteGroup.getLeader()));
		localGroup.setParentLeader(otherNodes.get(0));
		Assert.assertFalse(localStrategy.shouldAcceptJoinRequest(remoteGroup.getLeader()));
	}

	/**
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldAcceptToCreateAChild()}.
	 */
	@Test
	public void testShouldAcceptToCreateAChild() {
		Assert.assertTrue(localStrategy.shouldAcceptToCreateAChild());
	}

	/**
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldSuggestToMigrate(it.polimi.rtag.GroupDescriptor, polimi.reds.NodeDescriptor)}.
	 */
	@Test
	public void testShouldSuggestToMigrate() {
		localGroup.addFollower(otherNodes.get(0));
		localGroup.addFollower(otherNodes.get(1));
		remoteGroup.addFollower(otherNodes.get(2));
		Assert.assertFalse(localStrategy.shouldSuggestToMigrate(remoteGroup, otherNodes.get(2)));
		for (int i = 3; i < otherNodes.size(); i++) {
			remoteGroup.addFollower(otherNodes.get(i));
		}
		Assert.assertTrue(localStrategy.shouldSuggestToMigrate(remoteGroup, otherNodes.get(2)));
	}

	/**
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldAcceptToMigrate(it.polimi.rtag.GroupDescriptor)}.
	 */
	@Test
	public void testShouldAcceptToMigrate() {
		localGroup.addFollower(otherNodes.get(0));
		Assert.assertTrue(localStrategy.shouldAcceptToMigrate(remoteGroup));
		remoteGroup.addFollower(otherNodes.get(1));
		Assert.assertFalse(localStrategy.shouldAcceptToMigrate(remoteGroup));
	}

	/**
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldSplitToNode()}.
	 */
	@Test
	public void testShouldSplitToNode() {
		Assert.assertNull(localStrategy.shouldSplitToNode());
		for (NodeDescriptor node: otherNodes) {
			localGroup.addFollower(node);
		}
		Assert.assertNotNull(localStrategy.shouldSplitToNode());
	}

	/**
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#electNewLeader()}.
	 */
	@Test
	public void testElectNewLeader() {
		Assert.assertNull(localStrategy.electNewLeader());
		localGroup.setLeader(null);
		Assert.assertNull(localStrategy.electNewLeader());
		localGroup.addFollower(otherNodes.get(0));
		Assert.assertEquals(otherNodes.get(0),
				localStrategy.electNewLeader());
	}

}
