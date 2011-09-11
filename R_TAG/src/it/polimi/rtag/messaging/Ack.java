/**
 * 
 */
package it.polimi.rtag.messaging;

import polimi.reds.Message;
import polimi.reds.MessageID;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 * Defines the content of an acknowledge message sent to notify the reception of a communication.
 * @see MessageSubjects#ACK
 */
public class Ack extends Message {

	/**
	 * The message was received and the recipient was correct.
	 */
	public static final String OK = "OK";
	
	/**
	 * The message was received but the recipient was not a group leader
	 * and therefore it is not allowed to forward the message.</p>
	 * 
	 * This happens when sending an anycast or a multicast.
	 */
	public static final String NOT_GROUP_LEADER = "NGL";
	
	/**
	 * The message was received correctly but the recipient is no
	 * longer in that group. 
	 */
	public static final String NOT_GROUP_FOLLOWER = "NGF";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 328363290154139772L;
	private MessageID originalMessageID;
	private String response;
	
	/**
	 * Factory method to create an {@link Ack#OK} message
	 * 
	 * @param originalMessageID
	 * @return
	 */
	public static Ack createOkAck(MessageID originalMessageID) {
		return createAck(originalMessageID, OK);
	}

	/**
	 * Factory method to create an {@link Ack#NOT_GROUP_LEADER} message
	 * 
	 * @param originalMessageID
	 * @return
	 */
	public static Ack createNotGroupLeaderAck(MessageID originalMessageID) {
		return createAck(originalMessageID, NOT_GROUP_LEADER);
	}
	
	/**
	 * Factory method to create an {@link Ack#NOT_GROUP_FOLLOWER} message
	 * 
	 * @param originalMessageID
	 * @return
	 */
	public static Ack createNotGroupFollowerAck(MessageID originalMessageID) {
		return createAck(originalMessageID, NOT_GROUP_FOLLOWER);
	}
		
	/**
	 * Inner implementation of the factory methods.
	 * 
	 * @param originalMessageID the original message id
	 * @param response the response status
	 * @return the new instance
	 */
	private  static Ack createAck(MessageID originalMessageID, String response) {
		if (originalMessageID != null) {
			return new Ack(originalMessageID, response);
		} else {
			throw new NullPointerException(
					"Original message ID canno be null");
		}
		
	}

	
	/**
	 * Creates an acknowledgment message to notify the
	 * reception of the given message.
	 */
	private Ack(MessageID originalMessageID, String response) {
		this.originalMessageID = originalMessageID;
		this.response = response;
	}

	/**
	 * @return the messageID
	 */
	public MessageID getOriginalMessageID() {
		return originalMessageID;
	}

	/**
	 * @return the response
	 */
	public String getResponse() {
		return response;
	}

}
