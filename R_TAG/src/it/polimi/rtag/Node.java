package it.polimi.rtag;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import com.google.common.collect.HashMultimap;

import it.polimi.rtag.filters.AnycastFilter;
import it.polimi.rtag.filters.BroadcastFilter;
import it.polimi.rtag.filters.GroupcastFilter;
import it.polimi.rtag.filters.UnicastFilter;
import it.polimi.rtag.messaging.Ack;
import it.polimi.rtag.messaging.GroupLeaderCommand;
import it.polimi.rtag.messaging.GroupLeaderCommandAck;
import it.polimi.rtag.messaging.JoinGroupRequest;
import it.polimi.rtag.messaging.JoinGroupResponse;
import it.polimi.rtag.messaging.MessageSubjects;
import it.polimi.rtag.messaging.TupleMessage;
import polimi.reds.Filter;
import polimi.reds.Message;
import polimi.reds.MessageID;
import polimi.reds.NodeDescriptor;
import polimi.reds.Reply;
import polimi.reds.broker.overlay.AlreadyNeighborException;
import polimi.reds.broker.overlay.GenericOverlay;
import polimi.reds.broker.overlay.NotConnectedException;
import polimi.reds.broker.overlay.NotRunningException;
import polimi.reds.broker.overlay.Overlay;
import polimi.reds.broker.overlay.SimpleTopologyManager;
import polimi.reds.broker.overlay.TCPTransport;
import polimi.reds.broker.overlay.TopologyManager;
import polimi.reds.broker.overlay.Transport;
import polimi.reds.broker.routing.GenericTable;
import polimi.reds.broker.routing.HashReplyTable;
import polimi.reds.broker.routing.ImmediateForwardReplyManager;
import polimi.reds.broker.routing.ReplyManager;
import polimi.reds.broker.routing.ReplyTable;
import polimi.reds.broker.routing.Router;
import polimi.reds.broker.routing.RoutingStrategy;
import polimi.reds.broker.routing.SubscriptionForwardingRoutingStrategy;
import polimi.reds.broker.routing.SubscriptionTable;

import static it.polimi.rtag.messaging.MessageSubjects.*;

public class Node implements Router{

    public NodeDescriptor currentDescriptor;
    private GroupingStrategy strategy;
    private Overlay overlay;
    private RoutingStrategy routingStrategy;
	private ReplyManager replyManager;
	SubscriptionTable subscriptionTable;
	ReplyTable replyTable;
	private Object nodeReply;
    
	private HashMultimap<NodeDescriptor, MessageID> pendingCommunicationMessages = HashMultimap.create();
	private HashSet<TupleMessage> recentlyReceivedMessages = new HashSet<TupleMessage>();
	
	public Node(String address, int port) {
		/**
		 * This interface is the "core" of a REDS broker. Classes that implement this interface constitutes
		 * the main component of a REDS broker. It is registered to the <code>Overlay</code> to receive
		 * messages coming from the other nodes of the network. In most cases it delegates its main
		 * functionalities to the other components that compose a REDS broker: namely, the
		 * <code>RoutingStrategy</code> and the <code>ReplyManager</code>. It holds the common data
		 * structure managed by those components: the <code>SubscriptionTable</code> and the
		 * <code>ReplyTable</code>.
		 */
		
		// TODO implement a new routing topology manager
		TopologyManager topologyManager = new SimpleTopologyManager();
		Transport transport = new TCPTransport(port);
		
		overlay = new GenericOverlay(topologyManager, transport);
		
		// TODO implement a new routing strategy
		routingStrategy = new SubscriptionForwardingRoutingStrategy();
		
		// TODO implement a new reply strategy
		replyManager = new ImmediateForwardReplyManager();
		
		subscriptionTable = new GenericTable();
		
		replyTable = new HashReplyTable();
	}
    
    
	  /**
	   * Forwards the given reply to to its sender. According to the informations contained into the
	   * local reply table the reply will be sent to a specific neighbor or dropped if no information
	   * about its sender is contained in the reply table.
	   * <p>
	   * Note that reconfigurator assume that the implementation of this method is synchronized on the
	   * router object, so that the reconfigurator can acquire the lock when it needs to make sure that
	   * no router operations are executed concurrently with reconfiguration actions.
	   * 
	   * @param reply the reply to be sent.
	   */
	@Override
	public void forwardReply(Reply reply) {
		replyManager.forwardReply(reply);
	  }

	@Override
	public Overlay getOverlay() {
		return overlay;
	}

	@Override
	public ReplyTable getReplyTable() {
		return replyTable;
	}

	@Override
	public SubscriptionTable getSubscriptionTable() {
		return subscriptionTable;
	}

  /**
   * THE APPLICATION IS CALLING THIS!
   * 
   * Publish the given message coming from the specified neighbor. Depending on the routing policy
   * adopted, this requires to forward the given message to some or any of the neighbors of the
   * broker this router is part of.<br>
   * If <code>message</code> is instance of<code>Repliable</code>, the <code>Router</code>
   * needs to activate the <code>ReplyManager</code> to record the message in the
   * <code>ReplyTable</code>.
   * <p>
   * Note that reconfigurators assume that the implementation of this method is synchronized on the
   * router object, so that the reconfigurator can acquire the lock when it needs to make sure that
   * no router operations are executed concurrently with reconfiguration actions.
   * 
   * @param neighborID the identifier of the neighbor from which the message was received.
   * @param message the <code>Message</code> to be published.
   * 
   */
	@Override
	public void publish(NodeDescriptor sender, Message message) {
		if (!(message instanceof TupleMessage)) {
			throw new RuntimeException(
					"Can only publish TupleMessages. Found:" + message);
		}
		
		//publish the message in the tuple space
		TupleMessage tmessage = (TupleMessage)message;
		Filter filter = tmessage.getFilter();
		if (filter instanceof UnicastFilter) {
			// Unicast
			UnicastFilter unicastFilter = (UnicastFilter)filter;
			NodeDescriptor recipient = unicastFilter.getRecipient();
			if (recipient.equals(currentDescriptor)) {
				// TODO add message content to the current tuple space....
			} else {
				// Note: followers of the same leader know each other.
				sendMessageCommunication(recipient, tmessage);
			}
		} else if (filter instanceof AnycastFilter){
			// Anycast
			// TODO select a recipient matching the criteria
			// TODO overlay.send(title, tmessage, recipient);
		} else if (filter instanceof GroupcastFilter) {
			// Groupcast
			GroupcastFilter groupcastFilter = (GroupcastFilter)filter;
			GroupDescriptor group = GroupcastFilter.getGroupDescriptor();
			if (group.isLeader(currentDescriptor)) {
				// TODO add message content to the current tuple space....
				for (NodeDescriptor recipient: group.getFollowers()) {
					sendMessageCommunication(recipient, tmessage);
				}
				// Forward to the parent group
				NodeDescriptor parent = group.getParent();
				if (parent != null) {
					sendMessageCommunication(parent, tmessage);
				}				
			} else {
				// The current node is not leader. Forward this to the leader.
				sendMessageCommunication(group.getLeader(), tmessage);
			}
		} else if (filter instanceof BroadcastFilter) {
			// Broadcast
			// TODO add to tuplespace?
			for (NodeDescriptor recipient: overlay.getNeighbors()) {
				sendMessageCommunication(recipient, tmessage);
				// TODO how do we prevent duplicated messages from being sent?
			}
		} else {
			throw new AssertionError("Unrecognizer filter type: " + filter);
		}
	}

	@Override
	public void setOverlay(Overlay overlay) {
		if (this.overlay != null) {
			throw new AssertionError("Overlay already configured");
		}
		this.overlay = overlay;
	}

	@Override
	public void subscribe(NodeDescriptor node, Filter filter) {
		// Check if the filter represents a group
		try {
			GroupcastFilter groupFilter = GroupcastFilter.createFromFilter(filter);
			// Check if this node is the right group leader
			GroupDescriptor groupDescriptor = groupFilter.getDescriptor();
			if (groupDescriptor.isLeader(currentDescriptor)) {
				try {
					overlay.addNeighbor(node.getUrls()[0]);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// TODO Add node to the group and notify all the other followers
			} else {
				// TODO decide what to do if the current node is not the 
				// group leader. We could reply with the leader address if known.
				// Otherwhise we could simply send an error message.
			}
			
		} catch(IllegalArgumentException ex) {
			// Not a valid group filter: return
			return;
		}
	}

	@Override
	public void unsubscribe(NodeDescriptor node, Filter filter) {
		// Check if the filter represents a group
		try {
			GroupcastFilter groupFilter = GroupcastFilter.createFromFilter(filter);
			// Check if this node is the right group leader
			GroupDescriptor groupDescriptor = groupFilter.getDescriptor();
			if (groupDescriptor.isLeader(currentDescriptor)) {
				// TODO Remove node to the group and notify all the other followers
				overlay.removeNeighbor(node);
			} else {
				// TODO decide what to do if the current node is not the 
				// group leader. We could reply with the leader address if known.
				// Otherwise we could simply send an error message.
			}
			
		} catch(IllegalArgumentException ex) {
			// Not a valid group filter: return
			return;
		}
	}

	@Override
	public void unsubscribeAll(NodeDescriptor node) {
		// Invoked before a node is quitting or if a node has collapsed
		overlay.removeNeighbor(node);
		// TODO the node has to be removed from all the groups
	}

  /**
   * This method is called by the <code>Overlay</code> whenever a new packet arrives from a
   * neighbor of the local node.
   * 
   * @param subject the subject the packet was addressed to.
   * @param source the <code>NodeDescriptor</code> of the neighbor the packet comes from.
   * @param data the received packet.
   */
	@Override
	public void notifyPacketArrived(String subject, NodeDescriptor sender,
			Serializable packet) {
		try {
			// Handle each received message according to its subject
			if (COMMUNICATION.equals(subject)) {
				handleMessageCommunication(sender, (TupleMessage)packet);
			} else if (ACK.equals(subject)) {
				handleMessageAck(sender, (Ack)packet);
			} else if (JOIN_GROUP_REQUEST.equals(subject)) {
				handleMessageJoinGroupRequest(sender, (JoinGroupRequest)packet);
			} else if (JOIN_GROUP_RESPONSE.equals(subject)) {
				handleMessageJoinGroupResponse(sender, (JoinGroupResponse)packet);
			} else if (GROUP_LEADER_COMMAND.equals(subject)) {
				handleMessageGroupLeaderCommand(sender, (GroupLeaderCommand)packet);
			} else if (GROUP_LEADER_COMMAND_ACK.equals(subject)) {
				handleMessageGroupLeaderCommandAck(sender, (GroupLeaderCommandAck)packet);
			} else {
				// All the message subjects should be handled
				throw new RuntimeException("Unrecognized message subject: " + subject);
			}
		} catch (ClassCastException ex) {
			throw new RuntimeException(
					"Invalid package type. Packages must be of type Tuple message. Found: " + 
					packet);
		}
	}
	
	/**
	 * Handles {@link MessageSubjects#GROUP_LEADER_COMMAND_ACK} messages
	 * received from a follower.
	 * 
	 * @param sender
	 * @param message
	 */
	private void handleMessageGroupLeaderCommandAck(NodeDescriptor sender,
		GroupLeaderCommandAck message) {
		// TODO check if the command was sent by this node
		// TODO handle the response
	}

	/**
	 * Handles {@link MessageSubjects#GROUP_LEADER_COMMAND} messages by performing
	 * the proper action according to what is commanded by the leader.
	 * 
	 * @param sender
	 * @param message
	 */
	private void handleMessageGroupLeaderCommand(NodeDescriptor sender,
		GroupLeaderCommand message) {
		GroupDescriptor group = message.getGroupDescriptor();
		// TODO if the current node is not in the group notify the sender
		if (!group.isLeader(sender)) {
			// The sender is not the group leader
			// TODO do something then return.
			return;
		}
		String command = message.getCommand();
		// TODO handle the command 
}


	/**
	 * Handles {@link MessageSubjects#JOIN_GROUP_RESPONSE} messages received from a neighbor.
	 * A node should receive messages of this type only as a response to a join group
	 * request that it has sent.
	 * 
	 * @param sender the sender node
	 * @param message the join group message
	 */
	private void handleMessageJoinGroupResponse(NodeDescriptor sender,
		JoinGroupResponse packet) {
		// TODO handle response
	}


	/**
	 * Handles {@link MessageSubjects#JOIN_GROUP_REQUEST} messages received from a neighbor.
	 * 
	 * @param sender the sender node
	 * @param message the join group message
	 */
	private void handleMessageJoinGroupRequest(NodeDescriptor sender, JoinGroupRequest message) {
		// TODO if the current nod is not the leader of the given group 
		// then raise an exception	
		
		// TODO if we accept the join group request then we need to:
		// 1 - add the sender to the group
		// 2 - send him a join group response
		// 3 - notify the neighbor
		
		// TODO if we do not accept the join we can
		// 1 - simply reject
		// 2 - suggest another leader (e.g. a subgroup or a parent group leader)
	}


	/**
	 * Handles {@link MessageSubjects#ACK} messages received from a neighbor.
	 * 
	 * @param sender the sender node
	 * @param ack the acknowledge message
	 */
	private void handleMessageAck(NodeDescriptor sender, Ack ack) {
		// TODO do something depending on the response.
		// if the response is OK remove the message from the list of pending messages
		synchronized (pendingCommunicationMessages) {
			pendingCommunicationMessages.remove(sender, ack.getOriginalMessageID());
		}
		
		// if the response is not OK update the group descriptor used to compose this message
		// is probably not up to date and must be updated....
	}


	/**
	 * Handles {@link MessageSubjects#COMMUNICATION} messages
	 * 
	 * @param sender the sender node
	 * @param message the message content.
	 */
	private void handleMessageCommunication(NodeDescriptor sender,
			TupleMessage message) {
		
		// Check if we have already received the message.
		// Duplicated messages are discarded.
		synchronized (recentlyReceivedMessages) {
			if (recentlyReceivedMessages.contains(message)) {
				// Duplicated message received!
				sendMessageAck(sender, Ack.createOkAck(message.getID()));
				return;
			}
		}
	
		// Check if the message is expired.
		// Expired messages are discarded.
		if (message.isExpired()) {
			// Expired
			sendMessageAck(sender, Ack.createExpiredAck(message.getID()));
			return;
		}
		
		// Add the message id to the collection.
		synchronized (recentlyReceivedMessages) {
			recentlyReceivedMessages.add(message);
		}
		
		
		Filter filter = message.getFilter();
		if (filter instanceof UnicastFilter) {
			// Unicast
			UnicastFilter unicastFilter = (UnicastFilter)filter;
			NodeDescriptor recipient = unicastFilter.getRecipient();
			if (recipient.equals(currentDescriptor)) {
				// TODO add message content to the current tuple space....
				sendMessageAck(sender, Ack.createOkAck(message.getID()));
				return;
			} else {
				// The node has received a unicast which was not for it
				sendMessageAck(sender, Ack.createWrongRecipientAck(message.getID()));
				return;
			}
		} else if (filter instanceof AnycastFilter){
			// Anycast
			AnycastFilter anycastFilter = (AnycastFilter)filter;
			GroupDescriptor group = anycastFilter.getGroup();
			if (!group.isMember(currentDescriptor)) {
				sendMessageAck(sender, Ack.createNotGroupFollowerAck(message.getID()));
				return;
			} else {
				sendMessageAck(sender, Ack.createOkAck(message.getID()));
				return;
			}
		} else if (filter instanceof GroupcastFilter) {
			// Groupcast
			GroupcastFilter groupcastFilter = (GroupcastFilter)filter;
			GroupDescriptor group = GroupcastFilter.getGroupDescriptor();
			if (group.isMember(currentDescriptor)) {
				// TODO add message content to the local tuple space....
				if (group.isLeader(currentDescriptor)) {
					
					
					// When a leader receives a groupcast it sends it to all
					// the followers with the exclusion of the one sending it
					// then it forwards it to the parent group if any.
					for (NodeDescriptor recipient: group.getFollowers()) {
						if (sender.equals(recipient)) {
							// We do not send the message to the sender
							continue;
						}
						sendMessageCommunication(recipient, message);
					}
					// Forward to the parent group
					NodeDescriptor parent = group.getParent();
					if (parent != null) {
						sendMessageCommunication(parent, message);
					}
					
				} else {
					// The current node is not leader. 
					// Nothing to do.
				}
				sendMessageAck(sender, Ack.createOkAck(message.getID()));
				return;
				
			} else {
				sendMessageAck(sender, Ack.createNotGroupFollowerAck(message.getID()));
				return;
			}
		} else if (filter instanceof BroadcastFilter) {
			// Broadcast
			for (NodeDescriptor recipient: overlay.getNeighbors()) {
				if (sender.equals(recipient)) {
					// We do not send the message to the sender
					continue;
				}
				sendMessageCommunication(recipient, message);
			}
			sendMessageAck(sender, Ack.createOkAck(message.getID()));
			return;
		} else {
			// TODO shall we send an ack saying that sth went wrong?
			throw new AssertionError("Unrecognizer filter type: " + filter);
		}
	}

	private void sendMessageAck(NodeDescriptor recipient, Ack ack) {
		try {
			overlay.send(ACK, ack, recipient);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	/**
	 * Sends the given message to the specified node.
	 * 
	 * @param recipient
	 * @param message
	 */
	public void sendMessageCommunication(NodeDescriptor recipient, TupleMessage message) {
		// TODO check if message and recipient matches
		try {
			overlay.send(COMMUNICATION, message, recipient);
			synchronized (pendingCommunicationMessages) {
				pendingCommunicationMessages.put(recipient, message.getID());	
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void cleanPendingMessages() {
		// TODO clean the pendingCommunicationMessages map to avoid storing 
		// values for undelievered messages by removing all the expired ones.
	}

	private void cleanRecentlyReceivedMessages() {
		// TODO empty the recently received messages collection by 
		// removing all the expired ones.
		synchronized(recentlyReceivedMessages) {
			ArrayList<TupleMessage> messages = new ArrayList<TupleMessage>(recentlyReceivedMessages);
			for (int i = messages.size(); i > -1; i--) {
				TupleMessage message = messages.get(i);
				if (message.isExpired()) {
					messages.remove(i);
				}
			}
			recentlyReceivedMessages = new HashSet<TupleMessage>(messages);
		}
	}
	
	@Override
	public NodeDescriptor getID() {
		return currentDescriptor;
	}

	public void setStrategy(GroupingStrategy strategy) {
		this.strategy = strategy;
	}

	public GroupingStrategy getStrategy() {
		return strategy;
	}

	public NodeDescriptor getCurrentDescriptor() {
		return currentDescriptor;
	}

	public void setCurrentDescriptor(NodeDescriptor currentDescriptor) {
		this.currentDescriptor = currentDescriptor;
	}
	
}
