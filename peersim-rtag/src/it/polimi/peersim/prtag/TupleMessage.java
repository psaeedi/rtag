package it.polimi.peersim.prtag;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 */

public class TupleMessage implements Serializable {
	
	public enum Scope {
		/** Node tuples are directed to the given node
		 *  Singlecast/direct message.
		 *  
		 *  It uses the universe for delivery. Multiple copies are
		 *  discarded by the tuple space. 
		 */
		NODE,
		/** Group tuples are local to the sender group
		 *  Multicast/Groupcast 
		 */
		GROUP,
		/** 
		 * Hierarchy tuples need to 
		 * be propagated to the whole hierarchy
		 * Multicast/Groupcast 
		 */
		HIERARCHY,
	}
	
	private static final long serialVersionUID = -3141612598067811850L;
	
	private UUID id;
	private Scope scope;
	
	// This is equals to the SUBJECT in reds
	private int protocolId; 
	
	// The node id of the original sender, the one whom created the message
	private long senderId;
	
	// The action
	private String command; 
	
	// The message content
	private Serializable content;
	
	public TupleMessage(Scope scope, int protocolId, long senderId,
			String command, Serializable content) {
		super();
		this.scope = scope;
		this.protocolId = protocolId;
		this.senderId = senderId;
		this.command = command;
		this.content = content;
	}

	public UUID getId() {
		return id;
	}

	public Scope getScope() {
		return scope;
	}

	public int getProtocolId() {
		return protocolId;
	}

	public long getSenderId() {
		return senderId;
	}

	public String getCommand() {
		return command;
	}

	public Serializable getContent() {
		return content;
	}
}
