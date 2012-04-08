/**
 * 
 */
package it.polimi.peersim.prtag;

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
	
	
}
