package example2;

import A3.A3Message;
import A3.FollowerRole;


public class RedFollower extends FollowerRole {

	@Override
	public void receiveSupervisorMessage(A3Message msg) {
		// This method will be called when a supervisor sends a message	
		System.out.println("["+this.getNodeID()+"]    I just received from "+msg.getSupervisor()+" the following message: " + msg.getCounter());
	}

	@Override
	public void run() {
		// This method contains the role's behavior
		while (this.active) {
			//Add behavioral code...
		}
		
		
		
	}

	
	

}
