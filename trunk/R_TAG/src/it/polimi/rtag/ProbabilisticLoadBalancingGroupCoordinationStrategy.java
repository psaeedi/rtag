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
public class ProbabilisticLoadBalancingGroupCoordinationStrategy implements
		GroupCoordinationStrategy {

	/**
	 * If a remote leader ask this node to join its group, the node will accept with a
	 * probability which is the number of nodes in this group multiplied by this
	 * constant. 
	 */
	public static final double FOLLOWER_MIGRATION_PROBABILITY = 0.3;
	
	/**
	 * Every time a follower joins this group the leader will split the group
	 * with a probability which is the number of followers multiplied by this constant. 
	 */
	public static final double CONGESTED_GROUP_PROBABILITY = 0.2;
	private GroupDescriptor groupDescriptor;
	
	/**
	 * @param groupDescriptor
	 */
	public ProbabilisticLoadBalancingGroupCoordinationStrategy(
			GroupDescriptor groupDescriptor) {
		this.groupDescriptor = groupDescriptor;
	}
	
	/* (non-Javadoc)
	 * @see it.polimi.rtag.GroupCoordinationStrategy#shouldInviteToJoin(it.polimi.rtag.GroupDescriptor)
	 */
	@Override
	public boolean shouldInviteToJoin(GroupDescriptor remoteGroup) {
		NodeDescriptor localLeader = groupDescriptor.getLeader();
		NodeDescriptor localParent = groupDescriptor.getParentLeader();
		NodeDescriptor remoteParent = remoteGroup.getParentLeader();
		NodeDescriptor remoteLeader = remoteGroup.getLeader();
		
		if (groupDescriptor.isFollower(remoteLeader) || 
				remoteGroup.isFollower(localLeader)) {
			// Already in a hierarchy
			return false;
		}
		
		if (localParent != null && remoteParent == null) {
			if (localParent.equals(remoteGroup.getLeader())) {
				// The remote leader was the local parent.
				// This should not be happening
				// TODO think how to avoid the creation of rings
				return false;
			}
			// The local group is already part of a chain while the remote one is not
			if (remoteGroup.getFollowers().size() == 0) {
				return true;
			} else {
				return false;
			}
		} else if (localParent == null && remoteParent != null) {
			// This node will wait for the remote node to invite it
			// since the remote node is part of a chain.
			return false;
		} else if (localParent != null && remoteParent != null) {
			// They are both part of chain
			// Either we forward the events to their parents or we do nothing.
			// TODO think which one will be the best solution.
			return false;
		} else { // localParent == null && remoteParent == null
			// Neither the local or the remote group are part of a hierarchy
			// Therefore we can select what to do depending on the number
			// of followers
			int localFollowersCount = groupDescriptor.getFollowers().size();
			int remoteFollowersCount = remoteGroup.getFollowers().size();
			
			if (localFollowersCount == 0 && remoteFollowersCount == 0) {
				return selectLowerId(localLeader, remoteLeader);
			} else if (localFollowersCount != 0 && remoteFollowersCount == 0) {
				return true;
			} else if (localFollowersCount == 0 && remoteFollowersCount != 0) {
				return false;
			} else {
				// Both of them have followers
				return false;
			}
		}
	}

	/* (non-Javadoc)
	 * @see it.polimi.rtag.GroupCoordinationStrategy#shouldInviteToMerge(it.polimi.rtag.GroupDescriptor)
	 */
	@Override
	public boolean shouldInviteToMerge(GroupDescriptor remoteGroup) {
		NodeDescriptor localLeader = groupDescriptor.getLeader();
		NodeDescriptor localParent = groupDescriptor.getParentLeader();
		NodeDescriptor remoteParent = remoteGroup.getParentLeader();
		NodeDescriptor remoteLeader = remoteGroup.getLeader();
		
		if (groupDescriptor.isFollower(remoteLeader) || 
				remoteGroup.isFollower(localLeader)) {
			// Already in a hierarchy
			return false;
		}
		
		if (localParent != null && remoteParent == null) {
			if (localParent.equals(remoteGroup.getLeader())) {
				// The remote leader was the local parent.
				// This should not be happening
				// TODO think how to avoid the creation of rings
				return false;
			}
			// The local group is already part of a chain while the remote one is not
			if (remoteGroup.getFollowers().size() > 0) {
				return false;
			} else {
				return true;
			}
		} else if (localParent == null && remoteParent != null) {
			// This node will wait for the remote node to invite it
			// since the remote node is part of a chain.
			return false;
		} else if (localParent != null && remoteParent != null) {
			// They are both part of chain
			// Either we forward the events to their parents or we do nothing.
			// TODO think which one will be the best solution.
			if (localParent.equals(remoteParent)) {
				return selectLowerId(localLeader, remoteLeader);
			} else {
				return false;
			}
		} else { // localParent == null && remoteParent == null
			// Neither the local or the remote group are part of a hierarchy
			// Therefore we can select what to do depending on the number
			// of followers
			int localFollowersCount = groupDescriptor.getFollowers().size();
			int remoteFollowersCount = remoteGroup.getFollowers().size();
			if (localFollowersCount == 0 || remoteFollowersCount == 0) {
				return false;
			} else {
			
				//----------
				if (localFollowersCount < remoteFollowersCount) {
					return true;
				} else if (localFollowersCount > remoteFollowersCount) {
					// The current leader wait for the other one to invite it5
					return false;
				} else { 
					// they have the same number of followers or no followers
					// we need another logic to decide whom will invite the other
					
					return selectLowerId(localLeader, remoteLeader);
				}	
			}
		}
	}

	/**
	 * @param nodeA
	 * @param nodeB
	 * @return
	 */
	private boolean selectLowerId(NodeDescriptor nodeA,
			NodeDescriptor nodeB) {
		// The one with the higher id will send the invitation
		if (nodeA.getID().compareTo(nodeB.getID()) > 0) {
			return false;
		} else {
			return true;
		}
	}

	/* (non-Javadoc)
	 * @see it.polimi.rtag.GroupCoordinationStrategy#shouldAcceptToJoin(it.polimi.rtag.GroupDescriptor)
	 */
	@Override
	public boolean shouldAcceptToJoin(GroupDescriptor remoteGroup) {
		// True if the group has no followers
		return (groupDescriptor.getFollowers().size() == 0);
	}

	/* (non-Javadoc)
	 * @see it.polimi.rtag.GroupCoordinationStrategy#shouldAcceptToMerge(it.polimi.rtag.GroupDescriptor)
	 */
	@Override
	public boolean shouldAcceptToMerge(GroupDescriptor remoteGroup) {
		if (groupDescriptor.isFollower(remoteGroup.getLeader())) {
			// The remote group is a child group
			return false;
		}
		if (remoteGroup.isFollower(groupDescriptor.getLeader())) {
			// The current is a child group
			return false;
		}
		
		// If the parent is null OK otherwise KO
		return (groupDescriptor.getParentLeader() == null);
	}


	/* (non-Javadoc)
	 * @see it.polimi.rtag.GroupCoordinationStrategy#shouldAcceptToCreateAChild()
	 */
	@Override
	public boolean shouldAcceptToCreateAChild() {
		return true;
	}

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

	/* (non-Javadoc)
	 * @see it.polimi.rtag.GroupCoordinationStrategy#shouldAcceptToMigrate(it.polimi.rtag.GroupDescriptor)
	 */
	@Override
	public boolean shouldAcceptToMigrate(GroupDescriptor remoteGroup) {
		return groupDescriptor.getFollowers().size() > 
		remoteGroup.getFollowers().size();
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

	@Override
	public NodeDescriptor electNewLeader() {
		if (groupDescriptor.getLeader() != null ||
				groupDescriptor.getFollowers().size() == 0) {
			return null; 
		}
		return groupDescriptor.getFollowers().get(0);
	}

}
