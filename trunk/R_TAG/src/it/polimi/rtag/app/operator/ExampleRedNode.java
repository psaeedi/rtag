package it.polimi.rtag.app.operator;


public class ExampleRedNode extends AppNode implements Runnable{
	
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
	public  String RED = "Red";
	protected boolean active;
	private AppCommands appCommand;


	public ExampleRedNode(String host, int port) {
		super(host, port);
	}

	@Override
	public void run() {
		setUp("RED");
		//or joinToNewGroup("RED");
		setMaster("RED");
		getBehaviors("RED", active, active);
		
	}

	
	

}
