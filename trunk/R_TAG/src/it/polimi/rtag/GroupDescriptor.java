/**
 * 
 */
package it.polimi.rtag;

import java.io.Serializable;
import java.util.ArrayList;

import polimi.reds.NodeDescriptor;

/**
 * @author panteha
 * Describes a group within the system
 */
public class GroupDescriptor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -598794056247356570L;

	private String uniqueId;
	private String friendlyName;
	
	private LeaderDescriptor leader;
	private ArrayList<FollowerDescriptor> followers;
	
	private NodeDescriptor parent;
	private ArrayList<GroupDescriptor> children;

	
	/**
	 * @param uniqueId
	 * @param friendlyName
	 * @param leader
	 */
	public GroupDescriptor(String uniqueId, String friendlyName,
			LeaderDescriptor leader) {
		this.uniqueId = uniqueId;
		this.friendlyName = friendlyName;
		this.leader = leader;
	}
	
	/**
	 * @param uniqueId
	 * @param friendlyName
	 * @param leader
	 * @param parent
	 */
	public GroupDescriptor(String uniqueId, String friendlyName,
			LeaderDescriptor leader, NodeDescriptor parent) {
		this.uniqueId = uniqueId;
		this.friendlyName = friendlyName;
		this.leader = leader;
		this.parent = parent;
	}	
	
	/**
	 * @return the uniqueId
	 */
	public String getUniqueId() {
		return uniqueId;
	}
	/**
	 * @param uniqueId the uniqueId to set
	 */
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	/**
	 * @return the friendlyName
	 */
	public String getFriendlyName() {
		return friendlyName;
	}
	/**
	 * @param friendlyName the friendlyName to set
	 */
	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}
	/**
	 * @return the leader
	 */
	public LeaderDescriptor getLeader() {
		return leader;
	}
	/**
	 * @param leader the leader to set
	 */
	public void setLeader(LeaderDescriptor leader) {
		this.leader = leader;
	}
	/**
	 * @return the parent
	 */
	public NodeDescriptor getParent() {
		return parent;
	}
	/**
	 * @param parent the parent to set
	 */
	public void setParent(NodeDescriptor parent) {
		this.parent = parent;
	}
	/**
	 * @return the members
	 */
	public ArrayList<FollowerDescriptor> getFollowers() {
		return followers;
	}
	/**
	 * @return the children
	 */
	public ArrayList<GroupDescriptor> getChildren() {
		return children;
	}

	public boolean isLeader(NodeDescriptor currentDescriptor) {
		// TODO check if the equals methods works as we expect
		return leader.equals(currentDescriptor);
	}

	public boolean isMember(NodeDescriptor currentDescriptor) {
		return isLeader(currentDescriptor) || isFollower(currentDescriptor);
	}
	
	public boolean isFollower(NodeDescriptor currentDescriptor) {
		for (NodeDescriptor node: followers) {
			if (node.equals(currentDescriptor)) {
				return true;
			}
		}
		return false;
	}
}
