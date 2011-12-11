/**
 * 
 */
package it.polimi.rtag;

import java.io.Serializable;

import polimi.reds.NodeDescriptor;
import polimi.reds.broker.overlay.NeighborhoodChangeListener;
import polimi.reds.broker.overlay.Overlay;
import polimi.reds.broker.overlay.PacketListener;
import it.polimi.rtag.messaging.TupleGroupCommand;
import it.polimi.rtag.messaging.TupleMessageAck;
import it.polimi.rtag.messaging.TupleMessage;
import it.polimi.rtag.messaging.TupleNetworkNotification;
import it.polimi.rtag.messaging.TupleNodeNotification;
import it.polimi.rtag.messaging.TupleMessage.Scope;
import lights.Field;
import lights.Tuple;
import lights.TupleSpace;
import lights.interfaces.ITuple;
import lights.interfaces.TupleSpaceException;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class TupleSpaceManager implements PacketListener, NeighborhoodChangeListener {

	private static final String[] SUBJECTS = {
		TupleGroupCommand.SUBJECT,
		TupleMessageAck.SUBJECT,
		TupleNodeNotification.SUBJECT,
		TupleNetworkNotification.SUBJECT,
		TupleMessage.CUSTOM_MESSAGE
	};

	
	private TupleSpace tupleSpace = new TupleSpace();

	private Overlay overlay;
	private GroupCommunicationDispatcher dispatcher;
	private NodeDescriptor currentNode;
	

	public TupleSpaceManager(Overlay overlay,
			Node node) {
		this.currentNode = node.getNodeDescriptor();
		this.dispatcher = node.getGroupCommunicationDispatcher();
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
	
	private ITuple createTemplate(Scope scope, Long expireTime,
			Serializable recipient, String subject, String command,
			Class clazz) {
		ITuple template = new Tuple()
			.add(scope == null ? new Field().setType(Scope.class):
					new Field().setValue(scope)) //scope
			.add(expireTime == null ? new Field().setType(Long.class):
					new Field().setValue(expireTime)) // expire
			.add(recipient == null ? new Field().setType(Serializable.class):
					new Field().setValue(recipient)) //recipient
			.add(subject == null ? new Field().setType(String.class):
					new Field().setValue(subject)) // subject
			.add(command == null ? new Field().setType(String.class):
					new Field().setValue(command)) // command
			.add(new Field().setType(clazz)); //original message
		return template;
	}
	
	private ITuple createTemplateForNetworkNotification() {
		ITuple template = new Tuple()
			.add(new Field().setValue(Scope.NETWORK)) //scope
			.add(new Field().setType(Long.class)) // expire
			.add(new Field().setType(String.class)) //recipient
			.add(new Field().setValue(TupleNetworkNotification.SUBJECT)) // subject
			.add(new Field().setType(String.class)) // command
			.add(new Field().setType(TupleNetworkNotification.class)); //original message
		return template;
	}
	
	private ITuple createTuple(Scope scope, Long expireTime,
			Serializable recipient, String subject, String command,
			TupleMessage message) {
		ITuple tuple = new Tuple()
			.add(new Field().setValue(scope)) //scope
			.add(new Field().setValue(expireTime)) //timeout
			.add(new Field().setValue(recipient)) // recipient
			.add(new Field().setValue(subject)) // subject
			.add(new Field().setValue(command)) // subject
			.add(new Field().setValue(message)); // original message
		return tuple;
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
		ITuple template = createTemplate(Scope.NETWORK, null, hierarchyName,
				TupleNetworkNotification.SUBJECT, TupleNetworkNotification.ADD, 
				TupleNetworkNotification.class);
		try {
			ITuple tuple = tupleSpace.rdp(template);
			if (tuple != null) {
				TupleNetworkNotification message = (TupleNetworkNotification)
						((Field)tuple.getFields()[5]).getValue();
				return message.getGroupDescriptor();
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
		System.out.println("Adding leader for hierachy: " + groupDescriptor);
		TupleMessage message = new TupleNetworkNotification(
				groupDescriptor, TupleNetworkNotification.ADD);
		storeAndSend(message);
	}
	
	/**
	 * When the top leader collapses this entry is removed.
	 */
	private void removeLeaderForHierarchy(GroupDescriptor groupDescriptor) {
		ITuple template = createTemplate(Scope.NETWORK,
				null, groupDescriptor.getFriendlyName(),
				TupleNetworkNotification.SUBJECT, TupleNetworkNotification.ADD,
				TupleNetworkNotification.class);
		try {
			tupleSpace.inp(template);
		} catch (TupleSpaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		storeAndSend(new TupleNetworkNotification(
				groupDescriptor, TupleNetworkNotification.REMOVE));
	}
	
	public ITuple[] queryTuplespace(Scope scope, Long expireTime,
			Serializable recipient, String subject, String command,
			Class clazz) {
		ITuple template = createTemplate(scope, expireTime, 
				recipient, subject, command, clazz);
		try {
			return tupleSpace.rdg(template);
		} catch (TupleSpaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private TupleMessage[] getMessages(Scope scope, Serializable recipient) {
		ITuple template = createTemplate(scope, null, recipient,
				null, null, TupleMessage.class);
		try {
			ITuple[] results = tupleSpace.rdg(template);
			if (results == null) {
				return new TupleMessage[0];
			}
			TupleMessage[] messages = new TupleMessage[results.length];
			for (int i = 0; i < results.length; i++) {
				ITuple t = results[i];
				messages[i] = (TupleMessage)((Field)t.getFields()[4]).getValue();
			}
			return messages;
		} catch (TupleSpaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Removes all the messages of a given scope for a given recipient.
	 * This is useful when leaving a group or a hierarchy. 
	 */
	private void removeMessage(Scope scope, Serializable recipient) {
		ITuple template = createTemplate(scope, null,
				recipient, null, null, TupleMessage.class); // TODO confirm this works
		try {
			tupleSpace.ing(template);
		} catch (TupleSpaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void removeMessage(TupleMessage message) {
		ITuple template = new Tuple()
				.add(new Field().setType(Scope.class))
				.add(new Field().setType(long.class))
				.add(new Field().setType(Serializable.class))
				.add(new Field().setType(String.class))
				.add(new Field().setType(String.class))
				.add(new Field().setValue(message));
		try {
			tupleSpace.inp(template);
		} catch (TupleSpaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	public void storeHandleAndForward(TupleMessage message, NodeDescriptor sender) {
		if (message.isExpired()) {
			// the tuple is expired
			return;
		}
		System.out.println("storeHandleAndForward " + message.getSubject() + " " + message.getCommand());
		ITuple tuple = createTuple(message.getScope(),
				new Long(message.getExpireTime()), message.getRecipient(),
				message.getSubject(), message.getCommand(), message);		
		try {
			tupleSpace.out(tuple);
		} catch (TupleSpaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		switch (message.getScope()) {
			case NODE: {
				if (message instanceof TupleNodeNotification) {
					handleNodeMessage(
							(NodeDescriptor)message.getRecipient(),
							(TupleNodeNotification)message,
							sender);
				} else if (message instanceof TupleMessageAck) {
					handleNodeMessageAck(
							(NodeDescriptor)message.getRecipient(),
							(TupleMessageAck)message,
							sender);
				} else {
					// TODO handle other
				}
				break;
			}
			case GROUP: {
				handleGroupMessage(
						(GroupDescriptor)message.getRecipient(),
						(TupleGroupCommand)message,
						sender);
				break;
			}
			case HIERARCHY: {
				handleHierarchyMessage((String)message.getRecipient(), message, sender);
				break;
			}
			case NETWORK: {
				handleNetworkMessage(message, sender);
				break;
			}
		}
	}
	
	private void handleNetworkMessage(TupleMessage message,
			NodeDescriptor sender) {
		GroupCommunicationManager universeManager = 
				dispatcher.getGroupManagerForHierarchy(GroupDescriptor.UNIVERSE);
		if (universeManager != null) {
			universeManager.handleAndForwardTupleMessage(message, sender);
		} else {
			System.err.println(currentNode + " handleNetworkMessage Manager null for " +
					GroupDescriptor.UNIVERSE);
		}
		
		if (message instanceof TupleNetworkNotification) {
			TupleNetworkNotification notification =
					(TupleNetworkNotification) message;
			String command = notification.getCommand();
			if (TupleNetworkNotification.ADD.equals(command)) {
				GroupDescriptor remoteGroup = (GroupDescriptor)message.getContent();
				GroupCommunicationManager manager = 
						dispatcher.getGroupManagerForHierarchy(remoteGroup.getFriendlyName());
				if (manager != null) {
					manager.handleRemoteGroupDiscovered(remoteGroup);
				} 
			} else if (TupleNetworkNotification.REMOVE.equals(command)){
				// TODO remove tuple from tuplespace
			}
		} 
	}

	private void handleHierarchyMessage(String recipient, TupleMessage message,
			NodeDescriptor sender) {
		GroupCommunicationManager manager = 
				dispatcher.getGroupManagerForHierarchy(recipient);
		if (manager != null) {
			manager.handleAndForwardTupleMessage(message, sender);
		} else {
			System.err.println(currentNode + "Manager null for " + recipient);
		}
	}

	private void handleGroupMessage(GroupDescriptor recipient,
			TupleGroupCommand message, NodeDescriptor sender) {
		GroupCommunicationManager manager = 
				dispatcher.getGroupManagerForDescriptor(recipient);
		if (manager != null) {
			manager.handleAndForwardTupleMessage(message, sender);
		} else {
			System.out.println(currentNode + "Manager null for " + recipient);
		}
	}

	private void handleNodeMessage(NodeDescriptor recipient,
			TupleNodeNotification message, NodeDescriptor sender) {
		dispatcher.handleNodeMessage(message, sender);
		
		// If the message is a NOTIFY_GROUP_EXISTS all the tuples
		// of that group should be forwarded
		if (TupleNodeNotification.NOTIFY_GROUP_EXISTS.equals(message.getCommand())) {
			GroupDescriptor groupDescriptor = (GroupDescriptor) message.getContent();
			ITuple template = createTemplate(Scope.GROUP, null,
					groupDescriptor.getFriendlyName(), null, null, TupleGroupCommand.class);
			try {
				ITuple[] results = tupleSpace.rdg(template);
				if (results != null) {
					for (ITuple tuple: results) {
						TupleGroupCommand gmessage = (TupleGroupCommand)
								((Field)tuple.getFields()[5]).getValue();
						sendToNode(recipient, gmessage);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private void handleNodeMessageAck(NodeDescriptor recipient,
			TupleMessageAck message, NodeDescriptor sender) {
		dispatcher.handleNodeMessageAck(message, sender);
		// TODO remove this and the embedded message
	}
	
	public void storeAndSend(TupleMessage message) {
		if (message.isExpired()) {
			// the tuple is expired
			return;
		}
		System.out.println("storeAndSend " + message.getSubject() + " " + message.getCommand());
		ITuple tuple = createTuple(message.getScope(),
				new Long(message.getExpireTime()), message.getRecipient(),
				message.getSubject(), message.getCommand(), message);
		try {
			tupleSpace.out(tuple);
		} catch (TupleSpaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		send(message);
	}
	
	public void sendWithoutStoring(TupleMessage message) {
		if (message.isExpired()) {
			// the tuple is expired
			return;
		}
		send(message);
	}
	
	private void send(TupleMessage message) {
		System.out.println("XX Sending: " + message);
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
				sendToNetwork(message);
				break;
			}
		}
	}
	

	private void sendToNetwork(TupleMessage message) {
		GroupCommunicationManager manager = 
				dispatcher.getLocalUniverseManager();
		if (manager != null) {
			manager.forwardTupleMessage(message, currentNode);
		} /*else {
			throw new RuntimeException("Null universe");
		}*/
	}

	private void sendToHierarchy(String recipient, TupleMessage message) {
		GroupCommunicationManager manager = 
			dispatcher.getGroupManagerForHierarchy(recipient);
		if (manager != null) {
			manager.forwardTupleMessage(message, currentNode);
		} else {
			System.err.println("Manager is null for hierarchy: " +
					recipient);
		}
	}

	private void sendToGroup(GroupDescriptor recipient, TupleMessage message) {
		GroupCommunicationManager manager = 
				dispatcher.getGroupManagerForDescriptor(recipient);
		if (manager != null) {
			manager.forwardTupleMessage(message, currentNode);
		} else {
			System.err.println("sendToGroup: null manager for " + recipient);
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

	
	private void removeExpiredMessages() {
		//TODO
	}
	

	@Override
	public void notifyPacketArrived(String subject, NodeDescriptor sender,
			Serializable message) {
		storeHandleAndForward((TupleMessage)message, sender);
	}

	/**
	 * Send network tuples to new neighbors
	 */
	@Override
	public void notifyNeighborAdded(NodeDescriptor nodeDescriptor,
			Serializable reconfigurationInfo) {
		ITuple template = createTemplateForNetworkNotification();
		try {
			ITuple[] results = tupleSpace.rdg(template);
			if (results != null) {
				for (ITuple tuple: results) {
					TupleNetworkNotification message = (TupleNetworkNotification)
							((Field)tuple.getFields()[5]).getValue();
					sendToNode(nodeDescriptor, message);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		template = createTemplate(Scope.NODE, null,
				null, null, null, TupleNodeNotification.class);
		try {
			ITuple[] results = tupleSpace.rdg(template);
			if (results != null) {
				for (ITuple tuple: results) {
					TupleNodeNotification message = (TupleNodeNotification)
							((Field)tuple.getFields()[5]).getValue();
					sendToNode(nodeDescriptor, message);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void notifyNeighborDead(NodeDescriptor arg0, Serializable arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void notifyNeighborRemoved(NodeDescriptor arg0) {
		// TODO Auto-generated method stub
	}

	/**
	 * The dispatcher has created a new local group. 
	 */
	void handleNewLocalGroupCreated(GroupCommunicationManager manager) {
		GroupDescriptor groupDescriptor = manager.getGroupDescriptor();
		
		TupleMessage[] messages = getMessages(Scope.GROUP, groupDescriptor);
		for (TupleMessage m: messages) {
			manager.handleAndForwardTupleMessage(m, currentNode);
		}
		messages = getMessages(Scope.HIERARCHY,
				groupDescriptor.getFriendlyName());
		for (TupleMessage m: messages) {
			manager.handleAndForwardTupleMessage(m, currentNode);
		}
	}
		
}
