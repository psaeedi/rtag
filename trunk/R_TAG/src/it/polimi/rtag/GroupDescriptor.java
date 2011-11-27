/**
 * 
 */
package it.polimi.rtag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
	public static final String UNIVERSE = "__UNIVERSE";
	
	private UUID uniqueId;
	
	private String hierarchyName;
	
	//private Tuple description;
	
	private boolean universe = false;
	
	/**
	 * The top leader of this group hierarchy. This is need to 
	 * avoid creating loops. 
	 */
	private NodeDescriptor progenitor;
	
	/**
	 * The group leader.
	 */
	private NodeDescriptor leader;
	
	/**
	 * All the followers. 
	 */
	private ArrayList<NodeDescriptor> followers = new ArrayList<NodeDescriptor>();
	
	/**
	 * The parent group or null if the group is top level. 
	 */
	private NodeDescriptor parentLeader;

	/**
	 * @param uniqueId
	 * @param friendlyName
	 * @param leader
	 */
	public GroupDescriptor(UUID uniqueId, String friendlyName,
			NodeDescriptor leader) {
		this.uniqueId = uniqueId;
		this.hierarchyName = friendlyName;
		this.leader = leader;
	}
	
	/**
	 * @param uniqueId
	 * @param friendlyName
	 * @param leader
	 * @param parentGroup
	 */
	public GroupDescriptor(UUID uniqueId, String friendlyName,
			NodeDescriptor leader, NodeDescriptor parentLeader) {
		this.uniqueId = uniqueId;
		this.hierarchyName = friendlyName;
		this.leader = leader;
		this.parentLeader = parentLeader;
	}
	
	/**
	 * @param uuid
	 * @param friendlyName
	 * @param description
	 * @param universe
	 * @param leader
	 * @param parentLeader
	 */
	private GroupDescriptor(UUID uuid, String friendlyName, 
			boolean universe, NodeDescriptor leader,
			NodeDescriptor parentLeader) {
		super();
		this.uniqueId = uuid;
		this.hierarchyName = friendlyName;
		this.universe = universe;
		this.leader = leader;
		this.parentLeader = parentLeader;
	}
	
	public GroupDescriptor(GroupDescriptor oldDescriptor) {
		super();
		this.uniqueId = oldDescriptor.uniqueId;
		this.hierarchyName = oldDescriptor.hierarchyName;
		this.universe = oldDescriptor.universe;
		this.leader = oldDescriptor.leader;
		this.parentLeader = oldDescriptor.parentLeader;
		this.followers = new ArrayList<NodeDescriptor>(oldDescriptor.followers);
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
	public NodeDescriptor getLeader() {
		return leader;
	}
	/**
	 * @param leader the leader to set
	 */
	public void setLeader(NodeDescriptor leader) {
		if (followers.contains(leader)) {
			followers.remove(leader);
		}
		this.leader = leader;
	}


	public boolean isLeader(NodeDescriptor currentDescriptor) {
		if (leader == null) {
			return false;
		}
		return leader.equals(currentDescriptor);
	}
	
	public boolean isParentLeader(NodeDescriptor currentDescriptor) {
		if (parentLeader == null) {
			return false;
		}
		return parentLeader.equals(currentDescriptor);
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
	public List<NodeDescriptor> getFollowers() {
		return new ArrayList<NodeDescriptor>(followers);
	}

	/**
	 * @return the parentGroup
	 */
	public NodeDescriptor getParentLeader() {
		return parentLeader;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GroupDescriptor) {
			GroupDescriptor remoteGroup = (GroupDescriptor)obj;
			if (!uniqueId.equals(remoteGroup.uniqueId)) {
				return false;
			} 
			
			if ((leader != null && !leader.equals(remoteGroup.leader)) ||
					(remoteGroup.leader != null && !remoteGroup.leader.equals(leader))) {
				return false;
			}
			
			if ((parentLeader != null && !parentLeader.equals(remoteGroup.parentLeader)) ||
					remoteGroup.parentLeader != null && !remoteGroup.parentLeader.equals(parentLeader)) {
				return false;
			}
			
			if (followers.size() != remoteGroup.followers.size()) {
				return false;
			}
			
			for (int i = 0; i < followers.size(); i++) {
				NodeDescriptor l = followers.get(i);
				NodeDescriptor r = remoteGroup.followers.get(i);
				if (!l.equals(r)) {
					return false;
				}
			}
			
			return true;
		} else {
			return super.equals(obj);
		}
	}

	public ArrayList<NodeDescriptor> getMembers() {
		ArrayList<NodeDescriptor> members = new ArrayList<NodeDescriptor>(followers);
		members.add(leader);
		return members;
		
	}

	public static GroupDescriptor createUniverse(Node node) {
		return new GroupDescriptor(UUID.randomUUID(), UNIVERSE, true, node.getNodeDescriptor(), null);
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
	public void setParentLeader(NodeDescriptor parentLeader) {
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
	public boolean addFollower(NodeDescriptor descriptor) {
		if (leader.equals(descriptor)) {
			throw new RuntimeException("Cannot add leader as follower: " + descriptor);
		}
		if (followers.contains(descriptor)) {
			throw new RuntimeException("Attempting to add the same follower twice. Node " + 
					descriptor + " group: " + this);
		}
		return followers.add(descriptor);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.HashSet#remove(java.lang.Object)
	 */
	public boolean removeFollower(NodeDescriptor descriptor) {
		return followers.remove(descriptor);
	}
	
	public GroupVisitor acceptVisitor(GroupVisitor visitor) {
		visitor.visit(this);
		return visitor;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return acceptVisitor(new GroupToStringVisitor()).toString();
	}

	/**
	 * @return the progenitor
	 */
	public NodeDescriptor getProgenitor() {
		return progenitor;
	}

	/**
	 * @param progenitor the progenitor to set
	 */
	public void setProgenitor(NodeDescriptor progenitor) {
		this.progenitor = progenitor;
	}
}
