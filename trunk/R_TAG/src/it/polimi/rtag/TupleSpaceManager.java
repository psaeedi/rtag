/**
 * 
 */
package it.polimi.rtag;

import java.io.IOException;
import java.io.Serializable;

import polimi.reds.NodeDescriptor;
import polimi.reds.broker.overlay.NeighborhoodChangeListener;
import polimi.reds.broker.overlay.NotConnectedException;
import polimi.reds.broker.overlay.NotRunningException;
import polimi.reds.broker.overlay.Overlay;
import polimi.reds.broker.overlay.PacketListener;
import it.polimi.rtag.messaging.TupleGroupCommand;
import it.polimi.rtag.messaging.TupleGroupCommandAck;
import it.polimi.rtag.messaging.TupleMessage;
import it.polimi.rtag.messaging.TupleNetworkNotification;
import it.polimi.rtag.messaging.TupleMessage.Scope;
import lights.Field;
import lights.Tuple;
import lights.TupleSpace;
import lights.interfaces.IField;
import lights.interfaces.ITuple;
import lights.interfaces.TupleSpaceException;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class TupleSpaceManager implements PacketListener, NeighborhoodChangeListener {

	private static final String[] SUBJECTS = {
		TupleGroupCommand.SUBJECT,
		TupleGroupCommandAck.SUBJECT,
	};

	
	private TupleSpace tupleSpace = new TupleSpace();

	private Overlay overlay;
	private GroupCommunicationDispatcher dispatcher;
	private NodeDescriptor currentNode;
	

	public TupleSpaceManager(Overlay overlay,
			GroupCommunicationDispatcher dispatcher) {
		this.dispatcher = dispatcher;
		setOverlay(overlay);
	}
	
	/**
	 * @param overlay the overlay to set
	 */
	private void setOverlay(Overlay overlay) {
		if (this.overlay != null) {
			throw new AssertionError("Overlay already configured");
		}
		if (overlay == null) {
			throw new AssertionError("Overlay cannot be null");
		}
		this.overlay = overlay;
		for (String subject: SUBJECTS) {
			overlay.addPacketListener(this, subject);
		}
		overlay.addNeighborhoodChangeListener(this);
	}
	
	/**
	 * Query the tuple space if there is a {@link Scope#NETWORK} tuple
	 * identifying the give hierarchy name.>(p>
	 * 
	 * If there are multiple tuples for the same
	 * hierarchy then a random one is returned.</p>
	 * 
	 * Multiple tuples for the same hierarchy can coexist
	 * while the hierarchy is still forming.
	 * 
	 * @return the node descriptor of the hierarchy top leader.
	 */
	public GroupDescriptor getLeaderForHierarchy(String hierarchyName) {
		ITuple template = new Tuple()
				.add(new Field().setValue(Scope.NETWORK))
				.add(new Field().setType(long.class))
				.add(new Field().setValue(hierarchyName))
				.add(new Field().setValue(TupleNetworkNotification.SUBJECT))
				.add(new Field().setType(GroupDescriptor.class));
		try {
			ITuple tuple = tupleSpace.rdp(template);
			if (tuple != null) {
				return (GroupDescriptor)tuple.getFields()[4].getValue(); //TODO this does not work
			}
		} catch (TupleSpaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Sets a leader for the given hierarchy 
	 */
	public void setLeaderForHierarchy(GroupDescriptor groupDescriptor) {
		TupleMessage message = new TupleNetworkNotification(
				groupDescriptor);
		storeAndSend(message);
	}
	
	/**
	 * When the top leader collapses this entry is removed.
	 */
	public void removeLeaderForHierarchy(String hierarchyName, NodeDescriptor leader) {
		
	}
	
	public void setMessage(Scope scope, String recipient, TupleMessage message) {
		// TODO
	}
	
	public TupleMessage getMessage(Scope scope, String recipient) {
		// TODO
	}
	
	public TupleMessage[] getMessages(Scope scope, String recipient) {
		// TODO
	}
	
	public TupleMessage[] getAllMessages() {
		// TODO
	}
	
	public void removeMessage(TupleMessage message) {
		// TODO
	}
		
	public void storeAndSend(TupleMessage message) {
		if (message.isExpired()) {
			// the tuple is expired
			return;
		}
		ITuple tuple = new Tuple()
			.add(new Field().setValue(message.getScope()))
			.add(new Field().setValue(message.getExpireTime()))
			.add(new Field().setValue(message.getRecipient()))
			.add(new Field().setValue(message.getSubject()))
			.add(new Field().setValue(message));
		
		try {
			tupleSpace.out(tuple);
		} catch (TupleSpaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		send(message);
	}
	
	public void sendWithoutStoring(TupleMessage message) {
		send(message);
	}
	
	private void send(TupleMessage message) {
		switch (message.getScope()) {
			case NODE: {
				sendToNode((NodeDescriptor)message.getRecipient(), message);
				break;
			}
			case GROUP: {
				sendToGroup((GroupDescriptor)message.getRecipient(), message);
				break;
			}
			case HIERARCHY: {
				sendToHierarchy((String)message.getRecipient(), message);
				break;
			}
			case NETWORK: {
				sendToNetwork((String)message.getRecipient(), message);
				break;
			}
		}
	}
	

	private void sendToNetwork(String recipient, TupleMessage message) {
		GroupCommunicationManager manager = 
				dispatcher.getLocalUniverseManager();
		if (manager != null) {
			manager.forwardTupleMessage(message, currentNode);
		}
	}

	private void sendToHierarchy(String recipient, TupleMessage message) {
		GroupCommunicationManager manager = 
			dispatcher.getFollowedGroupByFriendlyName(recipient);
		if (manager != null) {
			manager.forwardTupleMessage(message, currentNode);
		}
	}

	private void sendToGroup(GroupDescriptor recipient, TupleMessage message) {
		GroupCommunicationManager manager = 
				dispatcher.getGroupManagerForDescriptor(recipient);
		if (manager != null) {
			manager.forwardTupleMessage(message, currentNode);
		}
	}

	/**
	 * If the recipient is connected send it a message, otherwise wait for sync
	 * 
	 * @param recipient
	 * @param content
	 */
	private void sendToNode(NodeDescriptor recipient, TupleMessage message) {
		if (overlay.isNeighborOf(recipient)) {
			try {
				overlay.send(message.getSubject(), message, recipient);
				removeMessage(message);
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}

	public void synchNode() {
		// TODO
	}
	public void synchGroup() {
		// TODO
	}
	public void synchHierarchy() {
		// TODO
	}
	public void synchNetwork() {
		// TODO
	}
	
	
	/**
	 * Removes all the messages of a given scope for a given recipient.
	 * This is useful when leaving a group or a hierarchy. 
	 */
	public void removeMessage(Scope scope, String recipient) {}
	
	public void removeExpiredMessages() {}
	

	@Override
	public void notifyPacketArrived(String subject, NodeDescriptor sender,
			Serializable message) {
		if (TupleGroupCommand.SUBJECT.equals(subject)) {
			storeAndSend((TupleMessage) message);
		} else if (TupleGroupCommandAck.SUBJECT.equals(subject)) {
			storeAndSend((TupleGroupCommandAck) message);
		} else if (TupleNetworkNotification.SUBJECT.equals(subject)) {
			storeAndSend((TupleMessage) message);
		}
		
	}

	@Override
	public void notifyNeighborAdded(NodeDescriptor arg0, Serializable arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyNeighborDead(NodeDescriptor arg0, Serializable arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyNeighborRemoved(NodeDescriptor arg0) {
		// TODO Auto-generated method stub
		
	}
		
}
