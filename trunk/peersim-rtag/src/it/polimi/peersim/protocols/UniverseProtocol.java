package it.polimi.peersim.protocols;

import java.util.ArrayList;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.core.Protocol;
import it.polimi.peersim.prtag.GroupCommunicationDispatcher;
import it.polimi.peersim.prtag.GroupDescriptor;

public class UniverseProtocol implements Protocol{ 
		
	private ArrayList<GroupDescriptor> discoveredGroupName =
			new ArrayList<GroupDescriptor>();
	
	private GroupDescriptor leadedUniverse;
	private GroupDescriptor followedUniverse;
	
	private Node currentNode;
	private GroupCommunicationDispatcher groupCommunicationDispatcher;

	
	private ArrayList<Node> followers = new ArrayList<Node>();

	private static final String UNIVERSE_PROTOCOL = "universe_protocol";
	private static int universeProtocolId;
	
	private static Node parentUniverse = null;
	
	//private static final String DISCOVERY_PROTOCOL = "discovery_protocol";
	//private static int discoveryProtocolId;

	public UniverseProtocol(String prefix) {
		universeProtocolId = Configuration.getPid(
				prefix + "." + UNIVERSE_PROTOCOL);
		//discoveryProtocolId = Configuration.getPid(
		//		prefix + "." + DISCOVERY_PROTOCOL);
		
	}

	@Override
	public Object clone() {
		UniverseProtocol inp = null;
        try {
        	inp = (UniverseProtocol) super.clone();
        	inp.discoveredGroupName = (ArrayList<GroupDescriptor>) 
        			this.discoveredGroupName.clone();
  
        } catch (CloneNotSupportedException e) {
        } // never happens
        return inp;
	}
	

	/**
	 * Sets the current node and create a new universe for it.
	 * Also set the current instance as a change listener for the
	 * discovery protocol.
	 * 
	 * @param currentNode the current node.
	 */
	public void initialize(Node currentNode){//, DiscoveryProtocol discoveryProtocol) {
		this.currentNode = currentNode;
		leadedUniverse = GroupDescriptor.createUniverse(this.currentNode);
	}


	
	/**
	 * Invoked every time a new neighbor is added.
	 * 
	 * This method simulates the coordination that
	 * happens when a node is discovered.
	 * 
	 * The lower Id will ask the other node to ask its 
	 * top parent leader to join him as follower!
	 * 
	 *  o   /O 
	 *  o  / o
	 *  o /  o
	 *  O/-->o
	 */
	public void handleNeighbourDiscovered(Node addedNode){	
		Node n = currentNode;
		UniverseProtocol protocol = null;
		if (currentNode.getID() < addedNode.getID()) {
			return;
		}
		
		//loop to reach the top leader.
		while (true) {
			if (n == null) {
				throw new AssertionError("Leader cannot be null");
			} 
			//for each node we found we get its protocol
			protocol = (UniverseProtocol) n.getProtocol(universeProtocolId);
			//we ask the other node protocol if the followed universe is null
			if (protocol.followedUniverse == null) {
				//so we find the top leader
				break;
			}
			//otherwise we go to its leader, and repeat the loop
			n = protocol.followedUniverse.getLeader();
			if (n == null) {
				throw new AssertionError("Leader cannot be null: " + protocol.followedUniverse);
			} 
			/*System.out.println("*?*Node " + currentNode.getID() + 
					" added to " + addedNode);*/
		}
		//in variable protocol we have an instance
		//of the top leader protocol
		//we tell the top leader to start following the current node
		protocol.startFollowing(addedNode);
	}
	
	/**
	 * Simulates that the current node will start 
	 * following the remote node.
	 * 
	 * If the current leaded group has no followers it will be dismantled.
	 * 
	 * If the remote node has no leaded group it will be created.
	 * 
	 * @param addedNode
	 */
	private void startFollowing(Node remoteNode) {
		
		//here the current node want to
		//follow the remote node!
		
		UniverseProtocol remoteProtocol = (UniverseProtocol) 
				remoteNode.getProtocol(universeProtocolId);
	
		
		/*if(remoteProtocol.followedUniverse != null) {
			throw new AssertionError("already following a universe");    
			
		}*/
		
		
		if(!leadedUniverse.isParentLeader(currentNode)){
		
		remoteProtocol.leadedUniverse.addFollower(currentNode);
		//System.out.println("?-------------we find top parent*");
		return;
		}
		// TODO update the other followers!!!!!
		followedUniverse = new GroupDescriptor(remoteProtocol.leadedUniverse);
		
	    // Ask the remote node protocol for its leaded universe
		// it means which universe it is leading.

		//TODO clone the universe
		//check the leadeduniverse
		
		//if it is null return, otherwise
		//if it is empty delete it
		//if it has followers update them
		
		//leadeduniverse was initiated 
		
		
		if(leadedUniverse == null) {
			//nothing to do
			return;
		}
		
		if(leadedUniverse.getFollowers().size() == 0) {
			//TODO inform the tuple space the group has been removed
			leadedUniverse = null;
			return;
		} else {//it has followers
			for(Node follower: leadedUniverse.getFollowers()){
				GroupDescriptor followedUniverse = remoteProtocol.acceptFollower(follower);
			//we need to update the group descriptor of all followers
			//instead of sending messages we call the protocol to do that
				//UniverseProtocol protocol = null;
				//protocol = (UniverseProtocol) follower.getProtocol(universeProtocolId);
				//followedUniverse = GroupDescriptor.createUniverse(follower);
				
				// set that universe as the followed universe.
				remoteProtocol.followedUniverse.setParentLeader(remoteNode);
	
			}
			return;
		}
		
	}

	// TODO RETURN A GROUP DESCRIPTOR!!!!!!!!
	private GroupDescriptor acceptFollower(Node remoteNode) {
		//the remote node is the top leader that wants to join the current node
		//so first check the current node is a leader or follower
		//if leader add it to the follower (first check if it is not already added)
		//otherwise ask your leader to do that.
		UniverseProtocol followerProtocol = (UniverseProtocol) 
				remoteNode.getProtocol(universeProtocolId);
		if(leadedUniverse.isLeader(currentNode)){
			if(!followerProtocol.followedUniverse.isFollower(remoteNode)){
				leadedUniverse.addFollower(remoteNode);
				return null;
			}
		}
		//if it is the follower
		Node leader = leadedUniverse.getLeader();
		//ask leader add to the group
		UniverseProtocol leaderProtocol = (UniverseProtocol) 
				leader.getProtocol(universeProtocolId);
		leaderProtocol.acceptFollower(remoteNode);
		return null;
	}
	
	/**
	 * Invoked every time one of the neighbor disappear.
	 * 
	 * If the lost neighbor is the leader a new leader should be elected.
	 * 
	 * If the neighbor was a follower the leader will handle it. 
	 * If this node is the leader then it has to notify the other followers. 
	 */
	public void handleNeighbourLost(Node lostNode) {
	    //ask the lostnode if it is a leader? 
		UniverseProtocol protocol = null;
		protocol = (UniverseProtocol) lostNode.getProtocol(universeProtocolId);
		
		if(protocol.followedUniverse.isLeader(lostNode))
		{
			//first check it has any follower or its group become empty!
			if(leadedUniverse.getFollowers().size() == 0)
			{//TODO inform the tuple space the group has been removed
				leadedUniverse = null;
				return;
			}
			//elect a new leader, replace the lost leader!
			protocol.electNewLeader(lostNode);
		}
		else{
			//inform all the members of the lost
			for(Node follower: leadedUniverse.getMembers()){
				UniverseProtocol followerProtocol = (UniverseProtocol) 
						follower.getProtocol(universeProtocolId);
				//we need to update the group descriptor of all followers
				//instead of sending messages we call the protocol to do that
				//TODO how are the followers are informed?
				return;
				}
		}
	}
		
	
	private void electNewLeader(Node lostNode) {
		long minId=0;
		Node nextLeader = null;
		
		for(Node follower: leadedUniverse.getFollowers()){
			UniverseProtocol followerProtocol = (UniverseProtocol) 
					follower.getProtocol(universeProtocolId);
			//select follower with smallest id as the leader
			if(follower.getID()>minId){
		        minId=follower.getID();
		        nextLeader = follower;
			}
			return;
		}
		
		for(Node follower: leadedUniverse.getFollowers()){
			UniverseProtocol followerProtocol = (UniverseProtocol) 
					follower.getProtocol(universeProtocolId);
			//we need to update the group descriptor of all followers
			//instead of sending messages we call the protocol to do that
			followerProtocol.followedUniverse.setLeader(nextLeader);
			return;
			}

	}

	public void joinGroup(String friendlyName) {
		// if string is equal to the constant universe, rise an exception
		if (GroupDescriptor.UNIVERSE.equals(friendlyName)) {
			throw new RuntimeException("The application cannot join the universe.");
		}
		// otherwise tell dispatcher to create and join a group of what we need
		groupCommunicationDispatcher.joinGroupAndNotifyNetwork(friendlyName);
	}

	public void leaveGroup(String friendlyName) {
		if (GroupDescriptor.UNIVERSE.equals(friendlyName)) {
			throw new RuntimeException("The application cannot leave the universe.");
		}
		groupCommunicationDispatcher.leaveGroupsWithName(friendlyName);
		
	}

	
	public ArrayList<Node> getFollowers(){
	
		if(leadedUniverse.isLeader(currentNode)){
			followers = (ArrayList<Node>) leadedUniverse.getFollowers();
			//System.out.println("?-------------pan**pan*");
		}
		return followers;
	}
	
	

	public void notifyAddedNode(ArrayList<Node> added) {
	
		
		if(parentUniverse==null){
			//ToDO this works for one universe(one group), fix it
			//for multi groups!
			parentUniverse = this.currentNode;
			leadedUniverse.setParentLeader(currentNode);
			System.out.println("created a top parent " + currentNode.getID()  );
			//return;
		}
	
		for (Node node: added) {
			handleNeighbourDiscovered(node);
			//System.out.println("nofify added Node " + currentNode.getID()  );
		}
		
	}

	public void notifyRemovedNode(ArrayList<Node> removed) {
		for (Node n: removed) {
			handleNeighbourLost(n);
			System.out.println("notify removed Node " + currentNode.getID()  );
		}
		
	}

	public GroupDescriptor getLeadedUniverse() {
		return leadedUniverse;
	}

	public GroupDescriptor getFollowedUniverse() {
		return followedUniverse;
	}
	

	
	
	/*
	public boolean shouldRequestToJoin() {
		return false;
		// TODO Auto-generated method stub
		
	}

	
	public boolean shouldAcceptJoinRequest() {
		return false;
		// TODO Auto-generated method stub
		
	}

	public boolean shouldSplitTo() {
		return false;
		// TODO Auto-generated method stub
		
	}

	public boolean shouldAcceptToCreateAChild() {
		return false;
		// TODO Auto-generated method stub
		
	}

	public void followerToSplit() {
		// TODO Auto-generated method stub
		
	}

	

	 */
}
