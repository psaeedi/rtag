/**
 * 
 */
package it.polimi.peersim.prtag;

import it.polimi.peersim.messages.BaseMessage;
import peersim.core.Node;

/**
 * @author Panteha Saeedi
 *
 */
public class UndeliverableMessageException extends Exception {
	
	private static final long serialVersionUID = -7415418500757969872L;
	
	private final Node recipient;
	private final BaseMessage baseMessage;
	
	public UndeliverableMessageException(Node recipient, BaseMessage baseMessage) {
		super();
		this.recipient = recipient;
		this.baseMessage = baseMessage;
	}

	public Node getRecipient() {
		return recipient;
	}

	public BaseMessage getBaseMessage() {
		return baseMessage;
	}
}
