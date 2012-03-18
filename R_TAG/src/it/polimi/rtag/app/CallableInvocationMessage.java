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
public class CallableInvocationMessage extends TupleMessage {

	private static final long serialVersionUID = 6107784983614599253L;

	public CallableInvocationMessage(Serializable recipient,
			Map<String, Serializable> params, CallableMethod command) {
		super(Scope.NODE, recipient, (Serializable) params, command.getName());
	}
	
	@Override
	public String getSubject() {
		return CUSTOM_MESSAGE;
	}
	
	public Map<String, Serializable> getParams() {
		return (Map<String, Serializable>) getContent();
	}
}
