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
 * implementing both the client and server1
 */
public class CallableApp extends AbstractApp {

	private Node currentNode;
	private Map<String, RemoteCallable> commands = new HashMap<String, RemoteCallable>();
	private PropertyChangeSupport asyncCommandSupport;
	private PropertyChangeSupport asyncRespondeSupport;
	
	public CallableApp() {
		asyncCommandSupport = new PropertyChangeSupport(this);
		asyncRespondeSupport = new PropertyChangeSupport(this);
	}
	
	public RemoteCallable put(String name, RemoteCallable callable) {
		return commands.put(name, callable);
	}
	
	public RemoteCallable remove(String name) {
		RemoteCallable callable = commands.remove(name);
		if (callable != null) {
			PropertyChangeListener[] listeners =
					asyncCommandSupport.getPropertyChangeListeners(name);
			for (PropertyChangeListener listener:listeners) {
				asyncCommandSupport.removePropertyChangeListener(name, listener);
			}
			listeners =
					asyncRespondeSupport.getPropertyChangeListeners(callable.getResponseName());
			for (PropertyChangeListener listener:listeners) {
				asyncRespondeSupport.removePropertyChangeListener(callable.getResponseName(), listener);
			}
		}
		return callable;
	}
	
	public void addCallListener(String name,
			PropertyChangeListener listener) {
		asyncCommandSupport.addPropertyChangeListener(name, listener);
	}
	
	public void addResponseListener(String name,
			PropertyChangeListener listener) {
		asyncRespondeSupport.addPropertyChangeListener(name, listener);
	}
	
	public void invokeCommand(NodeDescriptor recipient, String name, Map<String, Serializable> params) {
		if (!commands.containsKey(name)) {
			throw new AssertionError("Command " + name + " not found.");
		}
		RemoteCallable command = commands.get(name);
		CallableInvocationMessage message = new CallableInvocationMessage(recipient, params, command);
		currentNode.getTupleSpaceManager().storeAndSend(message);
	}
	
	public void handleCommandInvoked(NodeDescriptor caller, String name, Map<String, Serializable> params) {
		if (!commands.containsKey(name)) {
			throw new AssertionError("Command " + name + " not found.");
		}
		RemoteCallable command = commands.get(name);
		asyncCommandSupport.firePropertyChange(command.getName(), null, params);
		Serializable result = command.doCompute(params);
		sendResponse(command, result, caller);
	}

	private void sendResponse(RemoteCallable command, Serializable result,
			NodeDescriptor caller) {
		CallableResponseMessage response = new CallableResponseMessage(caller, result, command);
		currentNode.getTupleSpaceManager().storeAndSend(response);
	}
	
	public void handleCommandResponse(CallableResponseMessage response) {
		asyncRespondeSupport.firePropertyChange(response.getCommand(), null, response.getContent());
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

	public Node getCurrentNode() {
		return currentNode;
	}

	public void setCurrentNode(Node currentNode) {
		this.currentNode = currentNode;
		this.currentNode.getTupleSpaceManager().setApplication(this);
	}

	
}
