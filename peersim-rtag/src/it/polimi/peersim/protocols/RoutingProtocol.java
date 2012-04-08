/**
 * 
 */
package it.polimi.peersim.protocols;

import it.polimi.peersim.messages.BaseMessage;
import it.polimi.peersim.messages.RoutingMessage;
import it.polimi.peersim.prtag.LocalUniverseDescriptor;
import it.polimi.peersim.prtag.RoutingPath;
import it.polimi.peersim.prtag.UndeliverableMessageException;

import com.google.common.collect.HashMultimap;
import peersim.core.Node;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 * The protocol stack is:
 * 4 - Routing
 * 3 - UniverseProtocol
 * 2 - TupleSpaceProtocol
 * 1 - MockChannel
 */
public class RoutingProtocol extends ForwardingProtocol<RoutingMessage> {
	
	HashMultimap<Node, RoutingPath> routingTable = HashMultimap.create();

	public RoutingProtocol(String prefix) {
		super(prefix);
	}

	@Override
	public Object clone() {
		RoutingProtocol clone = null;
		clone = (RoutingProtocol) super.clone();
		clone.routingTable = HashMultimap.create(routingTable);
        return clone;
	}
	
	public void removeExpiredPath(){
		//TODO remove all the expired entry from the table
	}
	
	public void removeLostPath(Node lostNode){
		// remove all the lost entry from the table
		this.routingTable.removeAll(lostNode);
		// TODO remove all the entries with that node as a source
	}
	
	protected void addPath(LocalUniverseDescriptor localUniverse) {
		// TODO it creates the path for all 
		// the followers of that descriptor
		for (Node follower: localUniverse.getFollowers()){
			 RoutingPath routingpath = new RoutingPath(follower, 
					 localUniverse.getLeader());
			 routingTable.put(follower, routingpath);
		}
	}

	@Override
	public RoutingMessage handlePushDownMessage(Node currentNode,
			Node recipient, BaseMessage content) throws UndeliverableMessageException {
		Node proxy = getMostSuitableProxy(currentNode, recipient);
		if (proxy == null) {
			throw new UndeliverableMessageException(recipient, content);
		}
		return new RoutingMessage(protocolId, recipient, recipient, content);
	}
	
	protected Node getMostSuitableProxy(Node currentNode,
			Node recipient) {
		// Mock implementation using the recipient as a proxy
		// TODO implement this method
		return recipient;
	}

	@Override
	public BaseMessage handlePushUpMessage(Node currentNode, Node sender,
			RoutingMessage message) {
		if (currentNode.equals(message.getRecipient())) {
			return (BaseMessage) message.getContent();
		} else {
			// Forward the message
			try {
				pushDownMessage(
						currentNode, message.getRecipient(), (BaseMessage) message.getContent());
			} catch(UndeliverableMessageException ex) {
				handleForwardedUnreliableRecipientException(currentNode, ex);
			}
			return null;
		}
	}

	/**
	 * This node could not find a suitable proxy.
	 * The previous proxy should be notified of the
	 * problem and should re-send the message. 
	 */
	@Override
	protected void handleUnreliableRecipientException(
			Node currentNode, UndeliverableMessageException ex) {
		// TODO no proxy found.
		// Candel delivery and inform sender
	}

	/**
	 * The lower layer has failed to submit the message.
	 * This means that the used route is no longer valid.
	 * 
	 *  The route should be removed and the message 
	 *  should be re-sent.
	 */
	@Override
	protected void handleForwardedUnreliableRecipientException(
			Node currentNode,
			UndeliverableMessageException ex) {
		// TODO Auto-generated method stub
		RoutingMessage message = (RoutingMessage) ex.getBaseMessage();
		Node recipient = message.getRecipient();
		RoutingPath brokenPath = null;
		for(RoutingPath path: routingTable.get(recipient)) {
			if (path.getProxy().equals(message.getProxy())) {
				brokenPath = path;
				break;
			}
		}
		routingTable.remove(message.getRecipient(), brokenPath);
		try {
			pushDownMessage(currentNode, recipient, (BaseMessage) message.getContent());
		} catch(UndeliverableMessageException exc) {
			handleForwardedUnreliableRecipientException(currentNode, exc);
		}
	}

}
