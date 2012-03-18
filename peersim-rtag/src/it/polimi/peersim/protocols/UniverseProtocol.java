package it.polimi.peersim.protocols;

import java.util.ArrayList;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.core.Protocol;
import it.polimi.peersim.prtag.GroupCommunicationDispatcher;
import it.polimi.peersim.prtag.GroupDescriptor;

public class UniverseProtocol implements Protocol{ 

		
	private ArrayList<GroupDescriptor> discoveredGroupName =
			new ArrayList<GroupDescriptor>();
	
	private GroupDescriptor leadedUniverse;
	private GroupDescriptor followedUniverse;
	
	private Node currentNode;
	private GroupCommunicationDispatcher groupCommunicationDispatcher;

	
	private ArrayList<Node> followers = new ArrayList<Node>();

	private static final String UNIVERSE_PROTOCOL = "universe_protocol";
	private static int universeProtocolId;
	
	private static final String FOLLOWER_THRESHOLD = "follower_threshold";
	protected final int followerThreshold;
	
	//private static Node topParent = null;
	
	//private static final String DISCOVERY_PROTOCOL = "discovery_protocol";
	//private static int discoveryProtocolId;

	public UniverseProtocol(String prefix) {
		universeProtocolId = Configuration.getPid(
				prefix + "." + UNIVERSE_PROTOCOL);
		followerThreshold = Configuration.getInt(
				prefix + "." + FOLLOWER_THRESHOLD, 2);
		
	}

	@Override
	public Object clone() {
		UniverseProtocol inp = null;
        try {
        	inp = (UniverseProtocol) super.clone();
        	inp.discoveredGroupName = (ArrayList<GroupDescriptor>) 
        			this.discoveredGroupName.clone();
  
        } catch (CloneNotSupportedException e) {
        } // never happens
        return inp;
	}
	

	/**
	 * Sets the current node and create a new universe for it.
	 * Also set the current instance as a change listener for the
	 * discovery protocol.
	 * 
	 * @param currentNode the current node.
	 */
	public void initialize(Node currentNode){//, DiscoveryProtocol discoveryProtocol) {
		this.currentNode = currentNode;
		leadedUniverse = GroupDescriptor.createUniverse(this.currentNode, this.currentNode);
	}


	
	/**
	 * Invoked every time a new neighbor is added.
	 * 
	 * This method simulates the coordination that
	 * happens when a node is discovered.
	 * 
	 * The larger Id will ask the other node to ask its 
	 * top parent leader to join him as follower!
	 * 
	 *  o   /O 
	 *  o  / o
	 *  o /  o
	 *  O/-->o
	 */
	public void handleNeighbourDiscovered(Node addedNode){	
		//Node n = currentNode;
		Node remoteParentLeader ;
		Node currentParentLeader ;
		
		UniverseProtocol remoteprotocol = (UniverseProtocol) 
				addedNode.getProtocol(universeProtocolId);
		
		//1- find who add who?
		if(whoAskWhotoFollow(addedNode)!=currentNode)
		{
			//if the current node should ask continue otherwise
			//go back to notifyAddedNode
			return;
		}
		 
		//2-get  the family name , if any, of each node.
		remoteParentLeader = remoteprotocol.getParentLeaderOf();
		currentParentLeader = getParentLeaderOf();
		
	

		if(currentParentLeader == remoteParentLeader && currentParentLeader!=null)
		{
			//the neighbor nodes are already in a same hierarchy
			if(currentNode==remoteParentLeader){
			  return;
			  //no need to do anything
			  //since we want to add the remoteparentleader as follower of current!
			}
			//we should send a request to added node to join us!
			//not the parent! since it is our parents as well
			requestToJoin(addedNode);

		}
		
		if(remoteParentLeader==null){
			//addednode is in no universe
			System.out.println("remoteparentleader is null" );
			remoteprotocol.startFollowing(currentNode);
		}
		
		else{
			 System.out.println("remoteParentLeader " + remoteParentLeader.getID());
			 UniverseProtocol protocol = (UniverseProtocol) 
					remoteParentLeader.getProtocol(universeProtocolId);
			//in variable protocol we have an instance
			//of the top leader protocol
			//we tell the parentLeader of addednode to start following the current node

		 System.out.println("remote " + addedNode.getID()+ "currentnode" + currentNode.getID() );
		//asking you <parentleader> to start following me
		 
		protocol.startFollowing(currentNode);
		}
	}
	

	private void requestToJoin(Node addedNode) {
		// this is only for two family node!
		// with a same parentLeader
		//TODO
		throw new RuntimeException("requestTojoin is not implemented");
	}

	private Node getParentLeaderOf() {
		       Node parentLeader = null;
		
		   
				if(leadedUniverse!=null){
					parentLeader = leadedUniverse.getParentLeader();
					return parentLeader;
				}
				
				else if(followedUniverse!=null){
					 parentLeader = followedUniverse.getParentLeader();
					 return parentLeader;
				}
				return parentLeader;
		
	}

	

	private  Node whoAskWhotoFollow(Node addedNode) {
		//since both nodes are invoke for being neighbor
		//only one should ask to join him as follower
		UniverseProtocol remoteprotocol = (UniverseProtocol) 
				addedNode.getProtocol(universeProtocolId);
		
		//First see if any of two neighbors is not a leader!
		//the one without is not going to be the potential leader
		//else go for larger id
				if(leadedUniverse==null){
						if(remoteprotocol.leadedUniverse!=null){
							//System.out.println( "remote node has a LEADEDUNIVERSE but 
							//current node has NO LEADEDUNIVERSE" );
							//potential leader is the addedNode
							return addedNode;
						}
						System.out.println( "neither are in universe" );
						//go for the larger id
						if (currentNode.getID() < addedNode.getID()) {
							//System.out.println( "so select the larger id" );
							return addedNode;
						}
				}
				
				else {
					//both with leaded universes, so go for larger id
						if(remoteprotocol.leadedUniverse!=null){
							if (currentNode.getID() < addedNode.getID()) {
								//System.out.println( "so select the larger id" );
								return addedNode;
							}
							//System.out.println( "remote node has NO LEADEDUNIVERSE but current node has a LEADEDUNIVERSE" );
						}
				}
				//so the neighbor who should ask to follow is selected(potential leader)
		return  currentNode;
	}

	/**
	 * Simulates that the current node will start 
	 * following the remote node.
	 * 
	 * If the current leaded group has no followers it will be dismantled.
	 * 
	 * If the remote node has no leaded group it will be created.
	 * 
	 * @param addedNode
	 */
	private void startFollowing(Node potentialLeader) {
		//here the current node wants to
		//follow the remote node!
		//current node potential follower, which is a parentLeader or in no universe
		
		UniverseProtocol remoteProtocol = (UniverseProtocol) 
				potentialLeader.getProtocol(universeProtocolId);
	    //followeduniverse of potential follower
		if(followedUniverse != null) {
			//it should be no followed universe!?
			throw new AssertionError("already following a universe:@startFollowing");    
		}
		
		//we ask the remote node(potential leader) to add the current node(potential follower)
		remoteProtocol.addPotentialFollower(currentNode);
		//check if u are not passing the threshold (max num of followers)

	}
		


	
   //remote node wants to join the current node(potential leader)
	private void addPotentialFollower(Node potentialFollower) {
		 UniverseProtocol  remoteprotocol = (UniverseProtocol) 
				 potentialFollower.getProtocol(universeProtocolId);
		 
			  //remote node is a leader	 
		 
         if(remoteprotocol.leadedUniverse != null){
	        	//if potentialfollower is following the same universe already 
	    	    if(leadedUniverse!=null){
		            //get its hierarchy parent name
		    	    Node parentLeader = leadedUniverse.getParentLeader();
			        
					if(remoteprotocol.isFollowerOf(parentLeader))
					{//TODO check the threshold ! then reask to join
						throw new RuntimeException("they are already in a same heirarchy:currentNode" +
								 currentNode.getID()  );
						
					}
					//else we have two different universes!
	    	    }
        }
      
		//potential leader  is not yet a  leader at all!
        // we should first sort this!
         createleadedUniverse();   
	
		//now sort the potential follower leaded universe
		// Ask the remote node protocol for its leaded universe
		// it means which universe it is leading.
         
         if(remoteprotocol.leadedUniverse != null) {
                remoteprotocol.manageTheLeadeUniverse(leadedUniverse.getParentLeader());
         }
         //creates its followedUniverse, 
         remoteprotocol.followedUniverse = new GroupDescriptor(leadedUniverse); 
		 //now add it in the follower list
		 leadedUniverse.addFollower(potentialFollower);
		
	}

	
	/**
	 * manageTheLeadeUniverse, is responsible to
	 * update the parentLeader only
	 * The leader will be untouched also the followers!
	 * 
	 * 
	 **/
	private void manageTheLeadeUniverse(Node newParentLeader) {
		
		    if(leadedUniverse == null) {
		    	throw new RuntimeException("there is no leadedUniverse to manage:currentNode" +
						 currentNode.getID()  );
		    }
			
		    //if it is empty delete it
			//if it has followers update them
			if(leadedUniverse.getFollowers().size() == 0) {
				//TODO inform the tuple space the group has been removed
				leadedUniverse = null;
			} else {//it has followers
				for(Node follower: leadedUniverse.getFollowers()){
						UniverseProtocol followerProtocol = (UniverseProtocol) 
								follower.getProtocol(universeProtocolId);
					
						//we need to update the group descriptor of all followers
						// we changed the parentLeader, not the leader	
						if(followerProtocol.followedUniverse!=null){
						   followerProtocol.followedUniverse.setParentLeader(newParentLeader);
						}
					
					}
				//for the leader itself
				leadedUniverse.setParentLeader(newParentLeader);
				}
			
			return;
	}

	private void createleadedUniverse() {
		if(leadedUniverse == null){
			//but it is a follower, so it become a leader as well 
			if(followedUniverse!=null){
				Node parentLeader = followedUniverse.getParentLeader();
				leadedUniverse =  GroupDescriptor.createUniverse(currentNode, parentLeader);
	
			}
			//it is not following any universe nor leading 
			else{
	           leadedUniverse = GroupDescriptor.createUniverse(currentNode, currentNode);
	           }
		}
		return;
	}

	private boolean isFollowerOf(Node parentLeader) {
		
		if(leadedUniverse == null){
			return false;
		}
		if(parentLeader == leadedUniverse.getParentLeader()){
			if(parentLeader==currentNode)
			{
				throw new RuntimeException("parentLeader is the current node");
			}
			//it is already in a same hierarchy
		     return true;
		}
		return false;
	}

	
	/**
	 * Invoked every time one of the neighbor disappear.
	 * 
	 * If the lost neighbor is the leader a new leader should be elected.
	 * 
	 * If the neighbor was a follower the leader will handle it. 
	 * If this node is the leader then it has to notify the other followers. 
	 */
	public void handleNeighbourLost(Node lostNode) {
	    //ask the lostnode if it is a leader? 
		UniverseProtocol protocol = null;
		protocol = (UniverseProtocol) lostNode.getProtocol(universeProtocolId);
		
		if(protocol.followedUniverse.isLeader(lostNode))
		{
			//first check it has any follower or its group become empty!
			if(leadedUniverse.getFollowers().size() == 0)
			{//TODO inform the tuple space the group has been removed
				leadedUniverse = null;
				return;
			}
			//elect a new leader, replace the lost leader!
			protocol.electNewLeader(lostNode);
		}
		else{
			//inform all the members of the lost
			for(Node follower: leadedUniverse.getMembers()){
				UniverseProtocol followerProtocol = (UniverseProtocol) 
						follower.getProtocol(universeProtocolId);
				//we need to update the group descriptor of all followers
				//instead of sending messages we call the protocol to do that
				//TODO how are the followers are informed?
				return;
				}
		}
	}
		
	
	private void electNewLeader(Node lostNode) {
		long minId=0;
		Node nextLeader = null;
		
		for(Node follower: leadedUniverse.getFollowers()){
			UniverseProtocol followerProtocol = (UniverseProtocol) 
					follower.getProtocol(universeProtocolId);
			//select follower with smallest id as the leader
			if(follower.getID()>minId){
		        minId=follower.getID();
		        nextLeader = follower;
			}
			return;
		}
		
		for(Node follower: leadedUniverse.getFollowers()){
			UniverseProtocol followerProtocol = (UniverseProtocol) 
					follower.getProtocol(universeProtocolId);
			//we need to update the group descriptor of all followers
			//instead of sending messages we call the protocol to do that
			followerProtocol.followedUniverse.setLeader(nextLeader);
			return;
			}

	}

	public void joinGroup(String friendlyName) {
		// if string is equal to the constant universe, rise an exception
		if (GroupDescriptor.UNIVERSE.equals(friendlyName)) {
			throw new RuntimeException("The application cannot join the universe.");
		}
		// otherwise tell dispatcher to create and join a group of what we need
		groupCommunicationDispatcher.joinGroupAndNotifyNetwork(friendlyName);
	}

	public void leaveGroup(String friendlyName) {
		if (GroupDescriptor.UNIVERSE.equals(friendlyName)) {
			throw new RuntimeException("The application cannot leave the universe.");
		}
		groupCommunicationDispatcher.leaveGroupsWithName(friendlyName);
		
	}

	
	public ArrayList<Node> getFollowers(){
	
		if(leadedUniverse.isLeader(currentNode)){
			followers = (ArrayList<Node>) leadedUniverse.getFollowers();
		}
		return followers;
	}
	
	

	public void notifyAddedNode(ArrayList<Node> added) {
	
	
		for (Node addedNode: added) {
			if(addedNode==currentNode){
				throw new RuntimeException("WARNING:node is adding itself in a list of added node");
			}
			//for every found neighbor it should manage its neighbor
			handleNeighbourDiscovered(addedNode);
		}

	}


	public void notifyRemovedNode(ArrayList<Node> removed) {
		for (Node n: removed) {
			handleNeighbourLost(n);
			System.out.println("notify removed Node " + currentNode.getID()  );
		}
		
	}

	public GroupDescriptor getLeadedUniverse() {
		return leadedUniverse;
	}

	public GroupDescriptor getFollowedUniverse() {
		return followedUniverse;
	}
	

	
}
