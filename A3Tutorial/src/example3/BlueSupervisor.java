package example3;

import A3.A3Message;
import A3.A3UpdateMessage;
import A3.SupervisorRole;

public class BlueSupervisor extends SupervisorRole {

	@Override
	public void run() {
		// This method contains the role's behavior
		int i = 0;
		while (this.active) {
			// Add behavioral code...
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("[" + this.getNodeID() + "]    Sending message " + i + " to blue followers...");
			A3Message msg = new A3Message();
			msg.setSupervisor("[" + this.getNodeID() + "] BlueSupervisor");
			msg.setCounter(i);
			middleware.sendToFollowers(msg, "blue");
			i++;
		}
	}

	@Override
	public void receiveGroupUpdate(A3UpdateMessage msg) {
		// TODO Auto-generated method stub
		System.out.println(msg.getMessage());
	}

	@Override
	public void receiveFollowerMessage(A3Message msg) {
		// TODO Auto-generated method stub

	}

}
