package it.polimi.peersim.controls;

import peersim.core.Control;
import it.polimi.peersim.protocols.*;

public class ThresholdController implements Control {

	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	private void checkTheThreshold(Node currentNode) {
	
	 if(leadedUniverse.isLeader(currentNode) && 
			 leadedUniverse.getFollowers().size()> followerThreshold){
		 //TODO we have to ask a top leader to adopt it!
		 //if not ask a follower who is a leader to adopt it
		 //if not ask a follower create a group and adopt the follower!
		 
		 System.out.println("threshold" +followerThreshold);
		 GroupDescriptor myfollowedUniverse = this.getFollowedUniverse();
		 
	 
		 if (myfollowedUniverse != null) {
			 Node randomFollower0 = leadedUniverse.getFollowers().get(0);	 
			 UniverseProtocol followerProtocol = (UniverseProtocol) 
				 randomFollower0.getProtocol(universeProtocolId);
			 // TODO before adding to the top group check if it is congested
			 Node topLeader = myfollowedUniverse.getLeader();
			 followerProtocol.startFollowing(topLeader);
		 }
		 else{ 
			 for(Node n: leadedUniverse.getFollowers()){
				 UniverseProtocol  fellowProtocol = (UniverseProtocol) 
						 n.getProtocol(universeProtocolId); 
				
			     
				 if(fellowProtocol.leadedUniverse.isLeader(n) && n!=null){
					 System.out.println("****Node" + n.getID());
				//followerProtocol.checkTheThreshold(randomFollower);
				//ask the follower (leader) to adopt fellow follower
					 UniverseProtocol fellowFollowerProtocol
					 = (UniverseProtocol) 
							 n.getProtocol(universeProtocolId);
					 fellowFollowerProtocol.startFollowing(n);
				 break;
				 }
				
			 
			 }//end for
			 
			 Node randomFollower1 = leadedUniverse.getFollowers().get(1);
			 UniverseProtocol newLeaderProtocol = (UniverseProtocol) 
					 randomFollower1.getProtocol(universeProtocolId);
			 newLeaderProtocol.leadedUniverse.setLeader(randomFollower1);
	    }
		 //TODO ask a follower to create a group!
    }
	 
	 return;
	
}

}
