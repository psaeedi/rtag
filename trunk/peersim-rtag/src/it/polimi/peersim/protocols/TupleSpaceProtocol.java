/**
 * 
 */
package it.polimi.peersim.protocols;

import java.util.HashMap;
import java.util.UUID;

import it.polimi.peersim.messages.BaseMessage;
import it.polimi.peersim.messages.TupleSpaceMessage;
import it.polimi.peersim.prtag.UndeliverableMessageException;
import peersim.cdsim.CDProtocol;
import peersim.core.Node;

/**
 * @author Panteha Saeedi
 * 
 * Creates a tuple space as a shared memory for messages.
 *
 * The protocol stack is:
 * 2 - TupleSpaceProtocol
 * 1 - MockChannel
 */
public class TupleSpaceProtocol extends ForwardingProtocol<TupleSpaceMessage>
		implements CDProtocol {

	private HashMap<UUID, TupleSpaceMessage> messages = new HashMap<UUID, TupleSpaceMessage>();
		
	public TupleSpaceProtocol(String prefix) {
		super(prefix);
	}
	
	@Override
	public Object clone() {
		TupleSpaceProtocol clone = null;
		clone = (TupleSpaceProtocol) super.clone();
		clone.messages = new HashMap<UUID, TupleSpaceMessage>(messages);
        return clone;
	}
	
	
	private boolean alreadyIn(TupleSpaceMessage tupleSpaceMessage) {
		return messages.containsKey(tupleSpaceMessage.getInnerUuid());
	}

	@Override
	public void nextCycle(Node currentNode, int pid) {
		clearExpiredMessages();
	}

	private void clearExpiredMessages() {
		for (UUID id: messages.keySet()) {
			TupleSpaceMessage message = messages.get(id);
			if (message.isExpired()) {
				messages.remove(id);
			}
		}
	}

	@Override
	public TupleSpaceMessage handlePushDownMessage(Node currentNode,
			Node recipient, BaseMessage content) {
		return new TupleSpaceMessage(protocolId, content);
	}

	@Override
	public BaseMessage handlePushUpMessage(Node currentNode, Node sender,
			TupleSpaceMessage message) {
		if (alreadyIn(message)) {
			// discard
			return null;
		}
		messages.put(message.getInnerUuid(), message);
		return (BaseMessage) message.getContent();
	}

	@Override
	public void handleUnreliableRecipientException(
			Node currentNode, 
			UndeliverableMessageException ex)
			throws UndeliverableMessageException {
		// Nothing to do.
		// This layer does not raise exceptions
	}

	@Override
	protected void handleForwardedUnreliableRecipientException(
			Node currentNode, 
			UndeliverableMessageException ex)
			throws UndeliverableMessageException {
		TupleSpaceMessage message = (TupleSpaceMessage) ex.getBaseMessage();
		// The message was discarded
		messages.remove(message.getInnerUuid());
		throw new UndeliverableMessageException(ex.getRecipient(),
				(BaseMessage) message.getContent());
	}

}
