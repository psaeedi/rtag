/**
 * 
 */
package it.polimi.rtag.messaging;

import java.io.Serializable;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class TupleNetworkNotification extends TupleMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3969953716207972579L;

	public static final String SUBJECT = "TupleNetworkNotification";
	
	/**
	 * @param scope
	 * @param recipient
	 */
	public TupleNetworkNotification(Scope scope, Serializable recipient) {
		super(scope, recipient);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see it.polimi.rtag.messaging.TupleMessage#getSubject()
	 */
	@Override
	public String getSubject() {
		return SUBJECT;
	}

}
