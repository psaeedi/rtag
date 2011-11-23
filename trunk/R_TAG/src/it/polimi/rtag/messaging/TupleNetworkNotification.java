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

	public TupleNetworkNotification(Scope scope, Serializable recipient,
			Serializable content) {
		super(scope, recipient, content);
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
