/**
 * 
 */
package it.polimi.peersim.protocols.grouping;


import java.util.UUID;

import peersim.core.Node;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 */
public class GroupManager {

	private final GroupingProtocol protocol;
	
	private String name;
	private Node ownerNode;
	private GroupDescriptor followedGroup;
	private GroupDescriptor leadedGroup;
	
	private Node leaderBeingJoined;
	private int leaderBeingJoinedCycle = -1;
	
	public GroupManager(String name, Node ownerNode, GroupingProtocol protocol) {
		super();
		this.name = name;
		this.ownerNode = ownerNode;
		this.protocol = protocol;
	}

	public GroupDescriptor getFollowedGroup() {
		return followedGroup;
	}
	
	public void setFollowedGroup(GroupDescriptor followedGroup) {
		if (followedGroup == null) {
			throw new AssertionError("Cannot follow null group.");
		}
		if (this.followedGroup != null &&
				this.followedGroup.getLeader().getID() != followedGroup.getLeader().getID()) {
			throw new AssertionError("Already following " + this.followedGroup + 
					" while setting " + followedGroup);
		}
		this.followedGroup = followedGroup;
		if (leadedGroup != null) {
			leadedGroup.setParentLeader(this.followedGroup.getLeader());
			
		}
	}
	
	public void resetFollowedGroup() {
		this.followedGroup = null;
	}
	
	public GroupDescriptor getOrCreateLeadedGroup() {
		GroupDescriptor leadedGroup = getLeadedGroup();
		if (leadedGroup == null) {
			leadedGroup = new GroupDescriptor(UUID.randomUUID(), name, ownerNode);
			if (followedGroup != null) {
				leadedGroup.setParentLeader(followedGroup.getLeader());
			}
		}
		return leadedGroup;
	}

	public GroupDescriptor getLeadedGroup() {
		return leadedGroup;
	}
	
	public void setLeadedGroup(GroupDescriptor leadedGroup) {
		if (this.leadedGroup != null && this.leadedGroup != leadedGroup) {
			throw new AssertionError("Already leading another group.");
		}
		if (leadedGroup == null) {
			throw new AssertionError("Cannot lead null group.");
		}
		this.leadedGroup = leadedGroup;
	}
	
	public void resetLeadedGroup() {
		this.leadedGroup = null;
	}

	public Node getLeaderBeingJoined() {
		return leaderBeingJoined;
	}

	public void setLeaderBeingJoined(Node leaderBeingJoined, int cycle) {
		if (this.leaderBeingJoined != null) {
			throw new AssertionError("Already joining another leader.");
		}
		if (leaderBeingJoined == null) {
			throw new AssertionError("Cannot follow null leader.");
		}
		this.leaderBeingJoined = leaderBeingJoined;
		this.leaderBeingJoinedCycle = cycle;
	}

	public int getLeaderBeingJoinedCycle() {
		return leaderBeingJoinedCycle;
	}
	
	public void resetLeaderBeingJoinedCycle() {
		this.leaderBeingJoined = null;
		this.leaderBeingJoinedCycle = -1;
	}
	
	
}
