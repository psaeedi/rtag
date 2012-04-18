/**
 * 
 */
package it.polimi.peersim.protocols;

import java.io.Serializable;

import it.polimi.peersim.messages.BaseMessage;
import it.polimi.peersim.prtag.UndeliverableMessageException;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.core.Protocol;

/**
 * @author Panteha Saeedi
 *
 * A protocol that can both handle a message and forward
 * it to upper layer protocols.
 */
public abstract class ForwardingProtocol<K extends BaseMessage> implements Protocol {

	public static final String PROTOCOL_ID = "protocol_id";
	protected final int protocolId;
	
	public static final String LOWER_PROTOCOL_ID = "lower_protocol_id";
	protected final int lowerProtocolID;
	
	public ForwardingProtocol(String prefix) {
		protocolId = Configuration.getPid(
				prefix + "." + PROTOCOL_ID);
		lowerProtocolID = Configuration.getPid(
				prefix + "." + LOWER_PROTOCOL_ID);
	}
	
	@Override
	public Object clone() {
		ForwardingProtocol<K> clone = null;
        try {
        	clone = (ForwardingProtocol<K>) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens
        return clone;
	}
	
	public void pushDownMessage(Node currentNode, Node recipient, BaseMessage content)
			throws UndeliverableMessageException {
		
		K message;
		try {
			message = handlePushDownMessage(currentNode, recipient, content);
		} catch(UndeliverableMessageException ex) {
			handleUnreliableRecipientException(currentNode, ex);
			return;
		}
		
		try {
			if (message != null) {
				ForwardingProtocol<?> lowerProtocol = (ForwardingProtocol<?>)
						recipient.getProtocol(lowerProtocolID);
				if (this.getClass().equals(lowerProtocol.getClass())) {
					throw new AssertionError("Loop: " + this);
				}
				lowerProtocol.pushDownMessage(currentNode, recipient, message);
			}
		} catch(UndeliverableMessageException ex) {
			handleForwardedUnreliableRecipientException(currentNode, ex);
			return;
		}
	}
	

	

	protected abstract void handleUnreliableRecipientException(
			Node currentNode, UndeliverableMessageException ex)
					throws UndeliverableMessageException;
 
	protected abstract void handleForwardedUnreliableRecipientException(
			Node currentNode, UndeliverableMessageException ex)
					throws UndeliverableMessageException;

	
	/**
	 * @param currentNode
	 * @param recipient
	 * @param content
	 * @return A wrapped BaseMessage or <code>null</code> if the message has 
	 * 		not to be propagated, e.g. if the message was intended for this
	 * 		layer.
	 * @throws UndeliverableMessageException if the message cannot be propagated.
	 */
	public abstract K handlePushDownMessage(Node currentNode, Node recipient, BaseMessage content)
			throws UndeliverableMessageException;
	
	public void receiveAndPushUpMessage(Node currentNode, Node sender, K message) {
		BaseMessage content = handlePushUpMessage(currentNode, sender, message);
		if (content != null) {
			ForwardingProtocol wrappedProtocol = (ForwardingProtocol)
					currentNode.getProtocol(content.getPid());
			wrappedProtocol.receiveAndPushUpMessage(currentNode, sender, content);
		}
	}
	
	public abstract BaseMessage handlePushUpMessage(Node currentNode, Node sender, K message);
}
