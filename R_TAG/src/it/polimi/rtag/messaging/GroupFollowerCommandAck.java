/**
 * 
 */
package it.polimi.rtag.messaging;

import polimi.reds.Message;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class GroupFollowerCommandAck extends Message {
	
	
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
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -1605540968698583224L;

}
