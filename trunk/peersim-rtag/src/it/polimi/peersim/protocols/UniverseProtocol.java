package it.polimi.peersim.protocols;

import java.util.ArrayList;
import java.util.HashMap;

import peersim.cdsim.CDProtocol;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import it.polimi.peersim.initializers.ProtocolStackInitializer;
import it.polimi.peersim.messages.BaseMessage;
import it.polimi.peersim.messages.MessageCounting;
import it.polimi.peersim.messages.UniverseMessage;
import it.polimi.peersim.prtag.LocalUniverseDescriptor;
import it.polimi.peersim.prtag.UndeliverableMessageException;

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
public class UniverseProtocol extends ForwardingProtocol<UniverseMessage>
		implements CDProtocol, DiscoveryListener { 

	private static final String FOLLOWER_THRESHOLD = "follower_threshold";
	private final int followerThreshold;
	private static final String FOLLOWER_THRESHOLD_RATE = "follower_thresholdrate";
	private final int followerLeaderRate;
	
	ProtocolStackInitializer initializer ;
		
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
	private Node followerLeaders = null;

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
	
	/* (non-Javadoc)
	 * @see it.polimi.peersim.protocols.DiscoveryListener#notifyAddedNodes(peersim.core.Node, 
	 * java.util.ArrayList)
	 */
	@Override
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
			// TODO show a debug error message. 
			// this should not be happening 
			// because the discovery node was already connected
			return;
		}
		
		if (followers.contains(remoteNode)) {
			// TODO show a debug error message. 
			// this should not be happening 
			// because the discovery node was already connected
			return;
		}
		
		if (shouldLead(currentNode, remoteNode)) {
			// Nothing to be done
			// The follower ask to join
			return;
		} else {
			// This node will become the follower
			// and will start the join process
			try {
				UniverseMessage addfollowermessage = 
						UniverseMessage.createAddfollower(
								protocolId, currentNode, getLocaluniverse());
				pushDownMessage(currentNode, remoteNode, addfollowermessage);
			} catch(UndeliverableMessageException ex) {
				// It was impossible to connect to the new discovered node
				// Maybe it was just passing by?
				// Nothing to be done.
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see it.polimi.peersim.protocols.DiscoveryListener#notifyRemovedNodes(peersim.core.Node, java.util.ArrayList)
	 */
	@Override
	public void notifyRemovedNodes(Node currentNode, ArrayList<Node> removed) {
		for (Node lostNode: removed) {
			if (lostNode.getID() == currentNode.getID()){
				throw new RuntimeException("WARNING: node has lost itself.");
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
	    		try {
	    			MessageCounting.universeMessages(1);
	    			pushDownMessage(currentNode, topLeader, updateMsg);
	    		} catch(UndeliverableMessageException ex) {
	    			// The leader was unreachable
	    			// TODO shall we remove the leader from the list
	    			// 		or shall we simply wait the discovery protocol
	    			// 		update the neighbour list.
	    		}
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
		//System.out.println("][Node " + currentNode.getID() + 
				//"] Swapping follower " + remoteNode.getID());
		if (!followers.contains(remoteNode)) {
			
			throw new AssertionError("Node " + remoteNode.getID() + 
					" was not a follower");
		}
		
		try {
			UniverseMessage addfollowermessage = 
					UniverseMessage.createAddfollower(
							protocolId,
							currentNode,
							getLocaluniverse());
			pushDownMessage(currentNode, remoteNode, addfollowermessage);
		} catch(UndeliverableMessageException ex) {
			// The leader was unreachable
			// TODO shall we remove the leader from the list
			// 		or shall we simply wait the discovery protocol
			// 		update the neighbour list.
		}
	}
	
	private Node getLessCongestedFollower() {
		int followersCount = Integer.MAX_VALUE;
		Node lessCongested = null;
		int count = 0;
		for (Node follower: followers) {
			LocalUniverseDescriptor descriptor = 
					followerUniverseDescriptors.get(follower);
			if(descriptor!=null){
			count = descriptor.getFollowers().size();
			}
			if (count < followersCount) {
				followersCount = count;
				lessCongested = follower;
			}
		}
		return lessCongested;
	}
	
	private Node getFollowerWithOtherLeaders(Node currentNode) {
		for (Node follower: followers) {
			try {
				UniverseCommand command = new UniverseCommand
						(UniverseCommand.COUNT_LEADERS, 0);
				
				UniverseMessage message = 
						UniverseMessage.createUniverseCommand(
								protocolId,
								currentNode,
								command);
				pushDownMessage(currentNode, follower, message);
			} catch(UndeliverableMessageException ex) {
				// The follower was unreachable
    			// TODO shall we remove the follower from the list
    			// 		or shall we simply wait the discovery protocol
    			// 		update the neighbour list.
			}
			
			if(followerLeaders!=null){
				return followerLeaders;
			}
			
		}
		return null;
	}

	public boolean isCongested() {
		return (followers.size() > followerThreshold) && (followers.size() / 
				followerLeaderRate > leaders.size()); 
	}
	
    public boolean hasNoLeader() {
    	//System.out.println("node "+ localNode.getID()+ " the leaders size is:" + leaders.size());
		return leaders.isEmpty();
	}

	public void handleCongestion(Node currentNode) {
		//System.out.println("Starting congestion control loop.");
		while (isCongested()) {
			Node follower = getLessCongestedFollower();
			if (follower == null) {
				//System.out.println("handleCongestion() no folllower found.");
				return;
			}
			if(follower.isUp()){
			followerToLeader(currentNode, follower);}
		}
	}

	public void handleNodeNoLeader(Node currentNode) {
		//System.out.println("Starting no leader control loop.");
		while (hasNoLeader()) {
			Node follower = getFollowerWithOtherLeaders(currentNode);
			if (follower == null) {
				/*System.out.println(
						"handleNodeNoLeader() no follower found with other leaders." +
								currentNode.getID());*/
				return;
			}
			if(follower.isUp()){
			followerToLeader(currentNode, follower);}
		}
	}

	private void addFollower(Node currentNode, Node follower,
			LocalUniverseDescriptor remoteUniverse) {		
		try {
			UniverseMessage addfollowermessageAck =
					UniverseMessage.createAddfollowerAck(protocolId, currentNode);
			pushDownMessage(currentNode, follower, addfollowermessageAck);
			// TODO What happens if this message is lost?
		} catch(UndeliverableMessageException ex) {
			// It was impossible to reache the follower.
			// Aborting
			return;
		}

	    leaders.remove(follower);
		followers.add(follower);
		followerUniverseDescriptors.put(follower, remoteUniverse);
		localUniverse.addFollower(follower);

		
		// TODO update its leaders by sending them the updated descriptor
		for (Node topLeader: leaders) {
    		try {
        		UniverseMessage updateMsg = UniverseMessage.createUpdateDescriptor(
        				protocolId, currentNode, getLocaluniverse());
				pushDownMessage(currentNode, topLeader, updateMsg);
			} catch (UndeliverableMessageException e) {
				// The leader was unreachable
				// TODO shall we remove the leader from the list
				// 		or shall we simply wait the discovery protocol
				// 		update the neighbour list.
			}
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
			Node currentNode, Node recipient, BaseMessage content) {
		if (content instanceof UniverseMessage) {
			return (UniverseMessage) content;
		} else {
			return UniverseMessage.createSinglecast(
					protocolId, currentNode, (BaseMessage)content);
		}
	}


	@Override
	public BaseMessage handlePushUpMessage(Node currentNode, Node sender,
			UniverseMessage message) {
		
		String head = message.getHead();
		//System.out.println("Universe::handlePushUpMessage "+ currentNode.getID() + "<-" +
			//	message.getSender().getID() + " message " + 
				//head);
		
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
		
		if (UniverseMessage.SINGLECAST.equals(head)) {
			return (BaseMessage) message.getContent();
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
		
		if (UniverseMessage.UNIVERSE_COMMAND.equals(message.getHead())) {
			handleUniverseCommand(currentNode, sender, (UniverseCommand)message.getContent());
			return null;
		}
		return null;
	}


	private void handleUniverseCommand(Node currentNode, Node sender, UniverseCommand command) {
		// TODO Auto-generated method stub
		String commandName = command.getCommand();
		int content = command.getContent();
		
		if (UniverseCommand.COUNT_LEADERS.equals(commandName)) {
			handleUniverseCommandCountLeader(currentNode, sender);
			
		} else if (UniverseCommand.COUNT_LEADERS_RESPONSE.equals(commandName)) {
			handleUniverseCommandCountLeaderResponse(content, currentNode, sender);
		}
	}

	private void handleUniverseCommandCountLeaderResponse(int content, Node currentNode, Node sender){ 
		getFirstFollowerWithOtherLeader(currentNode,sender, content);
	}

	private void getFirstFollowerWithOtherLeader(Node currentNode, Node sender,  int content) {
		if(content > 1 ){
			followerLeaders = sender;
		}
		return;
	}

	/**
	 * Sends to the caller the number of followers. 
	 */
	private void handleUniverseCommandCountLeader(Node currentNode, Node sender) {
		try {
			UniverseCommand command = new UniverseCommand
					(UniverseCommand.COUNT_LEADERS_RESPONSE, leaders.size());
			
			UniverseMessage message = 
					UniverseMessage.createUniverseCommand(
							protocolId,
							currentNode,
							command);
			
			pushDownMessage(currentNode, sender, message);
		} catch (UndeliverableMessageException e) {
			// The follower was unreachable
			// TODO shall we remove the follower from the list
			// 		or shall we simply wait the discovery protocol
			// 		update the neighbor list.
		}
	}

	public void sendBroadCast(Node currentNode, BaseMessage message) {
		// We send to all the leaders
		for (Node topLeader: leaders) {
			UniverseMessage broadcast = 
					UniverseMessage.createBroadcast(protocolId, currentNode, message);
			try {
				pushDownMessage(currentNode, topLeader, broadcast);
			} catch (UndeliverableMessageException e) {
				// The leader was unreachable
				// TODO shall we remove the leader from the list
				// 		or shall we simply wait the discovery protocol
				// 		update the neighbor list.
			}
		}
		
		// Send to all the followers which are not grandchildrens
		ArrayList<Node> children = new ArrayList<Node>(followers);
		for (Node follower: followers) {
			 if(followerUniverseDescriptors.get(follower)!= null){
			    children.remove(followerUniverseDescriptors.get(follower).getFollowers());
			 }
		}
		for (Node child: children) {
			UniverseMessage broadcast =
					UniverseMessage.createBroadcast(protocolId, currentNode, message);
			try {
				pushDownMessage(currentNode, child, broadcast);
			} catch (UndeliverableMessageException e) {
				// The follower was unreachable
				// TODO shall we remove the follower from the list
				// 		or shall we simply wait the discovery protocol
				// 		update the neighbor list.
			}
		}
	}

	@Override
	protected void handleUnreliableRecipientException(Node currentNode,
			UndeliverableMessageException ex)
			throws UndeliverableMessageException {
		// Nothing to do
	}

	@Override
	protected void handleForwardedUnreliableRecipientException(
			Node currentNode, UndeliverableMessageException ex)
			throws UndeliverableMessageException {
		// Nothing to do
	}

	
	
}
