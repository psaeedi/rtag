package it.polimi.peersim.protocols;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;
import it.polimi.peersim.prtag.BroadcastContent;
import it.polimi.peersim.prtag.LocalUniverseDescriptor;
import it.polimi.peersim.prtag.UniverseMessage;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 */

// TODO implement Transport
public class UniverseProtocol implements Transport, EDProtocol { 

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
	
	public void notifyAddedNodes(ArrayList<Node> added) {
		for (Node addedNode: added) {
			if (addedNode.getID() == localNode.getID()){
				throw new RuntimeException("WARNING:node has found itself");
			}
			//for every found neighbor it should manage its neighbor
			handleNeighbourDiscovered(addedNode);
		}
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
			/*
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
			}*/
			// Nothing to be done
			return;
		} else {
			UniverseMessage addfollowermessage = UniverseMessage.createAddfollower(getLocaluniverse());
			send(localNode, remoteNode, addfollowermessage, universeProtocolId);
		}
		
	}
	
	public void notifyRemovedNodes(ArrayList<Node> removed) {
		for (Node lostNode: removed) {
			if (lostNode.getID() == localNode.getID()){
				throw new RuntimeException("WARNING: node has lost itself itself.");
			}
			handleNeighbourLost(lostNode);
		}
		
	}
	
	public void handleNeighbourLost(Node remoteNode) {
	    if (leaders.contains(remoteNode)) {
	    	// The lost node was one of the local universe leaders
	    	leaders.remove(remoteNode);
	    	// No message needs to be sent
	    	return;
	    }
	    
	    if (followers.contains(remoteNode)) {
	    	// The lost node is a follower.
	    	// we need to update the universe descriptor and notify both
	    	// followers and leaders with the new descriptor.
	    	
	    	// Remove the lost node from the list
	    	followers.remove(remoteNode);
	    	// Remove the lost node universe descriptor
	    	followerUniverseDescriptors.remove(remoteNode);
	    	// Update the local universe descriptor	    	
	    	localUniverse.removeFollower(remoteNode);
	    	
	    	// Notify the leaders that this local universe has changed.
	    	for (Node topLeader: leaders) {
	    		UniverseMessage updateMsg = UniverseMessage.createUpdateDescriptor(getLocaluniverse());
	    		send(localNode, topLeader, updateMsg, universeProtocolId);
			}
	    }
	}


	public LocalUniverseDescriptor getLocaluniverse() {
		if (this.localUniverse == null) {
			return null;
		}
		return new LocalUniverseDescriptor(localUniverse);
	}

	
	public void broadCast(BroadcastContent message) {
		System.out.println("[Node " + localNode.getID() + "] " + message);
		// forward the broadcast to the top protocols
		EDProtocol receiver = (EDProtocol)localNode.getProtocol(message.getPid());
		receiver.processEvent(localNode, message.getPid(), message);
		
		// We send to all the leaders
		for (Node topLeader: leaders) {
			UniverseMessage broadcast = UniverseMessage.createBroadcast(message);
			send(localNode, topLeader, broadcast, universeProtocolId);
			//The above replaced below
			/*UniverseProtocol topLeaderProtocol = (UniverseProtocol) 
					topLeader.getProtocol(universeProtocolId);
			topLeaderProtocol.broadCast(message);*/
		}
		
		// Send to all the followers which are not grandchildrens
		ArrayList<Node> children = new ArrayList<Node>(followers);
		for (Node follower: followers) {
			children.removeAll(followerUniverseDescriptors.get(follower).getFollowers());
		}
		for (Node child: leaders) {
			UniverseMessage broadcast = UniverseMessage.createBroadcast(message);
			send(localNode, child, broadcast, universeProtocolId);
		}
	}
	
	private void followerToLeader(Node remotenode) {
		System.out.println("[Node " + localNode.getID() + 
				"] Swapping follower " + remotenode.getID());
		if (!followers.contains(remotenode)) {
			throw new AssertionError("Node " + remotenode.getID() + " was not a follower");
		}
		
		UniverseMessage addfollowermessage = UniverseMessage.createAddfollower(getLocaluniverse());
		send(localNode, remotenode, addfollowermessage, universeProtocolId);
	}
	
	/*
	private void leaderTofollower(Node remoteNode) {
		//remote node has other leaders and it is my follower
		//I want to become his follower
		System.out.println("@[Node " + localNode.getID() + 
				"] not a leader anymore, instead following its follower:" + remoteNode.getID());
		if (!followers.contains(remoteNode)) {
			throw new AssertionError("Node " + remoteNode.getID() + " was not a follower");
		}
		//if it is not a leader already add it
		if(!leaders.contains(remoteNode)){
			//now follow ur follower, so add it to the leader
			//list and remove it from the follower list
			followers.remove(remoteNode);
			leaders.add(remoteNode);
			//TODO check below
			followerUniverseDescriptors.remove(remoteNode);
		}
		
		UniverseProtocol leaderProtocol = (UniverseProtocol) 
				remoteNode.getProtocol(universeProtocolId);
		leaderProtocol.leaders.remove(localNode);
		leaderProtocol.followers.add(localNode);
		leaderProtocol.followerUniverseDescriptors.put(localNode, getLocaluniverse());
		localUniverse.removeFollower(remoteNode);
	}*/
	
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
	
	private Node getFollowerWithOtherLeaders() {
		for (Node follower: followers) {
			LocalUniverseDescriptor followerDescriptor = followerUniverseDescriptors.get(follower);
			int followersCount = followerDescriptor.getFollowers().size();
			//except me it has other leaders
			if(followersCount > 1){
			   return follower;
			}
		}
		return null;
	}
	
	private Node getRandomFollower() {
		return this.followers.get((int)Math.floor(Math.random() * this.followers.size()));
	}

	public boolean isCongested() {
		return followers.size() / followerThreshold > leaders.size(); 
	}
	
    public boolean hasNoLeader() {
    	//System.out.println("node "+ localNode.getID()+ " the leaders size is:" + leaders.size());
		return leaders.isEmpty();
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

	public void handleNodeNoLeader() {
		System.out.println("Starting no leader control loop.");
		while (hasNoLeader()) {
			Node follower = getFollowerWithOtherLeaders();
			if (follower == null) {
				System.out.println("##no follower found with other leaders." + localNode.getID());
				return;
			}
			followerToLeader(follower);
		}
	}

	@Override
	public long getLatency(Node src, Node dest) {
	
		return 1000;
	}

	/**
	 * Sends a message
	 */
	@Override
	public void send(Node src, Node dest, Object msg, int pid) {
		getLatency(src, dest);
		UniverseProtocol remoteProtocol = (UniverseProtocol) 
				dest.getProtocol(pid);
		remoteProtocol.processEvent(src, pid, msg);
	}

	/**
	 * Receives a message 
	 */
	@Override
	public void processEvent(Node node, int pid, Object event) {
		if (event instanceof UniverseMessage) {
			UniverseMessage message = (UniverseMessage) event;
			
			System.out.println(node.getID() + "->" + localNode.getID() + " message " + ((UniverseMessage) event).getHead());
			
			if (UniverseMessage.UPDATE_DESCRIPTOR.equals(message.getHead())) {
				handleLocalUniverseDescriptorChanged((LocalUniverseDescriptor)message.getBody());
				return;
			}
			
			if (UniverseMessage.BROADCAST.equals(message.getHead())) {
				broadCast((BroadcastContent)message.getBody());
				return;
			}
			
			if (UniverseMessage.ADDFOLOWER.equals(message.getHead())) {
				getLatency(localNode, node);
				addFollower(node, (LocalUniverseDescriptor)message.getBody());
				return;
			}
			
			if (UniverseMessage.ADDFOLOWER_ACK.equals(message.getHead())) {
				getLatency(localNode, node);
				addFollowerAck(node);
				return;
			}
			
		} else {
			throw new AssertionError(
					"The universe protocol should only receive UniverseMessages");
		}
	}

	private void addFollower(Node follower, LocalUniverseDescriptor remoteUniverse) {
		// TODO add follower
		
	    leaders.remove(follower);
		followers.add(follower);
		followerUniverseDescriptors.put(follower, remoteUniverse);
		localUniverse.addFollower(follower);
		
		UniverseMessage addfollowermessageAck = UniverseMessage.createAddfollowerAck();
		send(localNode, follower, addfollowermessageAck, universeProtocolId);
		// TODO What happens if this message is lost?
		
		// TODO update its leaders by sending them the updated descriptor
		for (Node topLeader: leaders) {
    		UniverseMessage updateMsg = UniverseMessage.createUpdateDescriptor(getLocaluniverse());
    		send(localNode, topLeader, updateMsg, universeProtocolId);
		}
	}
	
	/**
	 * @param leader
	 * 
	 */
	private void addFollowerAck(Node leader) {
		// If the new leader was previously a follower
		followers.remove(leader);
		followerUniverseDescriptors.remove(leader);
		localUniverse.removeFollower(leader); 
		
		// Add it as a new leader
		leaders.add(leader);
	}
	
	/**
	 * Handles an updated universe descriptor 
	 */
	private void handleLocalUniverseDescriptorChanged(
			LocalUniverseDescriptor descriptor) {
		Node universeLeader = descriptor.getLeader();
		// If it is a follower universe store it
		if (followers.contains(universeLeader)) {
			followerUniverseDescriptors.put(
					universeLeader, descriptor);
		}
	}

	
	
}
