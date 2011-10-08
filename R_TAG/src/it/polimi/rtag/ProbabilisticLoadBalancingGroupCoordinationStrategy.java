/**
 * 
 */
package it.polimi.rtag;

import polimi.reds.NodeDescriptor;

/**
 * To distribute load on groups, avoid congestion.
 * 
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class ProbabilisticLoadBalancingGroupCoordinationStrategy
		extends LoadBalancingGroupCoordinationStrategy {

	public ProbabilisticLoadBalancingGroupCoordinationStrategy(
			GroupDescriptor groupDescriptor) {
		super(groupDescriptor);
	}

	/**
	 * If a remote leader ask this node to join its group, the node will accept with a
	 * probability which is the number of nodes in this group multiplied by this
	 * constant. 
	 */
	public static final double FOLLOWER_MIGRATION_PROBABILITY = 0.05;
	
	/**
	 * Every time a follower joins this group the leader will split the group
	 * with a probability which is the number of followers multiplied by this constant. 
	 */
	public static final double CONGESTED_GROUP_PROBABILITY = 0.1;


	/* (non-Javadoc)
	 * @see it.polimi.rtag.GroupCoordinationStrategy#shouldSuggestToMigrate(it.polimi.rtag.GroupDescriptor)
	 */
	@Override
	public boolean shouldSuggestToMigrate(GroupDescriptor remoteGroup, NodeDescriptor nodeDescriptor) {		
		if (groupDescriptor.getFollowers().size() > 
				remoteGroup.getFollowers().size()) {
			return false;
		} else {
			return Math.random() <= FOLLOWER_MIGRATION_PROBABILITY * remoteGroup.getFollowers().size();
		}
	}

	@Override
	public NodeDescriptor shouldSplitToNode() {
		if (Math.random() > groupDescriptor.getFollowers().size() * CONGESTED_GROUP_PROBABILITY) {
			return null;
		} else {
			// we select a random child
			int index = (int)Math.round(Math.random() * (groupDescriptor.getFollowers().size() - 1));
			return groupDescriptor.getFollowers().get(index);
		}
	}

}
