/**
 * 
 */
package it.polimi.peersim.prtag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import peersim.core.Node;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 */
public class GroupDescriptor implements Serializable{

	private static final long serialVersionUID = 2887953880462731123L;

	public static final String APPGROUP = "_APPGROUP";
	
	private UUID uniqueId;
	
	private String hierarchyName;
	
	private Node leader;
	
	private boolean appgroup = false;	
	
	private ArrayList<Node> followers = new ArrayList<Node>();
	
	/**
	 * The parent group or null if the group is top level. 
	 */
	private Node parentLeader;
	
	/**
	 * @param uniqueId
	 * @param friendlyName
	 * @param leader
	 */
	public GroupDescriptor(UUID uniqueId, String friendlyName,
			Node leader) {
		this.uniqueId = uniqueId;
		this.hierarchyName = friendlyName;
		this.leader = leader;
		this.appgroup = APPGROUP.equals(friendlyName);
	}
	
	/**
	 * @param uuid
	 * @param friendlyName
	 * @param description
	 * @param universe
	 * @param leader
	 * @param parentLeader
	 */
	public GroupDescriptor(UUID uuid, String friendlyName, 
			Node leader,
			Node parentLeader) {
		super();
		this.uniqueId = uuid;
		this.hierarchyName = friendlyName;
		this.appgroup = APPGROUP.equals(friendlyName);
		this.leader = leader;
		this.parentLeader = parentLeader;
	}
	
	public GroupDescriptor(GroupDescriptor oldDescriptor) {
		super();
		this.uniqueId = oldDescriptor.uniqueId;
		this.hierarchyName = oldDescriptor.hierarchyName;
		this.appgroup = APPGROUP.equals(oldDescriptor.hierarchyName);
		this.leader = oldDescriptor.leader;
		this.parentLeader = oldDescriptor.parentLeader;
		this.followers = new ArrayList<Node>(oldDescriptor.followers);
	}
	
	/**
	 * @return the uniqueId
	 */
	public UUID getUniqueId() {
		return uniqueId;
	}
	/**
	 * @return the friendlyName
	 */
	public String getName() {
		return hierarchyName;
	}
	/**
	 * @param friendlyName the friendlyName to set
	 */
	public void setFriendlyName(String friendlyName) {
		this.hierarchyName = friendlyName;
	}
	/**
	 * @return the leader
	 */
	public Node getLeader() {
		return leader;
	}
	/**
	 * @param leader the leader to set
	 */
	public void setLeader(Node leader) {
		if (followers.contains(leader)) {
			followers.remove(leader);
		}
		this.leader = leader;
	}
	
	
	public boolean isLeader(Node currentDescriptor) {
		if (leader == null) {
			return false;
		}
		return leader.equals(currentDescriptor);
	}
	
	public boolean isParentLeader(Node currentDescriptor) {
		if (parentLeader == null) {
			return false;
		}
		return parentLeader.equals(currentDescriptor);
	}
	
	public boolean isMember(Node currentDescriptor) {
		return isLeader(currentDescriptor) || isFollower(currentDescriptor);
	}
	
	public boolean isFollower(Node currentDescriptor) {
		for (Node node: followers) {
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
		return appgroup;
	}
	
	/**
	 * @return the followers
	 */
	public List<Node> getFollowers() {
		return new ArrayList<Node>(followers);
	}
	
	/**
	 * @return the parentGroup
	 */
	public Node getParentLeader() {
		return parentLeader;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GroupDescriptor) {
			GroupDescriptor remoteGroup = (GroupDescriptor)obj;
			return getUniqueId().equals(remoteGroup.getUniqueId());
		} else {
			return super.equals(obj);
		}
	}
	
	public ArrayList<Node> getMembers() {
		ArrayList<Node> members = new ArrayList<Node>(followers);
		members.add(leader);
		return members;
		
	}
	
	public static GroupDescriptor createUniverse(Node node) {
		return new GroupDescriptor(UUID.randomUUID(), APPGROUP, node, null);
	}
	
	public boolean isSameHierarchy(GroupDescriptor remoteGroupDescriptor) {
		if (remoteGroupDescriptor == null) {
			throw new RuntimeException("Remote descriptor cannot be null.");
		}
		return hierarchyName.equals(remoteGroupDescriptor.hierarchyName);
	}
	
	/**
	 * @param parentLeader the parentLeader to set
	 */
	public void setParentLeader(Node parentLeader) {
		if (followers.contains(parentLeader)) {
			throw new RuntimeException("Attempting to set a follower as a parent leader.");
		}
		this.parentLeader = parentLeader;
	}
	
	/**
	 * @param e
	 * @return
	 * @see java.util.HashSet#add(java.lang.Object)
	 */
	public boolean addFollower(Node descriptor) {
		if (leader != null && leader.equals(descriptor)) {
			throw new RuntimeException("Cannot add leader as follower: " + descriptor);
		}
		if (followers.contains(descriptor)) {
			System.err.println("WARNING:Attempting to add the same follower twice. Node " + 
					descriptor.getID() );
			return false;
		}
		return followers.add(descriptor);
	}
	
	/**
	 * @param o
	 * @return
	 * @see java.util.HashSet#remove(java.lang.Object)
	 */
	public boolean removeFollower(Node descriptor) {
		return followers.remove(descriptor);
	}

	public boolean hierarchyNameExist(String name) {
		if(this.hierarchyName.equals(name)){
		return true;}
		return false;
	}
	

	
	
}