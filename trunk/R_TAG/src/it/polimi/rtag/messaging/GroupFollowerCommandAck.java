/**
 * 
 */
package it.polimi.rtag.messaging;

import it.polimi.rtag.GroupDescriptor;
import polimi.reds.Message;
import polimi.reds.MessageID;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)</p>.
 *
 */
public class GroupFollowerCommandAck extends Message {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1605540968698583224L;
	
	/**
	 * The {@link GroupLeaderCommandAck} message was received correctly 
	 * and the sender fulfilled the command.
	 */
	public static final String OK = "OK";
	

	/**
	 * The Follower has declined the command. 
	 * In this case the follower should leave the group. 
	 */
	public static final String KO = "KO";
	
	private MessageID originalMessage;
	private String response;
	private GroupDescriptor groupDescriptor;
	
	
	public static GroupFollowerCommandAck createKoCommand(
			MessageID originalMessageID,
			GroupDescriptor groupDescriptor) {
		if (originalMessageID == null) {
			throw new IllegalArgumentException("Message id cannot be null.");
		}
		return new GroupFollowerCommandAck(
				originalMessageID, groupDescriptor, KO);
	}
	
	public static GroupFollowerCommandAck createOkCommand(
			MessageID originalMessageID,
			GroupDescriptor groupDescriptor) {
		if (originalMessageID == null) {
			throw new IllegalArgumentException("Message id cannot be null.");
		}
		return new GroupFollowerCommandAck(
				originalMessageID, groupDescriptor, OK);
	}

	/**
	 * @param originalMessage
	 * @param response
	 */
	public GroupFollowerCommandAck(MessageID originalMessage,
			GroupDescriptor groupDescriptor, String response) {
		createID();
		this.originalMessage = originalMessage;
		this.groupDescriptor = groupDescriptor;
		this.response = response;
	}


	/**
	 * @return the originalMessage
	 */
	public MessageID getOriginalMessage() {
		return originalMessage;
	}


	/**
	 * @return the response
	 */
	public String getResponse() {
		return response;
	}

	public GroupDescriptor getGroupDescriptor() {
		return groupDescriptor;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GroupFollowerCommandAck [originalMessage=" + originalMessage
				+ ", response=" + response + ", groupDescriptor="
				+ groupDescriptor.getUniqueId() + "]";
	}
	

}
