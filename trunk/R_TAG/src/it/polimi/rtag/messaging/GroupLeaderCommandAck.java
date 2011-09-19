/**
 * 
 */
package it.polimi.rtag.messaging;

import polimi.reds.Message;
import polimi.reds.MessageID;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)</p>.
 *
 */
public class GroupLeaderCommandAck extends Message {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5153927215535614233L;
	
	/**
	 * The {@link GroupLeaderCommandAck} message was received correctly 
	 * and the sender fulfilled the command.
	 */
	public static final String OK = "OK";
	
	/**
	 * The message was received but the recipient was not that group follower
	 * or no longer the group follower.</p>
	 * and therefore it is not allowed to perform the required action.
	 */
	public static final String NOT_GROUP_FOLLOWER = "NGF";
	
	/**
	 * The Follower has declined the command. 
	 * In this case the follower should leave the group. 
	 */
	public static final String KO = "KO";
		
	private MessageID originalMessage;
	private String response;
	
	
	public static GroupLeaderCommandAck createKoCommand(MessageID originalMessageID) {
		if (originalMessageID == null) {
			throw new IllegalArgumentException("Message id cannot be null.");
		}
		return new GroupLeaderCommandAck(originalMessageID, KO);
	}
	
	public static GroupLeaderCommandAck createOkCommand(MessageID originalMessageID) {
		if (originalMessageID == null) {
			throw new IllegalArgumentException("Message id cannot be null.");
		}
		return new GroupLeaderCommandAck(originalMessageID, OK);
	}
	
	public static GroupLeaderCommandAck createNotGroupFollowerCommand(MessageID originalMessageID) {
		if (originalMessageID == null) {
			throw new IllegalArgumentException("Message id cannot be null.");
		}
		return new GroupLeaderCommandAck(originalMessageID, NOT_GROUP_FOLLOWER);
	}

	/**
	 * @param originalMessage
	 * @param response
	 */
	public GroupLeaderCommandAck(MessageID originalMessage, String response) {
		createID();
		this.originalMessage = originalMessage;
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

}
