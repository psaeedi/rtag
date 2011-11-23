/**
 * 
 */
package it.polimi.rtag.messaging;

import java.io.Serializable;

import polimi.reds.MessageID;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class TupleGroupCommandAck extends TupleMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8703656652831571979L;

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
	
	public static final String SUBJECT = "TupleGroupCommandAck";
	
	private String response;

	public TupleGroupCommandAck(Scope scope, Serializable recipient,
			TupleGroupCommand originalMessage) {
		super(scope, recipient, originalMessage);
	}
	
	public String getSubject() {
		return SUBJECT;
	}
	
	public TupleGroupCommand getOriginalMessage() {
		return (TupleGroupCommand) getContent();
	}
	
	public String getResponse() {
		return response;
	}

}
