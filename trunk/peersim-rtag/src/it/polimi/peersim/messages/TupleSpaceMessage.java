/**
 * 
 */
package it.polimi.peersim.messages;

import java.io.Serializable;
import java.util.UUID;

import peersim.cdsim.CDState;

/**
 * @author Panteha Saeedi
 *
 *	Represents a message which can be stored in a tuple space.
 */
public class TupleSpaceMessage extends BaseMessage {
	
	private static final long serialVersionUID = -6941942310691098994L;
	
	private UUID innerUuid;
	
	// TODO set the expire time according to the cycle duration
	private final long expireTime = CDState.getIntTime() + 20 * 60 * 1000;

	/**
	 * @param content
	 */
	public TupleSpaceMessage(int pid, Serializable content) {
		super(pid, content);
		
		innerUuid = getUuid();
		Serializable innerContent = content;
		while (innerContent instanceof BaseMessage) {
			BaseMessage innerMessage = (BaseMessage) innerContent;
			innerUuid = innerMessage.getUuid();
			innerContent = innerMessage.getContent();
		}
	}

	public boolean isExpired() {
		return CDState.getIntTime() > expireTime;
	}

	public UUID getInnerUuid() {
		return innerUuid;
	};
}
