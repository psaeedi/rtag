package it.polimi.rtag;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import lights.Tuple;
import lights.interfaces.IField;


import com.google.common.collect.HashMultimap;

import it.polimi.rtag.messaging.Ack;
import it.polimi.rtag.messaging.GroupcastTupleMessage;
import it.polimi.rtag.messaging.MessageSubjects;
import it.polimi.rtag.messaging.TupleMessage;
import it.polimi.rtag.messaging.UnicastTupleMessage;
import polimi.reds.Filter;
import polimi.reds.MessageID;
import polimi.reds.NodeDescriptor;
import polimi.reds.broker.overlay.AlreadyNeighborException;
import polimi.reds.broker.overlay.GenericOverlay;
import polimi.reds.broker.overlay.Overlay;
import polimi.reds.broker.overlay.PacketListener;
import polimi.reds.broker.overlay.SimpleTopologyManager;
import polimi.reds.broker.overlay.TCPTransport;
import polimi.reds.broker.overlay.TopologyManager;
import polimi.reds.broker.overlay.Transport;

import static it.polimi.rtag.messaging.MessageSubjects.*;


/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)</p>.
 * 
 * TODO this should handle a collection of {@link GroupDiscoveredNotificationListener}
 */
public class Node implements PacketListener {

	
    private NodeDescriptor currentDescriptor;
    
    private GroupAwareTopologyManager topologyManager;
    private Overlay overlay;
    
    private GroupCommunicationDispatcher groupCommunicationDispatcher;
    
	private HashMultimap<NodeDescriptor, MessageID> pendingCommunicationMessages = HashMultimap.create();
	private HashSet<TupleMessage> recentlyReceivedMessages = new HashSet<TupleMessage>();
	
	private Object lock = new Object();
	
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
		topologyManager = new GroupAwareTopologyManager();
		Transport transport = new TCPTransport(port);
			
		setOverlay(new MessageCountingGenericOverlay(topologyManager, transport));
		
		groupCommunicationDispatcher = new GroupCommunicationDispatcher(this);
		
		groupCommunicationDispatcher.addGroupManager(GroupCommunicationManager.createUniverseCommunicationManager(this));
	}

	public Overlay getOverlay() {
		return overlay;
	}
	
	public void setOverlay(Overlay overlay) {
		if (this.overlay != null) {
			throw new AssertionError("Overlay already configured");
		}
		this.overlay = overlay;
		// TODO we want the ExtendedNodeDescriptor not the NodeDescriptor
		currentDescriptor = overlay.getNodeDescriptor();
		// Set listeners
		overlay.addPacketListener(this, COMMUNICATION);
		overlay.addPacketListener(this, COMMUNICATION_ACK);
	    overlay.setTrafficClass(COMMUNICATION_ACK, COMMUNICATION_ACK);
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
				if (packet instanceof UnicastTupleMessage) {
					handleUnicast((UnicastTupleMessage) packet);
				}
			} else if (COMMUNICATION_ACK.equals(subject)) {
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
	
	


	private void handleUnicast(UnicastTupleMessage packet) {
	// TODO Auto-generated method stub
	
}

	/**
	 * Handles {@link MessageSubjects#COMMUNICATION_ACK} messages received from a neighbor.
	 * 
	 * @param sender the sender node
	 * @param ack the acknowledge message
	 */
	private void handleMessageAck(NodeDescriptor sender, Ack ack) {
		// TODO do something depending on the response.
		// if the response is OK remove the message from the list of pending messages
		synchronized (lock) {
			pendingCommunicationMessages.remove(sender, ack.getOriginalMessageID());
		}
		
		// TODO if the response is not OK update the group descriptor used to compose this message
		// is probably not up to date and must be updated....
	}


	/**
	 * Handles {@link MessageSubjects#COMMUNICATION} messages
	 * 
	 * @param sender the sender node
	 * @param message the message content.
	 */
	public void handleGroupcast(NodeDescriptor sender, GroupcastTupleMessage message) {
		
		// Check if we have already received the message.
		// Duplicated messages are discarded.
		synchronized (lock) {
			if (recentlyReceivedMessages.contains(message)) {
				// Duplicated message received!
				sendMessageAck(sender, Ack.createOkAck(message.getID()));
				return;
			}
		}
		
		if (currentDescriptor.equals(sender)) {
			throw new RuntimeException("Groupcasting itself.");
		}
		
		// Check if the message is expired.
		// Expired messages are discarded.
		if (message.isExpired()) {
			// Expired
			sendMessageAck(sender, Ack.createExpiredAck(message.getID()));
			return;
		}
		
		// Add the message id to the collection.
		synchronized (lock) {
			recentlyReceivedMessages.add(message);
		}
		
		sendGroupcast(message);
	}

	public void sendGroupcast(GroupcastTupleMessage message) {
		GroupDescriptor recipient = message.getRecipient();
		GroupCommunicationManager manager = 
			groupCommunicationDispatcher.getGroupManagerWithName(recipient.getFriendlyName());
		
		if (manager != null) {
			manager.handleGroupcast(message);
			// TODO notify the application that a new message has arrived!
			return;
		} else {
			GroupDescriptor localUniverse = groupCommunicationDispatcher.getLocalUniverse();
			if (localUniverse.isLeader(currentDescriptor)) {
				GroupCommunicationManager universeManager = 
					groupCommunicationDispatcher.getLocalUniverseManager();
				universeManager.handleGroupcast(message);
			} else {
				//Nothing to do.
				return;
			}
		}
	}

	private void sendMessageAck(NodeDescriptor recipient, Ack ack) {
		try {
			overlay.send(COMMUNICATION_ACK, ack, recipient);
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
	public void sendUnicast(UnicastTupleMessage message) {
		NodeDescriptor recipient = message.getRecipient();
		if (recipient.equals(currentDescriptor)) {
			// TODO add message content to the current tuple space....
		} else {
			try {
				recipient = addNeigboor(recipient);
				overlay.send(COMMUNICATION, message, recipient);
				synchronized (lock) {
					pendingCommunicationMessages.put(recipient, message.getID());	
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private NodeDescriptor addNeigboor(NodeDescriptor descriptor) {
		if (currentDescriptor.equals(descriptor)) {
			throw new RuntimeException("Trying to talk to itself. " +
					"This should definitely NOT happen.");
		}

		if (overlay.isNeighborOf(descriptor)) {
			return descriptor;
		}
		
		NodeDescriptor node = null;
		for (String url: descriptor.getUrls()) {
			try {
				// TODO this may create a deadlock
				descriptor = getTopologyManager().addNeighbor(url);
			} catch (AlreadyNeighborException ex) {
				node = descriptor;
			} catch (Exception ex) {
				continue;
			}
		}
		return node;
	}

	protected void cleanPendingMessages() {
		// TODO clean the pendingCommunicationMessages map to avoid storing 
		// values for undelievered messages by removing all the expired ones.
	}

	protected void cleanRecentlyReceivedMessages() {
		// TODO empty the recently received messages collection by 
		// removing all the expired ones.
		synchronized(lock) {
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
	
	public NodeDescriptor getNodeDescriptor() {
		return currentDescriptor;
	}

	public void start() {
		overlay.start();
	}

	public void stop() {
		groupCommunicationDispatcher.removeAllGroupsAndDisconnect();
		overlay.stop();
	}

	/**
	 * @return the groupCommunicationDispatcher
	 */
	GroupCommunicationDispatcher getGroupCommunicationDispatcher() {
		return groupCommunicationDispatcher;
	}

	/**
	 * @return the topologyManager
	 */
	GroupAwareTopologyManager getTopologyManager() {
		return topologyManager;
	}

	public void joinGroup(String friendlyName) {
		// if string is equle to the constant universe, rise an excdption
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

	public GroupDescriptor getGroup(String friendlyName) {
		return groupCommunicationDispatcher.getGroupWithName(friendlyName);
	}

}
