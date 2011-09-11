/**
 * 
 */
package it.polimi.rtag.messaging;

import polimi.reds.Message;
import polimi.reds.MessageID;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class JoinGroupResponse extends Message {

	/**
	 * The {@link JoinGroupRequest} message was received correctly 
	 * and the sender was added to the group.
	 */
	public static final String OK = "OK";
	
	/**
	 * The message was received but the recipient was not a group leader
	 * and therefore it is not allowed to perform the required action.
	 */
	public static final String NOT_GROUP_LEADER = "NGL";
	
	/**
	 * The leader has declined the join request. This can happen for
	 * various reason, most likely because the topology of the
	 * group is changing.
	 * 
	 * The sender should wait and retry. 
	 */
	public static final String KO = "KO";
	

	/**
	 * Factory method to create an {@link JoinGroupResponse#NOT_GROUP_LEADER} message
	 * 
	 * @param originalMessageID
	 * @return
	 */
	public static JoinGroupResponse createNotGroupLeaderResponse(MessageID originalMessageID) {
		return createJoinGroupResponse(originalMessageID, NOT_GROUP_LEADER);
	}
	
	/**
	 * Factory method to create an {@link JoinGroupResponse#KO} message
	 * 
	 * @param originalMessageID
	 * @return
	 */
	public static JoinGroupResponse createKoResponse(MessageID originalMessageID) {
		return createJoinGroupResponse(originalMessageID, KO);
	}
	
	/**
	 * Factory method to create an {@link JoinGroupResponse#OK} message
	 * 
	 * @param originalMessageID
	 * @return
	 */
	public static JoinGroupResponse createOkResponse(MessageID originalMessageID) {
		return createJoinGroupResponse(originalMessageID, OK);
	}
	
	/**
	 * Inner implementation of the factory methods.
	 * 
	 * @param originalMessageID the original message id
	 * @param response the response status
	 * @return the new instance
	 */
	private  static JoinGroupResponse createJoinGroupResponse(MessageID originalMessageID, String response) {
		if (originalMessageID != null) {
			return new JoinGroupResponse(originalMessageID, response);
		} else {
			throw new NullPointerException(
					"Original message ID canno be null");
		}
	}
	
	private MessageID originalMessageID;
	private String responseCode;
	
	/**
	 * @param originalMessageID
	 * @param responseCode
	 */
	private JoinGroupResponse(MessageID originalMessageID, String responseCode) {
		this.originalMessageID = originalMessageID;
		this.responseCode = responseCode;
	}

	/**
	 * @return the originalMessageID
	 */
	public MessageID getOriginalMessageID() {
		return originalMessageID;
	}

	/**
	 * @return the responseCode
	 */
	public String getResponseCode() {
		return responseCode;
	}
	

}
