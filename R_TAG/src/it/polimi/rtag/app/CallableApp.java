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
	private Map<String, RemoteCallable> remoteCallables = new HashMap<String, RemoteCallable>();
	private Map<String, Command> commands = new HashMap<String, Command>();
	private PropertyChangeSupport asyncCommandSupport;
	private PropertyChangeSupport asyncRespondeSupport;
	private PropertyChangeSupport commandSupport;
	
	public CallableApp() {
		asyncCommandSupport = new PropertyChangeSupport(this);
		asyncRespondeSupport = new PropertyChangeSupport(this);
		commandSupport = new PropertyChangeSupport(this);
	}
	
	public RemoteCallable putRemoteCallable(String name, RemoteCallable callable) {
		// TODO remove param name and use the callable name
		return remoteCallables.put(name, callable);
	}
	
	public Command putCommand(Command command) {
		return commands.put(command.getName(), command);
	}
	
	public RemoteCallable removeRemoteCallable(String name) {
		RemoteCallable callable = remoteCallables.remove(name);
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
	
	public void addCommandListener(String name,
			PropertyChangeListener listener) {
		commandSupport.addPropertyChangeListener(name, listener);
	}
	
	public void invokeRemoteCallable(NodeDescriptor recipient, String name, Map<String, Serializable> params) {
		if (!remoteCallables.containsKey(name)) {
			throw new AssertionError("RemoteCallable " + name + " not found.");
		}
		RemoteCallable command = remoteCallables.get(name);
		CallableInvocationMessage message = new CallableInvocationMessage(recipient, params, command);
		currentNode.getTupleSpaceManager().storeAndSend(message);
	}
	
	public void invokeCommand(NodeDescriptor recipient, String name, Serializable params) {
		if (!commands.containsKey(name)) {
			throw new AssertionError("Command " + name + " not found.");
		}
		Command command = commands.get(name);
		CommandMessage message = new CommandMessage(recipient, params, command);
		currentNode.getTupleSpaceManager().storeAndSend(message);
	}
	
	public void handleRemoteCallableInvoked(NodeDescriptor caller, String name, Map<String, Serializable> params) {
		if (!remoteCallables.containsKey(name)) {
			throw new AssertionError("Command " + name + " not found.");
		}
		RemoteCallable command = remoteCallables.get(name);
		asyncCommandSupport.firePropertyChange(command.getName(), null, params);
		Serializable result = command.doCompute(params, currentNode);
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
	
	public void handleCommandMessage(CommandMessage message) {
		commandSupport.firePropertyChange(message.getCommand(), null, message.getContent());
	}

	@Override
	public void handleMessageReceived(NodeDescriptor sender,
			TupleMessage message) {
		if (message instanceof CallableInvocationMessage) {
			CallableInvocationMessage invocation = (CallableInvocationMessage) message;
			handleRemoteCallableInvoked(sender, invocation.getCommand(), invocation.getParams());
		} else if (message instanceof CallableResponseMessage) {
			CallableResponseMessage response = (CallableResponseMessage) message;
			handleCommandResponse(response);
		} else if (message instanceof CommandMessage) {
			CommandMessage command = (CommandMessage) message;
			handleCommandMessage(command);
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
