package it.polimi.rtag.app.operator;

import it.polimi.rtag.Node;


public class ExampleRedNode extends Node implements Runnable {
	
	/*
	 * For each app group define one class like this
	 * 1- first give your group a friendly name 
	 * 2- if the node just wants to start (was not yet joined the network) call the setup
	 * 3- if it is already started ask to join (joinToNewGroup())
	 * 4- set the node as a slave or master inside this current group
	 * if it is master set it otherwise it is considered as a slave in that group
	 * 5- activate which behavior the node can have, first is the master 
	 * second is the slave
	 * 6- get the required behaviors
	 * */
	//group friendly name: 
	private final static String RED = "Red";
	
	private AppCommand appCommand;
	
	public ExampleRedNode(String host, int port) {
		super(host, port);
		
		appCommand = new SlaveListener();
		this.setApplication(appCommand);
		
		appCommand.setMasterBehaviors(new RedMasterBehaviour());
		appCommand.setSlaveBehaviors(new RedSlaveBehaviour());
	}

	@Override
	public void run() {
		//or joinToNewGroup("RED");
		//setMaster("RED");
		//getBehaviors("RED", active, active);		
	}


	@Override
	public void start() {
		super.start();
		Thread th = new Thread(this);
		th.start();
	}

	public void setMaster() {
		appCommand.setMaster(RED);
	}

	public void setBehaviors(boolean canBeMaster, boolean canBeSlave) {
		appCommand.setBehaviors(RED, canBeMaster, canBeSlave);
	}
}
