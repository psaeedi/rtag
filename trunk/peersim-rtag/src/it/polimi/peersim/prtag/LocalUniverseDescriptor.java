package it.polimi.peersim.prtag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import peersim.core.Node;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 */

  public class LocalUniverseDescriptor implements Serializable{

	/**
	 * The group leader.
	 * Each node is leader of at most of its own universe and is 
	 * following multiple other universes (Can have multiple leaders and multiple followers).
	 */
  private Node leader;
	
	/**
	 * All the followers. 
	 * We do not keep trace of the leader's leaders.
	 */
   private ArrayList<Node> followers = new ArrayList<Node>();
	
	
	/**
	 * All the application groups,
	 * which the node is joined in 
	 */
   private ArrayList<GroupDescriptor> groupDescriptor = new ArrayList<GroupDescriptor>();

	
	//constructor by copy
   public LocalUniverseDescriptor(LocalUniverseDescriptor oldDescriptor){
		this.leader = oldDescriptor.leader;
		this.followers = new ArrayList<Node>(oldDescriptor.followers);
   }

	public LocalUniverseDescriptor(Node leader) {
		super();
		this.leader = leader;
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
	 * @return the followers
	 */
	public List<Node> getFollowers() {
		return new ArrayList<Node>(followers);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public ArrayList<Node> getMembers() {
		ArrayList<Node> members = new ArrayList<Node>(followers);
		members.add(leader);
		return members;
		
	}

	/**
	 * @param e
	 * @return
	 */
	public void addFollower(Node remoteNode) {
		if (leader != null && leader.equals(remoteNode)) {
			throw new RuntimeException("Cannot add leader as follower: " + remoteNode.getID());
		}
		
		if (followers.contains(remoteNode)) {
			throw new RuntimeException("Attempting to add the same follower twice. Node " + 
					remoteNode.getID() + " group: " + this);
		}
		followers.add(remoteNode);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.HashSet#remove(java.lang.Object)
	 */
	public boolean removeFollower(Node node) {  
		return followers.remove(node);
	}
	
}
