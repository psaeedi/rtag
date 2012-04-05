package it.polimi.rtag.app.operator;


import it.polimi.rtag.app.operator.AppCommand;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class MasterListener extends AppCommand {
	
    //this class should be extended for extra required listeners

	

	public MasterListener() {
		//for each command you should add a listener.
		addCommandListener(joinAppGroup.getName(), new JoinAppListener());
		addCommandListener(leaveAppGroup.getName(), new LeaveAppListener());
		addCommandListener(activateMaster.getName(), new ActivateMasterAppListener());
		addCommandListener(activateSlave.getName(), new ActivateSlaveAppListener());
		addCommandListener(electMaster.getName(), new ActivatelectnewMasterAppListener());
	}

   

	private class JoinAppListener implements PropertyChangeListener {
		@Override
		//master does not  receive command from other nodes 
		//to join or leave a group
		public void propertyChange(PropertyChangeEvent event) {
			throw new AssertionError("I am a master");
		}
	}
	
	private class LeaveAppListener implements PropertyChangeListener {
		@Override
		//master does not  receive command from other nodes 
        //to join or leave a group
		public void propertyChange(PropertyChangeEvent event) {
			throw new AssertionError("I am a master");
		}
	}
	
	private class ActivateMasterAppListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			throw new AssertionError("I am already a master");
		}
	}
	
	private class ActivateSlaveAppListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			getCurrentNode().getTupleSpaceManager().setApplication(new SlaveListener());
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
