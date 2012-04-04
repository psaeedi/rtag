package it.polimi.rtag.app.operator;

import it.polimi.rtag.app.CallableApp;
import it.polimi.rtag.app.Command;

public class AppCommands extends CallableApp {
	
	/*
	 * extend this class to add more commands
	 * commands are used to send a 
	 **/
	
	
	public Command joinAppGroup = new Command("joinAppGroup");
	public Command leaveAppGroup = new Command("leaveAppGroup");
	public Command activateMaster = new Command("activateMaster");
	public Command activateSlave = new Command("activateSlave");
	
	/*private Serializable recipient;
	private Map<String, Serializable> params;
	private RemoteCallable command;
	public CallableInvocationMessage deliverMessage =
			new CallableInvocationMessage(recipient, params, command);
	
	private Serializable caller;
	private Serializable result;
	public CallableResponseMessage responseMessage = 
			new CallableResponseMessage(caller, result, command);*/
	
	
	/**
	 * 
	 */
	public AppCommands() {
		putCommand(joinAppGroup);
		putCommand(leaveAppGroup);
		putCommand(activateMaster);
		putCommand(activateSlave);
	
		
	}

	public void sendCommand(Command command, AppNode recipient, String groupFriendlyName ) {
	             invokeCommand(recipient.getNodeDescriptor(),  
	            		 command.getName(),  groupFriendlyName);
	}

	
	
}
