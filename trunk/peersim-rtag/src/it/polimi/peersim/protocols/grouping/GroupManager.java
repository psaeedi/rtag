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

	private String name;
	private Node ownerNode;
	private GroupDescriptor followedGroup;
	private GroupDescriptor leadedGroup;
	
	private Node leaderBeingJoined;
	private int leaderBeingJoinedCycle = -1;
	
	public GroupManager(String name, Node ownerNode) {
		super();
		this.name = name;
		this.ownerNode = ownerNode;
		
	}

	public GroupManager(GroupManager oldgroupmanager) {
		this.name = oldgroupmanager.name;
		this.ownerNode = oldgroupmanager.ownerNode;
	}

	public GroupDescriptor getFollowedGroup() {
		return followedGroup;
	}
	
	public void setFollowedGroup(GroupDescriptor followedGroup) {
		this.followedGroup = followedGroup;
	}
	
	public GroupDescriptor getOrCreateLeadedGroup() {
		GroupDescriptor leadedGroup = getLeadedGroup();
		if (leadedGroup == null) {
			leadedGroup = new GroupDescriptor(UUID.randomUUID(), name, ownerNode);
		}
		return leadedGroup;
	}

	public GroupDescriptor getLeadedGroup() {
		return leadedGroup;
	}
	
	public void setLeadedGroup(GroupDescriptor leadedGroup) {
		this.leadedGroup = leadedGroup;
	}

	public Node getLeaderBeingJoined() {
		return leaderBeingJoined;
	}

	public void setLeaderBeingJoined(Node leaderBeingJoined, int cycle) {
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
