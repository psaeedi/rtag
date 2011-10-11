/**
 * 
 */
package it.polimi.rtag.messaging;

import it.polimi.rtag.GroupDescriptor;
import lights.Tuple;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class GroupcastTupleMessage extends TupleMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8793866016280092492L;
	
	GroupDescriptor recipient;

	public GroupcastTupleMessage(GroupDescriptor recipient, Tuple content) {
		super(content);
		this.recipient = recipient;
	}

	public GroupDescriptor getRecipient() {
		return recipient;
	}

	public void setRecipient(GroupDescriptor recipient) {
		this.recipient = recipient;
	}


}
