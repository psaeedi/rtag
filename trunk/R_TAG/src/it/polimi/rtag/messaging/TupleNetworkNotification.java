/**
 * 
 */
package it.polimi.rtag.messaging;

import it.polimi.rtag.GroupDescriptor;

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

	public static final String ADD = "Add";
	public static final String REMOVE = "Remove";
	
	public TupleNetworkNotification(
			GroupDescriptor groupDescriptor, String command) {
		super(Scope.NETWORK, groupDescriptor.getFriendlyName(), groupDescriptor, command);
	}

	/* (non-Javadoc)
	 * @see it.polimi.rtag.messaging.TupleMessage#getSubject()
	 */
	@Override
	public String getSubject() {
		return SUBJECT;
	}
	
	public GroupDescriptor getGroupDescriptor() {
		return (GroupDescriptor) getContent();
	}

}
