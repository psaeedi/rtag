/**
 * 
 */
package it.polimi.rtag;

import java.io.IOException;
import java.io.Serializable;

import polimi.reds.NodeDescriptor;
import polimi.reds.broker.overlay.NotConnectedException;
import polimi.reds.broker.overlay.NotRunningException;
import polimi.reds.broker.overlay.Overlay;
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
public class TupleSpaceManager {

	private TupleSpace tupleSpace = new TupleSpace();

	private Overlay overlay;
	private GroupCommunicationDispatcher dispatcher;
	private NodeDescriptor currentNode;
	
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
	public NodeDescriptor getLeaderForHierarchy(String hierarchyName) {
		ITuple template = new Tuple()
				.add(new Field().setValue(Scope.NETWORK))
				.add(new Field().setType(long.class))
				.add(new Field().setValue(hierarchyName))
				.add(new Field().setValue(TupleNetworkNotification.SUBJECT))
				.add(new Field().setType(NodeDescriptor.class));
		try {
			ITuple tuple = rdp(template);
			if (tuple != null) {
				return (NodeDescriptor)tuple.getFields()[4].getValue(); //TODO this does not work
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
	public void setLeaderForHierarchy(String hierarchyName, NodeDescriptor leader) {
		TupleMessage message = TupleMessage.createNotifyLeaderForHyerarchy(hierarchyName, leader);
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
			out(tuple);
		} catch (TupleSpaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
				overlay.send(TupleMessage.getSubject(), message, recipient);
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
	
	public int count(ITuple tuple) throws TupleSpaceException {
		return tupleSpace.count(tuple);
	}

	public ITuple in(ITuple tuple) throws TupleSpaceException {
		return tupleSpace.in(tuple);
	}

	public ITuple[] ing(ITuple tuple) throws TupleSpaceException {
		return tupleSpace.ing(tuple);
	}

	public ITuple inp(ITuple tuple) throws TupleSpaceException {
		return tupleSpace.inp(tuple);
	}

	public void out(ITuple tuple) throws TupleSpaceException {
		tupleSpace.out(tuple);
	}

	public void outg(ITuple[] tuple) throws TupleSpaceException {
		tupleSpace.outg(tuple);
	}

	public ITuple rd(ITuple tuple) throws TupleSpaceException {
		return tupleSpace.rd(tuple);
	}

	public ITuple[] rdg(ITuple tuple) throws TupleSpaceException {
		return tupleSpace.rdg(tuple);
	}

	public ITuple rdp(ITuple tuple) throws TupleSpaceException {
		return tupleSpace.rdp(tuple);
	}
		
}
