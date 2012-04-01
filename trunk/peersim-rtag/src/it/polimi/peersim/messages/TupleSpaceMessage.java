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
	
	private final UUID uuid = UUID.randomUUID(); 
	// TODO set the expire time according to the cycle duration
	private final long expireTime = CDState.getIntTime() + 20 * 60 * 1000;

	/**
	 * @param content
	 */
	public TupleSpaceMessage(int pid, Serializable content) {
		super(pid, content);
	}

	public UUID getUuid() {
		return uuid;
	}

	public boolean isExpired() {
		return CDState.getIntTime() > expireTime;
	};
}
