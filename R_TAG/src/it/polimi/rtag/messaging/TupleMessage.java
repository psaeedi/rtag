/**
 * 
 */
package it.polimi.rtag.messaging;

import lights.Tuple;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)</p>.
 * 
 * Applications should extend this with their own messages.
 */
public abstract class TupleMessage extends polimi.reds.Message {

	/**
	 * Messages are valid for 5 minutes.
	 * After that they should not be broadcasted any more. 
	 */
	private final static long EXPIRATION_TIME = 60 * 5 * 1000;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7291797665468391009L;

	private Tuple content;
	private long creationTime;
	
	public TupleMessage(Tuple content) {
		createID();
		this.content = content;
		creationTime = System.currentTimeMillis();
	}

	/**
	 * @return the content
	 */
	public Tuple getContent() {
		return content;
	}
	
	public boolean isExpired() {
		long now = System.currentTimeMillis();
		return now - creationTime > EXPIRATION_TIME;
	}
	
}

