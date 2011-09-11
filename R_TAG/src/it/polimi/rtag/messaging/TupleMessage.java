/**
 * 
 */
package it.polimi.rtag.messaging;

import java.util.Date;

import it.polimi.rtag.filters.TupleFilter;
import lights.Tuple;

/**
 * @author panteha
 * 
 * 
 */
public class TupleMessage extends polimi.reds.Message {

	/**
	 * Messages are valid for 5 minutes.
	 * After that they should not be broadcasted any more. 
	 */
	private final static long EXPIRATION_TIME = 60 * 5 * 1000;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7291797665468391009L;

	private TupleFilter filter;
	private Tuple content;
	private long creationTime;
	
	public TupleMessage(TupleFilter filter, Tuple content) {
		this.filter = filter;
		this.content = content;
		creationTime = System.currentTimeMillis();
	}

	/**
	 * @return the filter
	 */
	public TupleFilter getFilter() {
		return filter;
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

