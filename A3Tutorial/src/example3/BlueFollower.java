package example3;

import A3.A3Message;
import A3.FollowerRole;

public class BlueFollower extends FollowerRole {


	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (this.active) {
			//Add behavioral code...
		}
	}

	@Override
	public void receiveSupervisorMessage(A3Message msg) {
		// TODO Auto-generated method stub
		System.out.println("["+this.getNodeID()+"]    I just received from "+msg.getSupervisor()+" the following message: " + msg.getCounter());
	}

}
