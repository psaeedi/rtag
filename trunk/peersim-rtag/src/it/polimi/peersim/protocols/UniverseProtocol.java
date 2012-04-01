package it.polimi.peersim.protocols;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import it.polimi.peersim.messages.BaseMessage;
import it.polimi.peersim.messages.UniverseMessage;
import it.polimi.peersim.prtag.LocalUniverseDescriptor;

/**
 * @author Panteha Saeedi @ elet.polimi.it
 *
 * The protocol stack is:
 * 3 - UniverseProtocol
 * 2 - TupleSpaceProtocol
 * 1 - MockChannel
 * Demon - DiscoveryProtocol
 * Demon - GeoLocation
 */
public class UniverseProtocol extends ForwardingProtocol<UniverseMessage> implements CDProtocol { 

	private static final String FOLLOWER_THRESHOLD = "follower_threshold";
	private final int followerThreshold;
	private static final String FOLLOWER_THRESHOLD_RATE = "follower_thresholdrate";
	private final int followerLeaderRate;
	
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
		super(prefix);
		followerThreshold = Configuration.getInt(
				prefix + "." + FOLLOWER_THRESHOLD, 3);
		followerLeaderRate = Configuration.getInt(
				prefix + "." + FOLLOWER_THRESHOLD_RATE, 2);
	}
	
	@Override
	public Object clone() {
		UniverseProtocol clone = null;
		clone = (UniverseProtocol) super.clone();
		clone.leaders = new ArrayList<Node>(leaders);
		clone.followers = new ArrayList<Node>(followers);
		clone.localUniverse = localUniverse;
        return clone;
	}

	/**
	 * Sets the current node and create a new universe for it.
	 * Also set the current instance as a change listener for the
	 * discovery protocol.
	 * 
	 * @param currentNode the current node.
	 */
	public void initialize(Node currentNode){
		localUniverse = new LocalUniverseDescriptor(currentNode);
	}

	
	/**
	 * Tells if the localNode should become a leader of another given 
	 * node or the other way around.
	 * 
	 * @param remoteNode the given remote node
	 * @return <code>true</code> if the localNode should become a leader of 
	 * 		the give one, <code>false</code> if the other way around.
	 */
	public boolean shouldLead(Node currentNode, Node remoteNode) {
		return currentNode.getID() < remoteNode.getID();
	}
	
	public void notifyAddedNodes(Node currentNode, ArrayList<Node> added) {
		for (Node addedNode: added) {
			if (addedNode.getID() == currentNode.getID()){
				throw new RuntimeException("WARNING:node has found itself");
			}
			//for every found neighbor it should manage its neighbor
			handleNeighbourDiscovered(currentNode, addedNode);
		}
	}
	
	public void handleNeighbourDiscovered(Node currentNode, Node remoteNode) {
		if (leaders.contains(remoteNode)) {
			// TODO show a debug error message. this should not be happening
			return;
		}
		
		if (followers.contains(remoteNode)) {
			// TODO show a debug error message. this should not be happening
			return;
		}
		
		if (shouldLead(currentNode, remoteNode)) {
			// Nothing to be done
			return;
		} else {
			UniverseMessage addfollowermessage = 
					UniverseMessage.createAddfollower(
							protocolId, currentNode, getLocaluniverse());
			pushDownMessage(currentNode, remoteNode, addfollowermessage);
		}
		
	}
	
	public void notifyRemovedNodes(Node currentNode, ArrayList<Node> removed) {
		for (Node lostNode: removed) {
			if (lostNode.getID() == currentNode.getID()){
				throw new RuntimeException("WARNING: node has lost itself itself.");
			}
			handleNeighbourLost(currentNode, lostNode);
		}
		
	}
	
	public void handleNeighbourLost(Node currentNode, Node remoteNode) {
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
	    		UniverseMessage updateMsg =
	    				UniverseMessage.createUpdateDescriptor(
	    						protocolId, currentNode, getLocaluniverse());
	    		pushDownMessage(currentNode, topLeader, updateMsg);
			}
	    }
	}


	public LocalUniverseDescriptor getLocaluniverse() {
		if (this.localUniverse == null) {
			return null;
		}
		return new LocalUniverseDescriptor(localUniverse);
	}

	
	private BaseMessage handleBroadCast(Node currentNode, BaseMessage message) {
		sendBroadCast(currentNode, message);
		return message;
	}
	
	private void followerToLeader(Node currentNode, Node remoteNode) {
		System.out.println("[Node " + currentNode.getID() + 
				"] Swapping follower " + remoteNode.getID());
		if (!followers.contains(remoteNode)) {
			throw new AssertionError("Node " + remoteNode.getID() + 
					" was not a follower");
		}
		
		UniverseMessage addfollowermessage = 
				UniverseMessage.createAddfollower(
						protocolId,
						currentNode,
						getLocaluniverse());
		pushDownMessage(currentNode, remoteNode, addfollowermessage);
	}
	
	private Node getLessCongestedFollower() {
		int followersCount = Integer.MAX_VALUE;
		Node lessCongested = null;
		for (Node follower: followers) {
			LocalUniverseDescriptor descriptor = 
					followerUniverseDescriptors.get(follower);
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
			LocalUniverseDescriptor followerDescriptor = 
					followerUniverseDescriptors.get(follower);
			int followersCount = followerDescriptor.getFollowers().size();
			//except me it has other leaders
			if(followersCount > 1){
			   return follower;
			}
		}
		return null;
	}

	public boolean isCongested() {
		return (followers.size() > followerThreshold) && (followers.size() / followerLeaderRate > leaders.size()); 
	}
	
    public boolean hasNoLeader() {
    	//System.out.println("node "+ localNode.getID()+ " the leaders size is:" + leaders.size());
		return leaders.isEmpty();
	}

	public void handleCongestion(Node currentNode) {
		System.out.println("Starting congestion control loop.");
		while (isCongested()) {
			Node follower = getLessCongestedFollower();
			if (follower == null) {
				System.out.println("handleCongestion() no folllower found.");
				return;
			}
			followerToLeader(currentNode, follower);
		}
	}

	public void handleNodeNoLeader(Node currentNode) {
		System.out.println("Starting no leader control loop.");
		while (hasNoLeader()) {
			Node follower = getFollowerWithOtherLeaders();
			if (follower == null) {
				System.out.println(
						"handleNodeNoLeader() no follower found with other leaders." +
								currentNode.getID());
				return;
			}
			followerToLeader(currentNode, follower);
		}
	}

	private void addFollower(Node currentNode, Node follower,
			LocalUniverseDescriptor remoteUniverse) {
		// TODO add follower
		
	    leaders.remove(follower);
		followers.add(follower);
		followerUniverseDescriptors.put(follower, remoteUniverse);
		localUniverse.addFollower(follower);
		
		UniverseMessage addfollowermessageAck =
				UniverseMessage.createAddfollowerAck(protocolId, currentNode);
		pushDownMessage(currentNode, follower, addfollowermessageAck);
		// TODO What happens if this message is lost?
		
		// TODO update its leaders by sending them the updated descriptor
		for (Node topLeader: leaders) {
    		UniverseMessage updateMsg = UniverseMessage.createUpdateDescriptor(
    				protocolId, currentNode, getLocaluniverse());
    		pushDownMessage(currentNode, topLeader, updateMsg);
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

	@Override
	public void nextCycle(Node currentNode, int pid) {
        if (hasNoLeader()) {
        	handleNodeNoLeader(currentNode);
        } else if (isCongested()) {
        	handleCongestion(currentNode);
        	
        }
	}


	@Override
	public UniverseMessage handlePushDownMessage(
			Node currentNode, Node recipient, Serializable content) {
		if (content instanceof UniverseMessage) {
			return (UniverseMessage) content;
		} else {
			return UniverseMessage.wrapMessage(
					protocolId, currentNode, (BaseMessage)content);
		}
	}


	@Override
	public BaseMessage handlePushUpMessage(Node currentNode, Node sender,
			UniverseMessage message) {
		
		String head = message.getHead();
		System.out.println("Universe::handlePushUpMessage "+ currentNode.getID() + "<-" +
				message.getSender().getID() + " message " + 
				head);
		
		if (UniverseMessage.UPDATE_DESCRIPTOR.equals(head)) {
			handleLocalUniverseDescriptorChanged(
					(LocalUniverseDescriptor)message.getContent());
			// this message is for this protocol
			return null;
		}
		
		if (UniverseMessage.BROADCAST.equals(head)) {
			// forward the broadcast and send the message 
			// to the higher layer.
			return handleBroadCast(currentNode, (BaseMessage) message.getContent());
		}
		
		if (UniverseMessage.ADDFOLOWER.equals(head)) {
			addFollower(
					currentNode,
					message.getSender(), 
					(LocalUniverseDescriptor)message.getContent());
			return null;
		}
		
		if (UniverseMessage.ADDFOLOWER_ACK.equals(message.getHead())) {
			addFollowerAck(message.getSender());
			return null;
		}
		return null;
	}


	public void sendBroadCast(Node currentNode, BaseMessage message) {
		System.out.println("[{Node " + currentNode.getID() + "] " + message);
		// forward the broadcast to the top protocols
		EDProtocol receiver = (EDProtocol)currentNode.getProtocol(message.getPid());
		System.out.println("broadcast-content " + message.getContent());
		
		// We send to all the leaders
		for (Node topLeader: leaders) {
			UniverseMessage broadcast = 
					UniverseMessage.createBroadcast(protocolId, currentNode, message);
			System.out.println("********broadcast-content" + message.getContent());
			pushDownMessage(currentNode, topLeader, broadcast);
		}
		
		// Send to all the followers which are not grandchildrens
		ArrayList<Node> children = new ArrayList<Node>(followers);
		for (Node follower: followers) {
			children.removeAll(followerUniverseDescriptors.get(follower).getFollowers());
		}
		for (Node child: children) {
			System.out.println("!********broadcast-content" + message.getContent());
			UniverseMessage broadcast =
					UniverseMessage.createBroadcast(protocolId, currentNode, message);
			pushDownMessage(currentNode, child, broadcast);
		}
	}

	
	
}
