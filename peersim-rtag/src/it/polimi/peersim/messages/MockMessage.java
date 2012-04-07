/**
 * 
 */
package it.polimi.peersim.messages;

import java.io.Serializable;

import peersim.core.Node;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 * Transport message for 1-to-1 communication.
 */
public class MockMessage extends BaseMessage {
	
	private static final long serialVersionUID = -1471858591774512342L;
	
	private final Node sender;
	private final Node receiver;
	
	/**
	 * @param sender
	 * @param receiver
	 * @param content
	 */
	public MockMessage(int pid, Node sender, Node receiver, Serializable content) {
		super(pid, content);
		this.sender = sender;
		this.receiver = receiver;
	}

	public Node getSender() {
		return sender;
	}

	public Node getReceiver() {
		return receiver;
	}
}
