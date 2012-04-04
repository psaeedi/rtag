package it.polimi.rtag.app.example1;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class RedSlave extends RedApp {
	/**
	 * slaves are the all nodes in the network that are going to receive application 
	 * commands or remotecallable from a master.
	 * some slaves can be also master.
	 */

	public RedSlave() {
		super();
		addCommandListener(joinApp.getName(), new JoinAppListener());
		addCommandListener(leaveApp.getName(), new LeaveAppListener());
		addCommandListener(becomeMaster.getName(), new BecomeMasterAppListener());
		addCommandListener(becomeSlave.getName(), new BecomeSlaveAppListener());
	}

	private class JoinAppListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			String name = (String)event.getNewValue();
			getCurrentNode().joinGroup(name);
			System.out.println("joining "+ name);
		}
	}
	
	private class LeaveAppListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			String name = (String)event.getNewValue();
			getCurrentNode().leaveGroup(name);
		}
	}
	
	private class BecomeMasterAppListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			getCurrentNode().getTupleSpaceManager().setApplication(new RedMaster());
		}
	}
	
	private class BecomeSlaveAppListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			throw new AssertionError("Already a slave");
		}
	}
}


