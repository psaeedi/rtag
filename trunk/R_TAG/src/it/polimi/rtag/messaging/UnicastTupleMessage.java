/**
 * 
 */
package it.polimi.rtag.messaging;

import lights.Tuple;
import polimi.reds.NodeDescriptor;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class UnicastTupleMessage extends TupleMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2727643876694215660L;
	
	NodeDescriptor recipient;

	public UnicastTupleMessage(NodeDescriptor recipient, Tuple content) {
		super(content);
		this.recipient = recipient;
	}

	public NodeDescriptor getRecipient() {
		return recipient;
	}

	public void setRecipient(NodeDescriptor recipient) {
		this.recipient = recipient;
	}

}
