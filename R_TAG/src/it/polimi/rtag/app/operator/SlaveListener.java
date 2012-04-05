package it.polimi.rtag.app.operator;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


public class SlaveListener extends AppCommand {
	 //this class should be extended for extra added listeners		

	
	public SlaveListener() {
		//for each command you should add a listener.
		addCommandListener(joinAppGroup.getName(), new JoinAppListener());
		addCommandListener(leaveAppGroup.getName(), new LeaveAppListener());
		addCommandListener(activateMaster.getName(), new ActivateMasterAppListener());
		addCommandListener(activateSlave.getName(), new ActivateSlaveAppListener());
		addCommandListener(electMaster.getName(), new ActivatelectnewMasterAppListener());
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
	
	private class ActivateMasterAppListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			getCurrentNode().getTupleSpaceManager().setApplication(new MasterListener());
		}
	}
	
	private class ActivateSlaveAppListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			throw new AssertionError("Already a slave");
		}
	}
	
	public class ActivatelectnewMasterAppListener implements
	PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent arg0) {
			// TODO Auto-generated method stub
		
		}

	}
	
}
