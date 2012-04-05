package it.polimi.rtag.app.operator;

import java.util.HashMap;

import it.polimi.rtag.GroupDescriptor;
import it.polimi.rtag.Node;
import it.polimi.rtag.app.CallableApp;
import it.polimi.rtag.app.Command;

/**
 * author Panteha Saeedi@elet.polimi.it
 * extend this class to add more commands
 * commands are used to send a message that does not need a respond
 * 
 * for each command we need to add a listener for the nodes
 */
public class AppCommand extends CallableApp {
	
	public Command joinAppGroup = new Command("joinAppGroup");
	public Command leaveAppGroup = new Command("leaveAppGroup");
	public Command activateMaster = new Command("activateMaster");
	public Command activateSlave = new Command("activateSlave");
	public Command electMaster = new Command("electmaster");
	
	HashMap<String, Node> masterofexistingGroups = new HashMap<String, Node>();
	HashMap<String, Node> memebrofexistingGroups = new HashMap<String, Node>();
		
	private MasterBehavior masterBehaviors; 
	private SlaveBehavior slaveBehaviors; 
	
	/**
	 * 
	 */
	public AppCommand() {
		putCommand(joinAppGroup);
		putCommand(leaveAppGroup);
		putCommand(activateMaster);
		putCommand(activateSlave);	
		putCommand(electMaster);	
	}

	
	//-----------------------
	public GroupDescriptor joinGroup(String groupFriendlyName){
		memebrofexistingGroups.put(groupFriendlyName, this.getCurrentNode());
		return joinGroup(groupFriendlyName);
	}
	
	public Node getMaster(String friendlyName) {
		Node masternode = masterofexistingGroups.get(friendlyName);
		return masternode;
	}

	
	public void setMaster(String friendlyName) {
		masterofexistingGroups.put(friendlyName, this.getCurrentNode());
		// TODO inform the network?
	}
	
	
    public void setBehaviors(
    		String groupFriendlyName,
    		boolean activemaster, 
    		boolean activeslave) {
    	
    	if (activemaster == true) {
    		masterBehaviors.setBehavior();
    	} else if(isMaster(groupFriendlyName)) {
    		throw new RuntimeException("the node is set to be a master " +
    				"but its behavior is deactivated" );
    	}
    	
    	if (activeslave == true){
    		slaveBehaviors.setBehavior();
    	}
    	
    	else if (!isMaster(groupFriendlyName)){
    		throw new RuntimeException("the node is set to be a slave " +
    				"but its behavior is deactivated" );
    	}
	}

	private boolean isMaster(String groupFriendlyName) {
		Node master =  getMaster(groupFriendlyName);
		if(this.getCurrentNode().equals(master)){
		   return true;
		}
		return false;
	}
	
	public int getNumberofSlaves(String groupFriendlyName){
		return memebrofexistingGroups.size()-1;
	}

	public MasterBehavior getMasterBehaviors() {
		return masterBehaviors;
	}

	public void setMasterBehaviors(MasterBehavior masterBehaviors) {
		this.masterBehaviors = masterBehaviors;
	}

	public SlaveBehavior getSlaveBehaviors() {
		return slaveBehaviors;
	}

	public void setSlaveBehaviors(SlaveBehavior slaveBehaviors) {
		this.slaveBehaviors = slaveBehaviors;
	}
}
