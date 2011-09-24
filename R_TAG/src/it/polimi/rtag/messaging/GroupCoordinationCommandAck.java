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
public class GroupCoordinationCommandAck extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6000726290090237034L;

	/**
	 * The {@link GroupCoordinationCommandAck} message was received correctly 
	 * and the sender fulfilled the command.
	 */
	public static final String OK = "OK";
	
	/**
	 * The message was received but the recipient was not that group leader
	 * or no longer the group follower.</p>
	 * and therefore it is not allowed to perform the required action.
	 */
	public static final String NOT_GROUP_LEADER = "NGL";
	
	/**
	 * The Follower has declined the command. 
	 * In this case the follower should leave the group. 
	 */
	public static final String KO = "KO";
		
	private MessageID originalMessage;
	private GroupDescriptor groupDescriptor;
	private String response;
	
	
	public static GroupCoordinationCommandAck createKoCommand(
			MessageID originalMessageID, GroupDescriptor groupDescriptor) {
		if (originalMessageID == null) {
			throw new IllegalArgumentException("Message id cannot be null.");
		}
		return new GroupCoordinationCommandAck(
				originalMessageID, groupDescriptor, KO);
	}
	
	public static GroupCoordinationCommandAck createOkCommand(
			MessageID originalMessageID, GroupDescriptor groupDescriptor) {
		if (originalMessageID == null) {
			throw new IllegalArgumentException("Message id cannot be null.");
		}
		return new GroupCoordinationCommandAck(
				originalMessageID, groupDescriptor, OK);
	}
	
	public static GroupCoordinationCommandAck createNotGroupLeaderCommand(
			MessageID originalMessageID, GroupDescriptor groupDescriptor) {
		if (originalMessageID == null) {
			throw new IllegalArgumentException("Message id cannot be null.");
		}
		return new GroupCoordinationCommandAck(
				originalMessageID, groupDescriptor, NOT_GROUP_LEADER);
	}

	/**
	 * @param originalMessage
	 * @param response
	 */
	public GroupCoordinationCommandAck(MessageID originalMessage,
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

}
