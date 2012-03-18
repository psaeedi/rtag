package it.polimi.peersim.protocols;

import java.util.ArrayList;
import java.util.HashMap;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.core.Protocol;
import it.polimi.peersim.prtag.LocalUniverseDescriptor;

public class UniverseProtocol implements Protocol{ 

	private static final String UNIVERSE_PROTOCOL = "universe_protocol";
	private static int universeProtocolId;
	
	private static final String FOLLOWER_THRESHOLD = "follower_threshold";
	private final int followerThreshold;
	
	private Node localNode;
	
	// All the leaders of this Node
	public ArrayList<Node> leaders = new ArrayList<Node>();
	
	// All the followers of this node
	public ArrayList<Node> followers = new ArrayList<Node>();
	
	// Additional information about the followers
	// This is used to give leaders informations about its followers' followers
	private HashMap<Node, LocalUniverseDescriptor> followerUniverseDescriptors = 
			new HashMap<Node, LocalUniverseDescriptor>();
	
	// The local universe of the current node
	private LocalUniverseDescriptor localUniverse;
	

	public UniverseProtocol(String prefix) {
		universeProtocolId = Configuration.getPid(
				prefix + "." + UNIVERSE_PROTOCOL);
		followerThreshold = Configuration.getInt(
				prefix + "." + FOLLOWER_THRESHOLD, 2);
		
	}

	@Override
	public Object clone() {
		UniverseProtocol inp = null;
        try {
        	inp = (UniverseProtocol) super.clone();
        	inp.localUniverse = this.getLocaluniverse();
        	inp.leaders = new ArrayList<Node>(this.leaders);
        	inp.followers = new ArrayList<Node>(this.followers);
        	inp.followerUniverseDescriptors = new HashMap<Node, LocalUniverseDescriptor>(this.followerUniverseDescriptors);
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
		localNode = currentNode;
		localUniverse = new LocalUniverseDescriptor(localNode);
	}

	
	/**
	 * Tells if the localNode should become a leader of another given 
	 * node or the other way around.
	 * 
	 * @param remoteNode the given remote node
	 * @return <code>true</code> if the localNode should become a leader of 
	 * 		the give one, <code>false</code> if the other way around.
	 */
	public boolean shouldLead(Node remoteNode) {
		return localNode.getID() < remoteNode.getID();
	}
	
	public void handleNeighbourDiscovered(Node remoteNode) {
		if (leaders.contains(remoteNode)) {
			// TODO show a debug error message. this should not be happening
			return;
		}
		
		if (followers.contains(remoteNode)) {
			// TODO show a debug error message. this should not be happening
			return;
		}
		
		if (shouldLead(remoteNode)) {
			// Add the remote node to the list of followers
			followers.add(remoteNode);
			// Update the local univers
			localUniverse.addFollower(remoteNode);
			
			// Get grandchild information
			UniverseProtocol followerProtocol = (UniverseProtocol) 
					remoteNode.getProtocol(universeProtocolId);
			LocalUniverseDescriptor followerUniverseDescriptor = followerProtocol.localUniverse;
			// save a copy to the remote node
			followerUniverseDescriptors.put(remoteNode,
					new LocalUniverseDescriptor(followerUniverseDescriptor));
			// Add the current node as a leader of the remote
			followerProtocol.leaders.add(localNode);
			
			// Notify all the leaders of this node with the new descriptor
			for (Node topLeader: leaders) {
				UniverseProtocol topLeaderProtocol = (UniverseProtocol) 
						topLeader.getProtocol(universeProtocolId);
				// save a copy to the remote node
				topLeaderProtocol.followerUniverseDescriptors.put(localNode,
						getLocaluniverse());
			}
			return;
		} else {
			//nothing to be done
			// The other node will do
			return;
		}
		
	}
	
	public void handleNeighbourLost(Node remoteNode) {
	    if (leaders.contains(remoteNode)) {
	    	// The lost node was one of the local universe leaders
	    	leaders.remove(remoteNode);
	    	return;
	    }
	    
	    if (followers.contains(remoteNode)) {
	    	followers.remove(remoteNode);
	    	followerUniverseDescriptors.remove(remoteNode);
	    	
	    	localUniverse.removeFollower(remoteNode);
	    	
	    	// Update the group descriptor of the leaders
	    	for (Node topLeader: leaders) {
				UniverseProtocol topLeaderProtocol = (UniverseProtocol) 
						topLeader.getProtocol(universeProtocolId);
				// save a copy to the remote node
				topLeaderProtocol.followerUniverseDescriptors.put(localNode,
						getLocaluniverse());
			}
	    }
	}

	public void notifyAddedNodes(ArrayList<Node> added) {
		
		
		for (Node addedNode: added) {
			if (addedNode.getID() == localNode.getID()){
				throw new RuntimeException("WARNING:node has found itself");
			}
			//for every found neighbor it should manage its neighbor
			handleNeighbourDiscovered(addedNode);
		}

	}


	public void notifyRemovedNodes(ArrayList<Node> removed) {
		for (Node lostNode: removed) {
			if (lostNode.getID() == localNode.getID()){
				throw new RuntimeException("WARNING:node has lost itself itself.");
			}
			handleNeighbourLost(lostNode);
		}
		
	}

	public LocalUniverseDescriptor getLocaluniverse() {
		if (this.localUniverse == null) {
			return null;
		}
		return new LocalUniverseDescriptor(localUniverse);
	}

	
	public void broadCast(String message) {
		System.out.println("[Node " + localNode.getID() + "] " + message);
		// TODO add tuple space to avoid duplication
		
		// We send to all the leaders
		for (Node topLeader: leaders) {
			UniverseProtocol topLeaderProtocol = (UniverseProtocol) 
					topLeader.getProtocol(universeProtocolId);
			topLeaderProtocol.broadCast(message);
		}
		
		// Send to all the followers which are not grandchildrens
		ArrayList<Node> children = new ArrayList<Node>(followers);
		for (Node follower: followers) {
			children.removeAll(followerUniverseDescriptors.get(follower).getFollowers());
		}
		for (Node child: leaders) {
			UniverseProtocol childProtocol = (UniverseProtocol) 
					child.getProtocol(universeProtocolId);
			childProtocol.broadCast(message);
		}
	}
	
	private void followerToLeader(Node remotenode) {
		System.out.println("[Node " + localNode.getID() + 
				"] Swapping follower " + remotenode.getID());
		if (!followers.contains(remotenode)) {
			throw new AssertionError("Node " + remotenode.getID() + " was not a follower");
		}
		
		followers.remove(remotenode);
		followerUniverseDescriptors.remove(remotenode);
		localUniverse.removeFollower(remotenode);
		leaders.add(remotenode);
		
		UniverseProtocol leaderProtocol = (UniverseProtocol) 
				remotenode.getProtocol(universeProtocolId);
		leaderProtocol.leaders.remove(localNode);
		leaderProtocol.followers.add(localNode);
		leaderProtocol.followerUniverseDescriptors.put(localNode, getLocaluniverse());
	}
	
	
	private Node getLessCongestedFollower() {
		int followersCount = Integer.MAX_VALUE;
		Node lessCongested = null;
		for (Node follower: followers) {
			LocalUniverseDescriptor descriptor = followerUniverseDescriptors.get(follower);
			int count = descriptor.getFollowers().size();
			if (count < followersCount) {
				followersCount = count;
				lessCongested = follower;
			}
		}
		return lessCongested;
	}
	
	private Node getRandomFollower() {
		return this.followers.get((int)Math.floor(Math.random() * this.followers.size()));
	}

	public boolean isCongested() {
		return followers.size() / followerThreshold > leaders.size(); 
	}

	public void handleCongestion() {
		System.out.println("Starting congestion control loop.");
		while (isCongested()) {
			Node follower = getLessCongestedFollower();
			if (follower == null) {
				return;
			}
			followerToLeader(follower);
		}
	}
	
	
}
