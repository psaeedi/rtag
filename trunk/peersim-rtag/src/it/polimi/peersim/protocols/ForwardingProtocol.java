/**
 * 
 */
package it.polimi.peersim.protocols;

import java.io.Serializable;

import it.polimi.peersim.messages.BaseMessage;
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
	protected int protocolId;
	
	public static final String LOWER_PROTOCOL_ID = "lower_protocol_id";
	protected int lowerProtocolID;
	
	public ForwardingProtocol(String prefix) {
		protocolId = Configuration.getPid(
				prefix + "." + PROTOCOL_ID);
		lowerProtocolID = Configuration.getPid(
				prefix + "." + LOWER_PROTOCOL_ID);
	}
	
	@Override
	public Object clone() {
		ForwardingProtocol<K> inp = null;
        try {
        	inp = (ForwardingProtocol<K>) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens
        return inp;
	}
	
	public void pushDownMessage(Node currentNode, Node recipient, Serializable content) {
		K message = handlePushDownMessage(currentNode, recipient, content);
		if (message != null) {
			ForwardingProtocol<?> lowerProtocol = (ForwardingProtocol<?>)
					recipient.getProtocol(lowerProtocolID);
			lowerProtocol.pushDownMessage(currentNode, recipient, message);
		}
	}

	public abstract K handlePushDownMessage(Node currentNode, Node recipient, Serializable content);
	
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
