/**
 * 
 */
package it.polimi.rtag.messaging;

import it.polimi.rtag.filters.TupleFilter;
import lights.Tuple;

/**
 * @author panteha
 * 
 * 
 */
public class TupleMessage extends polimi.reds.Message {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7291797665468391009L;

	private TupleFilter filter;
	private Tuple content;
	
	public TupleMessage(TupleFilter filter, Tuple content) {
		super();
		this.filter = filter;
		this.content = content;
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
	
}

