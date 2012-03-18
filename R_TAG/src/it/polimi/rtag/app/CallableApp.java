/**
 * 
 */
package it.polimi.rtag.app;

import it.polimi.rtag.Node;
import it.polimi.rtag.messaging.TupleMessage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import polimi.reds.NodeDescriptor;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class CallableApp extends AbstractApp {

	private Node currentNode;
	private Map<String, CallableMethod> commands = new HashMap<String, CallableMethod>();
	private PropertyChangeSupport asyncCommandSupport;
	
	public CallableApp() {
		asyncCommandSupport = new PropertyChangeSupport(this);
	}
	
	public CallableMethod put(String name, CallableMethod callable) {
		return commands.put(name, callable);
	}
	
	public CallableMethod remove(String name) {
		CallableMethod callable = commands.remove(name);
		if (callable != null) {
			PropertyChangeListener[] listeners =
					asyncCommandSupport.getPropertyChangeListeners(name);
			for (PropertyChangeListener listener:listeners) {
				asyncCommandSupport.removePropertyChangeListener(name, listener);
			}
		}
		return callable;
	}
	
	public void addPropertyChangeListener(String name,
			PropertyChangeListener listener) {
		if (!commands.containsKey(name)) {
			throw new AssertionError("Command " + name + " not found.");
		}
		asyncCommandSupport.addPropertyChangeListener(name, listener);
	}
	
	public void invokeCommand(NodeDescriptor recipient, String name, Map<String, Serializable> params) {
		if (!commands.containsKey(name)) {
			throw new AssertionError("Command " + name + " not found.");
		}
		CallableMethod command = commands.get(name);
		CallableInvocationMessage message = new CallableInvocationMessage(recipient, params, command);
		currentNode.getTupleSpaceManager().storeAndSend(message);
	}
	
	public void handleCommandInvoked(NodeDescriptor caller, String name, Map<String, Serializable> params) {
		if (!commands.containsKey(name)) {
			throw new AssertionError("Command " + name + " not found.");
		}
		CallableMethod command = commands.get(name);
		Serializable result = command.doCompute(params);
		sendResponse(command, result, caller);
	}

	private void sendResponse(CallableMethod command, Serializable result,
			NodeDescriptor caller) {
		CallableResponseMessage response = new CallableResponseMessage(caller, result, command);
		currentNode.getTupleSpaceManager().storeAndSend(response);
	}
	
	public void handleCommandResponse(CallableResponseMessage response) {
		asyncCommandSupport.firePropertyChange(response.getCommand(), null, response.getContent());
	}

	@Override
	public void handleMessageReceived(NodeDescriptor sender,
			TupleMessage message) {
		if (message instanceof CallableInvocationMessage) {
			CallableInvocationMessage invocation = (CallableInvocationMessage) message;
			handleCommandInvoked(sender, invocation.getCommand(), invocation.getParams());
		} else if (message instanceof CallableResponseMessage) {
			CallableResponseMessage response = (CallableResponseMessage) message;
			handleCommandResponse(response);
		}
		
	}

	
}
