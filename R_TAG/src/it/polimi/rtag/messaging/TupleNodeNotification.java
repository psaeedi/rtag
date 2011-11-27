/**
 * 
 */
package it.polimi.rtag.messaging;

import it.polimi.rtag.GroupDescriptor;

import java.io.Serializable;

import polimi.reds.NodeDescriptor;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class TupleNodeNotification extends TupleMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7984702454020758507L;

	public static final String SUBJECT = "TupleNodeNotification";

	public static final String NOTIFY_GROUP_EXISTS = "NotifyGroupExists";
	
	public static final String JOIN_GROUP = "JoinGroup";

	
	public static TupleNodeNotification createNotifyGroupExistsNotification(
			NodeDescriptor recipient, GroupDescriptor groupDescriptor) {
		return new TupleNodeNotification(recipient, groupDescriptor, NOTIFY_GROUP_EXISTS);
	}

	public static TupleNodeNotification createJoinGroupExistsNotification(
			NodeDescriptor recipient, GroupDescriptor groupDescriptor) {
		return new TupleNodeNotification(recipient, groupDescriptor, JOIN_GROUP);
	}

	
	/**
	 * @param scope
	 * @param recipient
	 * @param content
	 * @param command
	 */
	public TupleNodeNotification(Serializable recipient,
			Serializable content, String command) {
		super(Scope.NODE, recipient, content, command);
	}

	/* (non-Javadoc)
	 * @see it.polimi.rtag.messaging.TupleMessage#getSubject()
	 */
	@Override
	public String getSubject() {
		return SUBJECT;
	}

}
