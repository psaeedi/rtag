package it.polimi.rtag;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import com.google.common.collect.HashMultimap;

import it.polimi.rtag.filters.AnycastFilter;
import it.polimi.rtag.filters.BroadcastFilter;
import it.polimi.rtag.filters.GroupcastFilter;
import it.polimi.rtag.filters.UnicastFilter;
import it.polimi.rtag.messaging.Ack;
import it.polimi.rtag.messaging.GroupFollowerCommand;
import it.polimi.rtag.messaging.GroupFollowerCommandAck;
import it.polimi.rtag.messaging.GroupLeaderCommand;
import it.polimi.rtag.messaging.GroupLeaderCommandAck;
import it.polimi.rtag.messaging.MessageSubjects;
import it.polimi.rtag.messaging.TupleMessage;
import polimi.reds.Filter;
import polimi.reds.Message;
import polimi.reds.MessageID;
import polimi.reds.NodeDescriptor;
import polimi.reds.Reply;
import polimi.reds.broker.overlay.AlreadyNeighborException;
import polimi.reds.broker.overlay.GenericOverlay;
import polimi.reds.broker.overlay.NeighborhoodChangeListener;
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

public class Node implements Router {

	/**
	 * The local universe to which this node belongs.
	 * Each node can only belong to a single universe 
	 * which would be its local universe!
	 */
	private GroupDescriptor followedUniverse;
	
	/***
	 * If the node is a universe leader then this
	 * is the universe it leads.
	 */
	private GroupDescriptor leadedUniverse;
	
	/**
	 * All the groups of which this node is leader. 
	 */
	private HashSet<GroupDescriptor> leadedGroups = new HashSet<GroupDescriptor>();
	
	/**
	 * All the groups of which this node is follower. 
	 */
	private HashSet<GroupDescriptor> followedGroups = new HashSet<GroupDescriptor>();
	
	private ArrayList<GroupCommunicationManager> groupCommunicationManagers =
			new ArrayList<GroupCommunicationManager>();
	
    public NodeDescriptor currentDescriptor;
    private GroupingStrategy strategy;
    private Overlay overlay;
    private RoutingStrategy routingStrategy;
	private ReplyManager replyManager;
	private SubscriptionTable subscriptionTable;
	private ReplyTable replyTable;
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
			
		setOverlay(new GenericOverlay(topologyManager, transport));

		// TODO implement a new routing strategy
		routingStrategy = new SubscriptionForwardingRoutingStrategy();
		
		setReplyManager(new ImmediateForwardReplyManager());
		
		subscriptionTable = new GenericTable();
		replyTable = new HashReplyTable();
		
		GroupCommunicationManager.createUniverse(this);
	}


	/**
	 * 
	 */
	private void setReplyManager(ReplyManager replyManager) {
		// TODO implement a new reply strategy
		this.replyManager = replyManager;
		replyManager.setRouter(this);
	  	
	}
    
    
	  /**
	   * Forwards the given reply to to its sender. According to the informations contained into the
	   * local reply table the reply will be sent to a specific neighbor or dropped if no information
	   * about its sender is contained in the reply table.
	   * <p>
	   * Note that re-configurator assume that the implementation of this method is synchronized on the
	   * router object, so that the re-configurator can acquire the lock when it needs to make sure that
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
		
		// TODO reimplement this by calling sendMessageCommunication
		
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
			GroupDescriptor group = groupcastFilter.getGroupDescriptor();
			if (group.isMember(currentDescriptor)) {
				// TODO add to tuplespace
				if (group.isLeader(currentDescriptor)) {
					for (NodeDescriptor recipient: group.getFollowers()) {
						sendMessageCommunication(recipient, tmessage);
					}
					// Forward to the parent group
					GroupDescriptor parentGroup = group.getParentGroup();
					if (parentGroup != null) {
						sendMessageCommunication(parentGroup.getLeader(), tmessage);
					}				
				} else {
					// The current node is not leader. Forward this to the leader.
					sendMessageCommunication(group.getLeader(), tmessage);
				}
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
		// TODO we want the ExtendedNodeDescriptor not the NodeDescriptor
		currentDescriptor = overlay.getNodeDescriptor();
		// Set listeners
		overlay.addPacketListener(this, PUBLISH);
		overlay.addPacketListener(this, REPLY);
	    overlay.setTrafficClass(Router.REPLY, Router.MESSAGE_CLASS);
	}

	@Override
	public void subscribe(NodeDescriptor node, Filter filter) {
		// TODO implement a routing strategy and move this code there
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
				// Otherwise we could simply send an error message.
			}
			
		} catch(IllegalArgumentException ex) {
			// Not a valid group filter: return
			return;
		}
	}

	@Override
	public void unsubscribe(NodeDescriptor node, Filter filter) {
		// TODO implement a routing strategy and move this code there
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
		// TODO implement a routing strategy and move this code there
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
			if (PUBLISH.equals(subject)) {
				handleMessagePublish(sender, (TupleMessage)packet);
			} else if (REPLY.equals(subject)) {
				handleMessageAck(sender, (Ack)packet);
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
	 * Handles {@link MessageSubjects#REPLY} messages received from a neighbor.
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
		
		// TODO if the response is not OK update the group descriptor used to compose this message
		// is probably not up to date and must be updated....
	}


	/**
	 * Handles {@link MessageSubjects#PUBLISH} messages
	 * 
	 * @param sender the sender node
	 * @param message the message content.
	 */
	private void handleMessagePublish(NodeDescriptor sender,
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
			GroupDescriptor group = groupcastFilter.getGroupDescriptor();
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
					GroupDescriptor parentGroup = group.getParentGroup();
					if (parentGroup != null) {
						sendMessageCommunication(parentGroup.getLeader(), message);
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
			overlay.send(REPLY, ack, recipient);
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
			overlay.send(PUBLISH, message, recipient);
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

	/**
	 * @param manager
	 * 
	 * @see GroupCommunicationManager#createGroup(Node, GroupDescriptor)
	 * @see GroupCommunicationManager#createGroup(Node, String, String, lights.Tuple)
	 * @see GroupCommunicationManager#createUniverse(Node)
	 */
	public void addGroup(GroupCommunicationManager manager) {
		this.groupCommunicationManagers.add(manager);
		GroupDescriptor descriptor = manager.getGroupDescriptor();
		if (descriptor.isLeader(currentDescriptor)) {
			leadedGroups.add(descriptor);
		} else if (descriptor.isFollower(currentDescriptor)) {
			followedGroups.add(descriptor);
		}
	}

}
