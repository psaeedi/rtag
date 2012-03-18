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
 */
public class CallableResponseMessage extends TupleMessage {

	private static final long serialVersionUID = 6107784983614599253L;

	public CallableResponseMessage(Serializable recipient,
			Serializable results, CallableMethod command) {
		super(Scope.NODE, recipient, results, command.getResponseName());
	}
	
	@Override
	public String getSubject() {
		return CUSTOM_MESSAGE;
	}
	
}
