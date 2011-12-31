/**
 * 
 */
package it.polimi.rtag.messaging;

import java.io.Serializable;

import lights.interfaces.ITuple;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)</p>.
 * 
 * Applications should extend this with their own messages.
 */
public abstract class TupleMessage extends polimi.reds.Message {

	/**
	 * Defines the scope of this message
	 */
	public enum Scope {
		/** Node tuples are directed to the given node */
		NODE,
		/** Group tuples are local to the sender group */
		GROUP,
		/** 
		 * Hierarchy tuples need to 
		 * be propagated to the whole hierarchy */
		HIERARCHY,
		/** 
		 * Network tuples are spread over the whole network 
		 * using the universe group*/
		NETWORK
	}
	
	/**
	 * Messages are valid for 5 minutes.
	 * After that they should not be broadcasted any more. 
	 */
	private final static long EXPIRATION_TIME = 60 * 60 * 1000;
	
	public static final String CUSTOM_MESSAGE = "CustomMessage";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7291797665468391009L;

	private long creationTime;
	private Scope scope;
	private Serializable recipient;
	private Serializable content;
	private String command;

	public TupleMessage(Scope scope, Serializable recipient,
			Serializable content, String command) {
		createID();
		creationTime = System.currentTimeMillis();
		this.scope = scope;
		this.recipient = recipient;
		this.content = content;
		this.command = command;
	}

	public boolean isExpired() {
		long now = System.currentTimeMillis();
		return now - creationTime > EXPIRATION_TIME;
	}

	public long getExpireTime() {
		return creationTime + EXPIRATION_TIME;
	}
	
	public Scope getScope() {
		return scope;
	}

	public Serializable getRecipient() {
		return recipient;
	}
	
	public Serializable getContent() {
		return content;
	}

	public String getCommand() {
		return command;
	}
	
	public abstract String getSubject();

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TupleMessage) {
			TupleMessage message = (TupleMessage)obj;
			return this.id.equals(message.id);
		}
		return super.equals(obj);
	}
}
