package example1;

import A3.A3Message;
import A3.A3UpdateMessage;
import A3.SupervisorRole;

public class RedSupervisor extends SupervisorRole {

	@Override
	public void run() {
		// This method contains the role's behavior
		int i=0;
		while (this.active) {
			//Add behavioral code...
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("["+this.getNodeID()+"]    Sending message "+i+" to followers...");
			A3Message msg = new A3Message();
			msg.setSupervisor("["+this.getNodeID()+"] RedSupervisor");
			msg.setCounter(i);
			middleware.sendToFollowers(msg, "red");
			i++;
		}
	}

	@Override
	public void receiveGroupUpdate(A3UpdateMessage msg) {
		// This method will be called when their is an update in the nodes in the group
		System.out.println(msg.getMessage());
		
	}

	@Override
	public void receiveFollowerMessage(A3Message msg) {
		// This method will be called when a follower sends a message		
	}

}
