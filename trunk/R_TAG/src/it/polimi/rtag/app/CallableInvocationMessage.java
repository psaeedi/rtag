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
public class CallableInvocationMessage extends TupleMessage {

	private static final long serialVersionUID = 6107784983614599253L;

	public CallableInvocationMessage(Serializable recipient,
			Map<String, Serializable> params, RemoteCallable command) {
		super(Scope.NODE, recipient, (Serializable) params, command.getName());
	}
	
	
	// a message has a subject, command and content
	// this is for all messages
	
	@Override
	public String getSubject() {
		return CUSTOM_MESSAGE;
	}
	
	public Map<String, Serializable> getParams() {
		return (Map<String, Serializable>) getContent();
	}
}
