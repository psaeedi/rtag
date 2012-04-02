package it.polimi.rtag.app.example1;

import it.polimi.rtag.*;
import it.polimi.rtag.messaging.TupleMessage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

/**
 * @author Panteha saeedi@ elet.polimi.it
 *
 */

public class RedMaster extends RedApp implements Runnable {
	
	/**
	 * master of application is a node (that can also not be part of the group)
	 * but it has an interface with an operator or an automatic server.
	 * In the network there might be one or several nodes that can act as a master.
	 * each time only one should be active and operator can send commands 
	 * or remotecallables to the rest of the network.
	 * not all the nodes have the ability to be a master since for instance
	 * the have no interface with an operator.
	 */
	
	private boolean active = false;
	
	public RedMaster() {
		//for each command you should add a listener.
		super();
		addCommandListener(joinApp.getName(), new JoinAppListener());
		addCommandListener(leaveApp.getName(), new LeaveAppListener());
		addCommandListener(becomeMaster.getName(), new BecomeMasterAppListener());
		addCommandListener(becomeSlave.getName(), new BecomeSlaveAppListener());
	}

	private class JoinAppListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			throw new AssertionError("I am a master");
		}
	}
	
	private class LeaveAppListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			throw new AssertionError("I am a master");
		}
	}
	
	private class BecomeMasterAppListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			throw new AssertionError("I am already a master");
		}
	}
	
	private class BecomeSlaveAppListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			getCurrentNode().getTupleSpaceManager().setApplication(new RedSlave());
		}
	}
	
	public void start() {
		active = true;
		Thread th = new Thread(this);
		th.start();
	}
	
	public void stop() {
		active = false;
	}
	
	@Override
	public void run() {
		// This method contains the role's behavior
		int i = 0;
		while (this.active) {
			//Add behavioral code...
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("[" + this.getCurrentNode() + "]    Sending message " + i + " to followers...");
			for (GroupDescriptor descriptor: getCurrentNode().getAllGroups()) {
				ExampleMessage message = new ExampleMessage(descriptor.getFriendlyName(), "sequence " + i, "HELLO");
				getCurrentNode().getTupleSpaceManager().storeAndSend(message);
			}
			i++;
		}
	}

}

class ExampleMessage extends TupleMessage {
	private static final long serialVersionUID = -5146903837877861792L;
	
	public ExampleMessage(String recipient,
			Serializable content, String command) {
		super(Scope.HIERARCHY, recipient, content, command);
	}
	
	@Override
	public String getSubject() {
		return CUSTOM_MESSAGE;
	}
	
}