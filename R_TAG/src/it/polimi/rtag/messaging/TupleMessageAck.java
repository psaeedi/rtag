/**
 * 
 */
package it.polimi.rtag.messaging;

import it.polimi.rtag.GroupDescriptor;

import java.io.Serializable;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class TupleMessageAck extends TupleMessage {

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
	
	
	public static TupleMessageAck createOkAck(Scope scope, Serializable recipient,
			TupleMessage originalMessage) {
		return new TupleMessageAck(scope, recipient, originalMessage, OK);
	}
	
	public static TupleMessageAck createKoAck(Scope scope, Serializable recipient,
			TupleMessage originalMessage) {
		return new TupleMessageAck(scope, recipient, originalMessage, KO);
	}
	
	protected TupleMessageAck(Scope scope, Serializable recipient,
			TupleMessage originalMessage, String command) {
		super(scope, recipient, originalMessage, command);
	}
	
	public String getSubject() {
		return SUBJECT;
	}
	
	public TupleMessage getOriginalMessage() {
		return (TupleMessage) getContent();
	}
	
}
