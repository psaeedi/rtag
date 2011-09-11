/**
 * 
 */
package it.polimi.rtag.messaging;

import polimi.reds.Message;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class GroupLeaderCommandAck extends Message {
	
	
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
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 5153927215535614233L;
	
	// TODO implement this as Ack
	
	//

}
