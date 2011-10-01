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
	 * When two groups are both empty only one
	 * should invite the other to join.
	 * 
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldInviteToJoin(it.polimi.rtag.GroupDescriptor)}.
	 */
	@Test
	public void testShouldInviteToJoin_bothEmpty() {
		Assert.assertEquals(localStrategy.shouldInviteToJoin(remoteGroup),
				!remoteStrategy.shouldInviteToJoin(localGroup));
	}

	/**
	 * The empty one should join the other.
	 * 
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldInviteToJoin(it.polimi.rtag.GroupDescriptor)}.
	 */
	@Test
	public void testShouldInviteToJoin_oneEmpty() {
		remoteGroup.addFollower(otherNodes.get(0));
		
		Assert.assertFalse(localStrategy.shouldInviteToJoin(remoteGroup));
		Assert.assertTrue(remoteStrategy.shouldInviteToJoin(localGroup));
	}

	/**
	 * Even if empty if they are part of a hierarchy they do not join
	 * 
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldInviteToJoin(it.polimi.rtag.GroupDescriptor)}.
	 */
	@Test
	public void testShouldInviteToJoin_bothWithParent() {
		remoteGroup.setParentLeader(otherNodes.get(0));
		localGroup.setParentLeader(otherNodes.get(1));
		
		Assert.assertFalse(localStrategy.shouldInviteToJoin(remoteGroup));
		Assert.assertFalse(remoteStrategy.shouldInviteToJoin(localGroup));
	}
	
	/**
	 * If one of the two groups is empty they should not merge.
	 * Instead the empty one should join the other.
	 * 
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldInviteToMerge(it.polimi.rtag.GroupDescriptor)}.
	 */
	@Test
	public void testShouldInviteToMerge_atLeastOneEmpty() {
		Assert.assertFalse(localStrategy.shouldInviteToMerge(remoteGroup));
		Assert.assertFalse(remoteStrategy.shouldInviteToMerge(localGroup));
		
		remoteGroup.addFollower(otherNodes.get(0));
		Assert.assertFalse(localStrategy.shouldInviteToMerge(remoteGroup));
		Assert.assertFalse(remoteStrategy.shouldInviteToMerge(localGroup));
		
		localGroup.setParentLeader(otherNodes.get(1));
		Assert.assertFalse(localStrategy.shouldInviteToMerge(remoteGroup));
		Assert.assertFalse(remoteStrategy.shouldInviteToMerge(localGroup));
	}
	
	/**
	 * Even if empty if they are part of a hierarchy they do not merge
	 * unless they have the same parent.
	 * 
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldInviteToMerge(it.polimi.rtag.GroupDescriptor)}.
	 */
	@Test
	public void testShouldInviteToMerge_bothWithParent() {
		remoteGroup.setParentLeader(otherNodes.get(0));
		localGroup.setParentLeader(otherNodes.get(1));
		
		Assert.assertFalse(localStrategy.shouldInviteToMerge(remoteGroup));
		Assert.assertFalse(remoteStrategy.shouldInviteToMerge(localGroup));
		
		localGroup.setParentLeader(otherNodes.get(0));
		Assert.assertEquals(localStrategy.shouldInviteToMerge(remoteGroup),
				!remoteStrategy.shouldInviteToMerge(localGroup));
	}
	
	/**
	 * If only one has a parent the one which is not in a hierarchy
	 * should join the other.
	 * 
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldInviteToMerge(it.polimi.rtag.GroupDescriptor)}.
	 */
	@Test
	public void testShouldInviteToMerge_oneWithParent() {
		remoteGroup.setParentLeader(otherNodes.get(0));
		
		Assert.assertFalse(localStrategy.shouldInviteToMerge(remoteGroup));
		Assert.assertTrue(remoteStrategy.shouldInviteToMerge(localGroup));
	}
	
	/**
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldAcceptToJoin(it.polimi.rtag.GroupDescriptor)}.
	 */
	@Test
	public void testShouldAcceptToJoin() {
		Assert.assertTrue(localStrategy.shouldAcceptToJoin(remoteGroup));
		localGroup.addFollower(otherNodes.get(0));
		Assert.assertFalse(localStrategy.shouldAcceptToJoin(remoteGroup));
	}

	/**
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldAcceptToMerge(it.polimi.rtag.GroupDescriptor)}.
	 */
	@Test
	public void testShouldAcceptToMerge() {
		Assert.assertTrue(localStrategy.shouldAcceptToMerge(remoteGroup));
		localGroup.setParentLeader(otherNodes.get(0));
		Assert.assertFalse(localStrategy.shouldAcceptToMerge(remoteGroup));
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
		Assert.assertFalse(localStrategy.shouldSuggestToMigrate(remoteGroup, otherNodes.get(2)));
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
