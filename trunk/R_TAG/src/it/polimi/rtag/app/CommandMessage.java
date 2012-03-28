/**
 * 
 */
package it.polimi.rtag.app;

import java.io.Serializable;
import java.util.Map;

import it.polimi.rtag.messaging.TupleMessage;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 * the node who wants to invoke a remote method
 */
public class CommandMessage extends TupleMessage {

	private static final long serialVersionUID = -3825697753446047131L;

	public CommandMessage(Serializable recipient,
			Serializable params, Command command) {
		super(Scope.NODE, recipient, params, command.getName());
	}
	
	
	// a message has a subject, command and content
	// this is for all messages
	
	@Override
	public String getSubject() {
		return CUSTOM_MESSAGE;
	}
	
}
