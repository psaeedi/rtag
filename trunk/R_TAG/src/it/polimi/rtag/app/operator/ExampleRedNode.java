package it.polimi.rtag.app.operator;

import it.polimi.rtag.Node;


public class ExampleRedNode extends Node implements Runnable {
	
	/*
	 * For each app group define one class like this
	 * 1- first give your group a friendly name 
	 * 3- set the node as a slave or master inside this current group
	 * if it is master set it otherwise it is considered as a slave in that group
	 * 4- activate which behavior the node can have, 
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
		//TODO	
	}


	@Override
	public void start() {
		super.start();
		Thread th = new Thread(this);
		th.start();
	}

	//the node that call this should be defined in the launcher.
	public void setMaster() {
		appCommand.setMaster(RED);
	}

	public void setBehaviors(boolean canBeMaster, boolean canBeSlave) {
		appCommand.setBehaviors(RED, canBeMaster, canBeSlave);
	}
}
