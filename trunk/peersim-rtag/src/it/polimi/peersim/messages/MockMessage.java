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
	
	private static final long serialVersionUID = 4844188023821045727L;
	private final Node sender;
	private final Node receiver;
	
	/**
	 * @param sender
	 * @param receiver
	 * @param content
	 */
	public MockMessage(int pid, Node sender, Node receiver, Serializable content) {
		super(pid, content);
		if (sender.getID() == receiver.getID()) {
			throw new AssertionError("Sender and receiver cannot be the same node");
		}
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
