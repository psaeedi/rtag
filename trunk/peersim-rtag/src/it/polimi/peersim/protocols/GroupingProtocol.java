/**
 * 
 */
package it.polimi.peersim.protocols;

import it.polimi.peersim.messages.BaseMessage;
import it.polimi.peersim.messages.GroupingMessage;
import it.polimi.peersim.messages.UniverseMessage;
import it.polimi.peersim.prtag.AppGroupManager;
import it.polimi.peersim.prtag.GroupDescriptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.HashMultimap;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.Node;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 * The protocol stack is:
 * 5 - Grouping
 * 4 - Routing
 * 3 - UniverseProtocol
 * 2 - TupleSpaceProtocol
 * 1 - MockChannel
 */
public class GroupingProtocol extends ForwardingProtocol<GroupingMessage> implements CDProtocol {
	
	private static final String UNIVERSE_PROTOCOL = "universe_protocol";
	private static int universeProtocolId;
	
	// All the managers of this node
	private HashMap<String, AppGroupManager> managers = new 
			HashMap<String, AppGroupManager>();
	
	// All the known group descriptors by group name
	private HashMultimap<String, GroupDescriptor> knownGroups = 
			HashMultimap.create();
	
	private ArrayList<String> myGroupNames = new ArrayList<String>();

	public GroupingProtocol(String prefix) {
		super(prefix);
		universeProtocolId = Configuration.getPid(
				prefix + "." + UNIVERSE_PROTOCOL);
    }
	
	@Override
	public Object clone() {
		GroupingProtocol clone = null;
		clone = (GroupingProtocol) super.clone();
		clone.managers = new HashMap<String, AppGroupManager>(managers);
		clone.knownGroups = HashMultimap.create(knownGroups);
		clone.myGroupNames = new ArrayList<String>(myGroupNames);
        return clone;
	}
	
	public void removeExpiredMessages() {
		// remove all the messages whose timestamp is expired
	}
	
	public void removeGroupForLostNodes() {
		// ???????????????????
		// TODO remove groups for a lost node
	}
	
	public AppGroupManager getOrCreateManager(Node currentNode, String groupName) {
		if(!managers.containsKey(groupName)){
			AppGroupManager groupmanager = 
					new AppGroupManager(groupName, currentNode);
		   managers.put(groupName, groupmanager);
		   return groupmanager;
		}
		return managers.get(groupName);
	}
	
	

	public void joinOrCreateGroup(Node currentNode, String name) {
		
		//immediately check u have a manager for that group
		AppGroupManager groupManager = getOrCreateManager(currentNode, name);
		if (groupManager.getFollowedGroup() != null || groupManager.getLeadedGroup() != null) {
			throw new AssertionError(
			"This node is already following a group called: " + name);
		}
		
		// if no group exist with that name create a new one and
		// broadcast to everyone
		if (knownGroups.get(name).isEmpty()) {
			GroupDescriptor groupDescriptor = new GroupDescriptor(
					UUID.randomUUID(), name, currentNode); 
			
			knownGroups.put(name, groupDescriptor);
			myGroupNames.add(name);
			groupManager.setLeadedGroup(groupDescriptor);
			System.out.println("Group::[Node " + currentNode.getID() + 
					"] [creating group " + name+"]" + groupManager.getLeadedGroup().getFriendlyName() );
		    broadcastGroupCreatedOrChanged(currentNode, groupDescriptor);
		   
		 } else {
			// if a known group exist ask to join
			System.out.println("Group::[Node " + currentNode.getID() + 
					"] [joining group " + name+"]");
			Node leader = null;
			GroupDescriptor groupdescriptor = null;
				for(GroupDescriptor descriptors: knownGroups.get(name)){
			       leader = descriptors.getLeader();
			       // TODO (optional) use the routing protocol to find the closer leader
			       if (leader != null) {
			    	   groupdescriptor=descriptors;
			    	   break;
			       }
				}
				if (leader == null) {
					throw new AssertionError(
							"If leader is null why we have a groupdescriptor");
				}
			    askLeaderToJoin(currentNode, leader, groupdescriptor);
		 }
	}
	
	public void broadcastGroupCreatedOrChanged(Node currentNode, GroupDescriptor descriptor) {
		// Use the  universe protocol to send the changes
		GroupingMessage message = GroupingMessage.createUpdateDescriptor(
				protocolId, currentNode, descriptor);
		
		UniverseProtocol protocol = (UniverseProtocol)currentNode.
				getProtocol(universeProtocolId);
		//System.out.println("gdb1"+message.getBody() );
		protocol.sendBroadCast(currentNode, message);
	}
	
	public void broadcastGroupDeleted(Node currentNode, GroupDescriptor descriptor) {
		// Use the  universe protocol to send the changes
		GroupingMessage message = GroupingMessage.createDeleteDescriptor(
				protocolId, currentNode, descriptor);
		UniverseProtocol protocol = (UniverseProtocol)currentNode.getProtocol(universeProtocolId);
		protocol.sendBroadCast(currentNode, message);
	}
	
	public void askLeaderToJoin(Node currentNode, Node leader, GroupDescriptor groupdescriptor) {
		// the node ask a leader to join him
		System.out.println("pani");
		/*GroupingMessage message = GroupingMessage.createJoinRequest(
				protocolId, currentNode, groupdescriptor);
		pushDownMessage(currentNode, leader, message);*/
		
		GroupCommand command = new GroupCommand
				(GroupCommand.JOIN_REQUEST, groupdescriptor);
		
		GroupingMessage message = GroupingMessage.createJoinRequest(
						protocolId,
						currentNode,
						command);
		pushDownMessage(currentNode, leader, message);
		
	}
	
	public void handleJoinRequest(Node currentNode, Node follower, GroupDescriptor groupDescriptor2) {
		// a node asked this leader to join him
		// TODO add to the leaded group of that group and get the updated group descriptor
		// TODO send the updated group descriptor to all the other followers
		String groupName = groupDescriptor2.getFriendlyName();
		AppGroupManager manager = getOrCreateManager(currentNode, groupName);
		GroupDescriptor groupDescriptor = manager.getOrCreateLeadedGroup();
		System.out.println("========Group::[Node " + currentNode.getID() + 
				"] [creating group "  + manager.getOrCreateLeadedGroup().getFriendlyName() );
		
		groupDescriptor.addFollower(follower);
		
		GroupingMessage message = GroupingMessage.createJoinRequestAck(
				protocolId, currentNode, groupDescriptor);
		pushDownMessage(currentNode, follower, message);

		broadcastGroupCreatedOrChanged(currentNode, groupDescriptor);
	}

	public void handleJoinRequestAck(
			Node currentNode, Node leader, GroupDescriptor groupDescriptor) {
		// the leader has responded
		// add to the group manager the leader
		AppGroupManager manager = getOrCreateManager(
				currentNode, groupDescriptor.getFriendlyName());
		if (manager.getFollowedGroup() != null) {
			throw new AssertionError("Already in a group");
		}
		
		manager.setFollowedGroup(groupDescriptor);
		String groupName = groupDescriptor.getFriendlyName();
		knownGroups.put(groupName, groupDescriptor);
		AppGroupManager groupManager = getOrCreateManager(currentNode, groupName);
		managers.put(groupName, groupManager);
		if(!myGroupNames.contains(groupName)){
			myGroupNames.add(groupName);}
	}
	

	private void handleLocalGroupDescriptorChanged(
			Node currentNode, GroupDescriptor remotegroupDescriptor) {
		
		Node groupLeader = remotegroupDescriptor.getLeader();
		List<Node> groupFollowers = remotegroupDescriptor.getFollowers();
		String groupName = remotegroupDescriptor.getFriendlyName();
		// we check if we have the manager for that!?
		AppGroupManager manager = getOrCreateManager(currentNode, groupName);
		//does it replace it ?!
		//update the descriptor
  		knownGroups.put(groupName, remotegroupDescriptor);
  		if(!myGroupNames.contains(groupName)){
			myGroupNames.add(groupName);}
  		//if there is no manager, we should create one
  		
		
		// If it is a groupleader update the descriptor
		if(currentNode == groupLeader){
			manager.setLeadedGroup(remotegroupDescriptor);
			knownGroups.removeAll(groupName);
			knownGroups.put(groupName, remotegroupDescriptor);
			//leader should inform its followers of the updated groupDescriptor.
			/*for(Node followers: groupmanager.getLeadedGroup().getFollowers()){
				GroupingProtocol protocol = (GroupingProtocol)followers.getProtocol(groupProtocolId);
				protocol.groupmanager.setFollowedGroup(remotegroupDescriptor);
				protocol.
			}*/
		}
		
		if(groupFollowers.contains(currentNode)){
			manager.setFollowedGroup(remotegroupDescriptor);
			knownGroups.removeAll(groupName);
			knownGroups.put(groupName, remotegroupDescriptor);
			System.out.println("*@*@*@*@*@ node"+currentNode.getID() );
		} else {
			knownGroups.put(groupName, remotegroupDescriptor);
			if(!myGroupNames.contains(groupName)){
				myGroupNames.add(groupName);}
		}
	}

	@Override
	public void nextCycle(Node currentNode, int pid) {
		// TODO load balancing
	}
	
	//==============================================================================
	
    
    public void askToMerge(Node remoteNode){
    	//TODO ask the neighbor to Merge its sub-group with u
    	//one should remove its sub group
    	
    }
    
    public void askToSplit(Node remoteNode){
    	//TODO ask the follower to become a leader 
    	
    }
    
    public void askToAdopt(Node remoteNode){
    	//TODO ask other leader to adopt a follower	
    }
    
    public void handleMergeRequest(Node caller, GroupDescriptor descriptor){
    	//TODO ask the neighbor to Merge its sub-group with u
    	//one should remove its sub group
    	
    }
    
    public void handleSplitRequest(Node caller, GroupDescriptor descriptor){
    	//TODO ask the follower to become a leader 
    	
    }
    
    public void handleAdoptRequest(Node caller, GroupDescriptor descriptor){
    	//TODO ask other leader to adopt a follower	
    }
    
    public void askToMergeAck(Node remoteNode){
    	//TODO ask the neighbor to Merge its sub-group with u
    	//one should remove its sub group
    	
    }
    
    public void askToSplitAck(Node remoteNode){
    	//TODO ask the follower to become a leader 
    	
    }
    
    public void askToAdoptAck(Node remoteNode){
    	//TODO ask other leader to adopt a follower	
    }


	@Override
	public GroupingMessage handlePushDownMessage(Node currentNode,
			Node recipient, Serializable content) {
		if (content instanceof GroupingMessage) {
			return (GroupingMessage) content;
		} else {
			return GroupingMessage.wrapMessage(
					protocolId, currentNode, (BaseMessage)content);
		}
	}


	@Override
	public BaseMessage handlePushUpMessage(
			Node currentNode, Node sender,
			GroupingMessage message) {
		
		String head = message.getHead();
		System.out.println("GROUP:: " + currentNode.getID() + " <- " + 
				message.getSender().getID() + 
				" message " + head);
		
		if (GroupingMessage.UPDATE_DESCRIPTOR.equals(head)) {
			System.out.println("UPDATE_DESCRIPTOR node " + message.getSender().getID());
			handleLocalGroupDescriptorChanged(
					currentNode, (GroupDescriptor) message.getContent());
			return null;
		}
		
		if (GroupingMessage.DELETE_DESCRIPTOR.equals(head)) {
			handleLocalGroupDescriptorChanged(
					currentNode, (GroupDescriptor) message.getContent());
			return null;
		}
		
		/*if (GroupingMessage.JOIN_REQUEST.equals(head)) {
			//leader is the local node
			System.out.println("66666666666666666666666666" +
					"JOIN_REQUESTR node " + message.getSender().getID());
			handleJoinRequest(
					currentNode,
					message.getSender(),
					(GroupDescriptor) message.getContent());
			return null;
		}*/
		
		if (GroupingMessage.GROUP_COMMAND.equals(message.getHead())) {
			handleGroupCommand(currentNode, sender, (GroupCommand)message.getContent());
			return null;
		}
		
		if (GroupingMessage.REPLACELEADER_REQUEST.equals(head)) {
			//leader is the local node
			handleLeaderReplacement(
					currentNode,
					(String) message.getContent());
			return null;
		}
		
		if (GroupingMessage.JOIN_REQUEST_ACK.equals(head)) {
			System.out.println("gdh " + message.getHead());
			System.out.println("gdb " + message.getContent());
			handleJoinRequestAck(
					currentNode,
					message.getSender(),
					(GroupDescriptor) message.getContent());
			return null;
		}
		
		if (GroupingMessage.WRAP.equals(head)) {
			return (BaseMessage) message.getContent();
		}
		return null;
	}
	
	
	private void handleGroupCommand(Node currentNode, Node sender, GroupCommand command) {

		String commandName = command.getCommand();
		GroupDescriptor groupDescriptor = (GroupDescriptor) command.getContent();
		
		if (GroupCommand.JOIN_REQUEST.equals(commandName)) {
			handleJoinRequest(currentNode, sender, groupDescriptor);
			
		} 
	}

	

	private void handleLeaderReplacement(Node currentNode, String groupName) {
		AppGroupManager manager = getOrCreateManager(currentNode, groupName);
		//the current node is a follower of the groupName but now he should become
		//the leader, since the leader is dead
		GroupDescriptor groupDescriptor = manager.getFollowedGroup();
		groupDescriptor.setLeader(currentNode);
		manager.setLeadedGroup(groupDescriptor);
		//no need to send ack, since we update the groupdescriptor!
		broadcastGroupCreatedOrChanged(currentNode, groupDescriptor);
	}

	public void notifyRemovedNodes(Node currentNode, Node lostNode) {
		//current node checks all its known groups for the lost node
		//get its manager for each and check if it is following them or leading them
		
		for(String groups: myGroupNames){
	        for(GroupDescriptor groupDescriptor: knownGroups.get(groups)){
	        	//System.out.println("^^^^^^^^^^^Group::[Node " + currentNode.getID() + 
				//		"] [check" + groups+ "leader"+groupDescriptor.getLeader().getID() );
	        	//System.out.println("^^^^^^^^^^^Group::[lostNode " + lostNode.getID() + 
				//		"] [check" + groups ); 	
	        	//both are the follower
	        	if(groupDescriptor.isFollower(lostNode) && groupDescriptor.isFollower(currentNode)){
	        		// inform the leader of that group its follower is lost
	        		// so he should update the groupDescriptor
	        		Node leader = groupDescriptor.getLeader();
	        		tellLeaderTheLostFollower(currentNode, lostNode, leader, groups);
	        	}
	        	
	        	//lost node was the follower and current node the leader
	        	if(groupDescriptor.isFollower(lostNode) && groupDescriptor.isLeader(currentNode)){
	        		//currentNode is a leader, it should update its groupdescriptor,
			    	// and remove the lost node from its leadedgroup 
			 		groupDescriptor.removeFollower(lostNode);
			 		broadcastGroupCreatedOrChanged(currentNode, groupDescriptor);
	        	}
	        	//lost node was the leader and current node the follower
	        	if(groupDescriptor.isLeader(lostNode) && groupDescriptor.isFollower(currentNode)){
	        		Node nextLeader = null;
	        		//the follower with smallest Id
	        		for( Node members: groupDescriptor.getFollowers()){
	        			long smallID = -1;
	        			if( members.getID()< smallID){
	        				smallID = members.getID();
	        			    nextLeader = members;
	        			 }
	        		  }
	        		  //inform the newleader!
	        		  //he should announce himself as the new leader
	        		  //and update the groupdescriptor
	        	      tellTheNewLeaderIsElected(currentNode, nextLeader, groups);	
	        	}
	        	
	        }
	        
		}
	}

	private void tellTheNewLeaderIsElected(Node currentNode, Node nextLeader,
			String groupName) {
		    // currentNode tell the other follower that he should replace the dead leader1
			GroupingMessage message = GroupingMessage.createLeaderReplacement(
					protocolId, currentNode, groupName);
			pushDownMessage(currentNode, nextLeader, message);
		
	}

	private void tellLeaderTheLostFollower(Node currentNode , Node lostFollower,
			Node leader, String groupName) {
		//TODO who shall we send?
		GroupingMessage message = GroupingMessage.createRemoveLostFollowerRequest(
				protocolId, lostFollower, groupName);
		pushDownMessage(currentNode, leader, message);
		
	}

}
