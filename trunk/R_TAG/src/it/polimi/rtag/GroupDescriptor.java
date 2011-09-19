/**
 * 
 */
package it.polimi.rtag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import lights.Tuple;

import polimi.reds.NodeDescriptor;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)</p>.
 * 
 * Describes a group within the system
 */
public class GroupDescriptor implements Serializable {

	

	/**
	 * 
	 */
	private static final long serialVersionUID = -598794056247356570L;
	private static final String UNIVERSE = "__UNIVERSE";
	private String uniqueId;
	private String friendlyName;
	private Tuple description;
	private boolean universe = false;
	
	/**
	 * The group leader.
	 */
	private NodeDescriptor leader;
	
	/**
	 * All the followers. 
	 */
	private HashSet<NodeDescriptor> followers;
	
	/**
	 * The parent group or null if the group is top level. 
	 */
	private NodeDescriptor parentLeader;

	
	//TODO add a constructor to create universe groups
	
	/**
	 * @param uniqueId
	 * @param friendlyName
	 * @param leader
	 */
	public GroupDescriptor(String uniqueId, String friendlyName,
			NodeDescriptor leader, Tuple description) {
		this.uniqueId = uniqueId;
		this.friendlyName = friendlyName;
		this.leader = leader;
		this.description = description;
	}
	
	/**
	 * @param uniqueId
	 * @param friendlyName
	 * @param leader
	 * @param parentGroup
	 */
	public GroupDescriptor(String uniqueId, String friendlyName,
			NodeDescriptor leader, Tuple description, NodeDescriptor parentLeader) {
		this.uniqueId = uniqueId;
		this.friendlyName = friendlyName;
		this.leader = leader;
		this.description = description;
		this.parentLeader = parentLeader;
	}
	
	/**
	 * @param uniqueId
	 * @param friendlyName
	 * @param description
	 * @param universe
	 * @param leader
	 * @param parentLeader
	 */
	private GroupDescriptor(String uniqueId, String friendlyName,
			Tuple description, boolean universe, NodeDescriptor leader,
			NodeDescriptor parentLeader) {
		super();
		this.uniqueId = uniqueId;
		this.friendlyName = friendlyName;
		this.description = description;
		this.universe = universe;
		this.leader = leader;
		this.parentLeader = parentLeader;
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
	public NodeDescriptor getLeader() {
		return leader;
	}
	/**
	 * @param leader the leader to set
	 */
	public void setLeader(NodeDescriptor leader) {
		this.leader = leader;
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

	/**
	 * @return the universe
	 */
	public boolean isUniverse() {
		return universe;
	}

	/**
	 * @return the followers
	 */
	public HashSet<NodeDescriptor> getFollowers() {
		return followers;
	}

	/**
	 * @return the parentGroup
	 */
	public NodeDescriptor getParentLeader() {
		return parentLeader;
	}

	/**
	 * @return the description
	 */
	public Tuple getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Tuple) {
			return description.equals(obj);
		} else if (obj instanceof GroupDescriptor) {
			return description.equals(((GroupDescriptor)obj).description);
		} else {
			return super.equals(obj);
		}
	}

	public HashSet<NodeDescriptor> getMembers() {
		HashSet<NodeDescriptor> memebers = new HashSet<NodeDescriptor>(followers);
		memebers.add(leader);
		return memebers;
		
	}

	public static GroupDescriptor createUniverse(Node node) {
		// TODO think of a unique id
		return new GroupDescriptor("uniqueid", UNIVERSE, null, true, node.getID(), null);
	}
}
