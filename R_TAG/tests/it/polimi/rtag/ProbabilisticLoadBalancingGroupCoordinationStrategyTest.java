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
		for (int i = 0; i < 10; i++) {
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
	}
	
	/**
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldAcceptToJoin(it.polimi.rtag.GroupDescriptor)}.
	 */
	@Test
	public void testShouldAcceptToJoin() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldAcceptToMerge(it.polimi.rtag.GroupDescriptor)}.
	 */
	@Test
	public void testShouldAcceptToMerge() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldAcceptToCreateAChild()}.
	 */
	@Test
	public void testShouldAcceptToCreateAChild() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldSuggestToMigrate(it.polimi.rtag.GroupDescriptor, polimi.reds.NodeDescriptor)}.
	 */
	@Test
	public void testShouldSuggestToMigrate() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldAcceptToMigrate(it.polimi.rtag.GroupDescriptor)}.
	 */
	@Test
	public void testShouldAcceptToMigrate() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#shouldSplitToNode()}.
	 */
	@Test
	public void testShouldSplitToNode() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link it.polimi.rtag.ProbabilisticLoadBalancingGroupCoordinationStrategy#electNewLeader()}.
	 */
	@Test
	public void testElectNewLeader() {
		fail("Not yet implemented");
	}

}
