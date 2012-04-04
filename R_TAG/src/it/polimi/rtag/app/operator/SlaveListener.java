package it.polimi.rtag.app.operator;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


public class SlaveListener extends AppCommand {
	 //this class should be extended for extra added listeners		
		
    private boolean active = false;
	
	public SlaveListener() {
		//for each command you should add a listener.
		addCommandListener(joinAppGroup.getName(), new JoinAppListener());
		addCommandListener(leaveAppGroup.getName(), new LeaveAppListener());
		addCommandListener(activateMaster.getName(), new ActivateMasterAppListener());
		addCommandListener(activateSlave.getName(), new ActivateSlaveAppListener());
		//For sending messages
		//addCallListener(deliverMessage.getSubject(), new MessageListener());
		//addResponseListener(responseMessage.getSubject(), new ResponseListener());
		//TODO we should add a listener for Master death!
		//all the slaves of that group should be updated,
		//in the app they will define the master election
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
	
}
