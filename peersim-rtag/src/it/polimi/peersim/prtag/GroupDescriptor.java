package it.polimi.peersim.prtag;

import java.util.ArrayList;
import java.util.List;

import peersim.core.Node;

public class GroupDescriptor {
	
	public static final String UNIVERSE = "__UNIVERSE";
	
    private long  uniqueId;
	
	private String hierarchyName;
	
	//private Tuple description;
	
	private boolean universe = false;

	/**
	 * The group leader.
	 */
	private Node leader;
	
	/**
	 * All the followers. 
	 */
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
	

	//long ID= n.getID();
	
	//constructor by copy
   public GroupDescriptor(GroupDescriptor oldDescriptor){
	   
	    this.uniqueId = oldDescriptor.uniqueId;
		this.hierarchyName = oldDescriptor.hierarchyName;
		this.leader = oldDescriptor.leader;
		this.universe = oldDescriptor.universe;
		this.parentLeader = oldDescriptor.parentLeader;
		this.followers = new ArrayList<Node>(oldDescriptor.followers);
		if(isFollower(this.parentLeader)){
			throw new RuntimeException("1Attempting to create universe with a follower as a parent leader.");
		}
   }

	public GroupDescriptor(long id, String friendlyName, Node leader, Node parentLeader) {
		super();
		this.uniqueId = id;
		this.hierarchyName = friendlyName;
		this.leader = leader;
		this.parentLeader = parentLeader;
		this.universe = UNIVERSE.equals(friendlyName);
		if(isFollower(this.parentLeader)){
			throw new RuntimeException("2Attempting to create universe with a follower as a parent leader.");
		}
	
	}

	public long getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(long uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	/**
	 * @return the friendlyName
	 */
	public String getFriendlyName() {
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
	
	public boolean isLeader(Node currentNode) {
		if (leader == null) {
			return false;
		}
		return leader.equals(currentNode);
	}
	
	public boolean isParentLeader(Node currentNode) {
		if (parentLeader == null) {
			return false;
		}
		return parentLeader.equals(currentNode);
	}

	public boolean isMember(Node currentNode) {
		return isLeader(currentNode) || isFollower(currentNode);
	}
	
	public boolean isFollower(Node currentNode) {
		if(followers.isEmpty()){
			//System.out.println("follower list is empty-GroupDescriptor");
		}
		for (Node node: followers) {
			if (node.equals(currentNode)) {
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
	public ArrayList<Node> getMembers() {
		ArrayList<Node> members = new ArrayList<Node>(followers);
		members.add(leader);
		return members;
		
	}

	/*public static GroupDescriptor createUniverse(Node node) {
		return new GroupDescriptor(node.getID(), UNIVERSE, node, node);
		
	}*/
	
	public static GroupDescriptor createUniverse(Node node, Node parentLeader) {
		return new GroupDescriptor(node.getID(), UNIVERSE, node, parentLeader);
		
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
	public boolean addFollower(Node remotenode) {
		if (leader != null && leader.equals(remotenode)) {
			throw new RuntimeException("Cannot add leader as follower: " + remotenode);
		}
		
		if (followers.contains(remotenode)) {
			throw new RuntimeException("Attempting to add the same follower twice. Node " + 
					remotenode + " group: " + this);
		}
		
		System.out.println("************************IMP:node is added as follower-groupdescriptor:" + remotenode.getID() +
				", size followers:" + followers.size());   
		return followers.add(remotenode);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.HashSet#remove(java.lang.Object)
	 */
	public boolean removeFollower(Node node) {
		System.out.println("----------------IMP:node is removed as follower-groupdescriptor:" + node.getID() +
				", size followers:" + followers.size());   
		return followers.remove(node);
	}
	
	
	
}
