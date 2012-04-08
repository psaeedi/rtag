/**
 * 
 */
package it.polimi.peersim.protocols;

import it.polimi.peersim.messages.BaseMessage;
import it.polimi.peersim.messages.GroupingMessage;
import it.polimi.peersim.prtag.GroupManager;
import it.polimi.peersim.prtag.GroupDescriptor;
import it.polimi.peersim.prtag.UndeliverableMessageException;

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
public class GroupingProtocol extends ForwardingProtocol<GroupingMessage> 
		implements CDProtocol {
	
	private static final String UNIVERSE_PROTOCOL = "universe_protocol";
	private static int universeProtocolId;
	
	// All the managers of this node
	private HashMap<String, GroupManager> managers = new 
			HashMap<String, GroupManager>();
	
	// All the known group descriptors by group name
	private HashMultimap<String, GroupDescriptor> knownGroups = 
			HashMultimap.create();
	
	public GroupingProtocol(String prefix) {
		super(prefix);
		universeProtocolId = Configuration.getPid(
				prefix + "." + UNIVERSE_PROTOCOL);
    }
	
	@Override
	public Object clone() {
		GroupingProtocol clone = null;
		clone = (GroupingProtocol) super.clone();
		clone.managers = new HashMap<String, GroupManager>(managers);
		clone.knownGroups = HashMultimap.create(knownGroups);
		return clone;
	}
	
	
	public GroupManager getOrCreateManager(Node currentNode, String groupName) {
		if(!managers.containsKey(groupName)){
			GroupManager groupmanager = 
					new GroupManager(groupName, currentNode);
		   managers.put(groupName, groupmanager);
		   return groupmanager;
		}
		return managers.get(groupName);
	}
	

	public void joinOrCreateGroup(Node currentNode, String groupName) {
		if (managers.containsKey(groupName)) {
			throw new AssertionError(
					"This node is already in a group called: " + groupName);
		}
		
		GroupManager groupManager = getOrCreateManager(
				currentNode, groupName);

		// If no group exist with that name create a new one and
		// broadcast to everyone
		if (knownGroups.get(groupName).isEmpty()) {
			// No groups with that name exists in the network
			GroupDescriptor groupDescriptor = new GroupDescriptor(
					UUID.randomUUID(), groupName, currentNode); 			
			knownGroups.put(groupName, groupDescriptor);
			groupManager.setLeadedGroup(groupDescriptor);
			System.out.println("Group::[Node " + currentNode.getID() + 
					"] [creating group " + groupName+"]");
		    broadcastGroupCreatedOrChanged(currentNode, groupDescriptor);
		 } else {
			// if a known group exist ask to join
			System.out.println("Group::[Node " + currentNode.getID() + 
					"] [joining group " + groupName+"]");
			Node leader = null;
			GroupDescriptor groupDescriptor = null;
			for (GroupDescriptor descriptor: knownGroups.get(groupName)) {
		       // TODO (optional) use the routing protocol to find the closer leader
		       leader = descriptor.getLeader();
		       groupDescriptor = descriptor;
		       break;
			}
			if (leader == null) {
				throw new AssertionError(
						"If leader is null why we have a groupdescriptor");
			}
			// the node ask a leader to join him
			GroupCommand command = new GroupCommand
					(GroupCommand.JOIN_REQUEST, groupDescriptor);
			
			GroupingMessage message = GroupingMessage.createJoinRequest(
							protocolId, currentNode, command);
			try {
				pushDownMessage(currentNode, leader, message);
			} catch(UndeliverableMessageException ex) {
				// The selected leader was unreachable
				// Remove the descriptor from the list and retry
				knownGroups.remove(groupName, groupDescriptor);
				joinOrCreateGroup(currentNode, groupName);
			}
		 }
	}

	
	public void broadcastGroupCreatedOrChanged(Node currentNode, GroupDescriptor descriptor) {
		// Use the  universe protocol to send the changes
		GroupingMessage message = GroupingMessage.createUpdateDescriptor(
				protocolId, currentNode, descriptor);
		
		UniverseProtocol protocol = (UniverseProtocol)currentNode.
				getProtocol(universeProtocolId);
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
		GroupCommand command = new GroupCommand
				(GroupCommand.JOIN_REQUEST, groupdescriptor);
		
		GroupingMessage message = GroupingMessage.createJoinRequest(
						protocolId, currentNode, command);
		try {
			pushDownMessage(currentNode, leader, message);
		} catch(UndeliverableMessageException ex) {
			// The selected leader was unreachable
			// Remove the descriptor from the list and retry
			knownGroups.remove(groupdescriptor.getName(), groupdescriptor);
			joinOrCreateGroup(currentNode, groupdescriptor.getName());
		}
	}
	
	public void handleJoinRequest(Node currentNode, Node follower,
			String groupName) {
		// a node asked this leader to join him
		// TODO add to the leaded group of that group and get the updated group descriptor
		// TODO send the updated group descriptor to all the other followers
		GroupManager manager = getOrCreateManager(currentNode, groupName);
		GroupDescriptor groupDescriptor = manager.getOrCreateLeadedGroup();
		groupDescriptor.addFollower(follower);
		
		try {
			GroupCommand command = new GroupCommand(
					GroupCommand.JOIN_REQUEST_RESPONSE, groupDescriptor);
			GroupingMessage message = GroupingMessage.createGroupCommand(
					protocolId, currentNode, command);
			pushDownMessage(currentNode, follower, message);
		} catch (UndeliverableMessageException e) {
			// The remote node was unreachable
			// Aborting
			return;
		}

		broadcastGroupCreatedOrChanged(currentNode, groupDescriptor);
	}

	public void handleJoinRequestAck(
			Node currentNode, Node leader, 
			GroupDescriptor groupDescriptor) {
		// the leader has responded
		// add to the group manager the leader
		GroupManager manager = getOrCreateManager(
				currentNode, groupDescriptor.getName());
		if (manager.getFollowedGroup() != null) {
			throw new AssertionError("Already in a group");
		}
		
		manager.setFollowedGroup(groupDescriptor);
		String groupName = groupDescriptor.getName();
		knownGroups.put(groupName, groupDescriptor);
		managers.put(groupName, manager);
	}
	

	private void handleGroupDescriptorChanged (
			Node currentNode, GroupDescriptor remotegroupDescriptor) {
		Node groupLeader = remotegroupDescriptor.getLeader();
		List<Node> groupFollowers = remotegroupDescriptor.getFollowers();
		String groupName = remotegroupDescriptor.getName();
		
		// Add it to the map
		knownGroups.put(groupName, remotegroupDescriptor);

		// we check if we have the manager for that!?
		//if there is no manager, we should create one
		GroupManager manager = managers.get(groupName);
		if (manager == null) {
			return;
		}
		
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
		}
	}
	
	private void handleGroupDescriptorDeleted (
			Node currentNode, GroupDescriptor deletedDescriptor) {
		String groupName = deletedDescriptor.getName();
		knownGroups.remove(groupName, deletedDescriptor);

		GroupManager manager = managers.get(groupName);
		if (manager == null) {
			return;
		}
		
		if (deletedDescriptor.equals(manager.getLeadedGroup())) {
			throw new AssertionError("The group was not deleted by its leader");
		} else if (deletedDescriptor.equals(manager.getFollowedGroup())) {
			findGroupToFollow(currentNode, groupName);
		}
	}

	/**
	 * Attempts to follow any of the other known groups
	 */
	private void findGroupToFollow(Node currentNode, String groupName) {
		ArrayList<GroupDescriptor> groups = new ArrayList<GroupDescriptor>(knownGroups.get(groupName));
		if (groups.isEmpty()) {
			// No suitable group found
			return;
		}
		// TODO select the closest group leader
		GroupDescriptor selectedGroup = groups.get(0);

		GroupCommand command = new GroupCommand
				(GroupCommand.JOIN_REQUEST, selectedGroup);
		
		GroupingMessage message = GroupingMessage.createJoinRequest(
						protocolId, currentNode, command);
		try {
			pushDownMessage(currentNode, selectedGroup.getLeader(), message);
		} catch(UndeliverableMessageException ex) {
			// The selected leader was unreachable
			// Remove the descriptor from the list and retry
			knownGroups.remove(groupName, selectedGroup);
			findGroupToFollow(currentNode, groupName);
		}
	}

	@Override
	public void nextCycle(Node currentNode, int pid) {
		// TODO load balancing
		// nodes should be connected to the ones nearby
	}
	

	@Override
	public GroupingMessage handlePushDownMessage(Node currentNode,
			Node recipient, BaseMessage content) {
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
			handleGroupDescriptorChanged(
					currentNode, (GroupDescriptor) message.getContent());
			return null;
		}
		
		if (GroupingMessage.DELETE_DESCRIPTOR.equals(head)) {
			handleGroupDescriptorDeleted(
					currentNode, (GroupDescriptor) message.getContent());
			return null;
		}
		
		if (GroupingMessage.GROUP_COMMAND.equals(message.getHead())) {
			handleGroupCommand(currentNode, sender, (GroupCommand)message.getContent());
			return null;
		}
		
		/*
		if (GroupingMessage.REPLACELEADER_REQUEST.equals(head)) {
			//leader is the local node
			handleLeaderReplacement(
					currentNode,
					(String) message.getContent());
			return null;
		}*/
		
		if (GroupingMessage.WRAP.equals(head)) {
			return (BaseMessage) message.getContent();
		}
		return null;
	}
	
	
	private void handleGroupCommand(Node currentNode, Node sender, GroupCommand command) {

		String commandName = command.getCommand();
		
		if (GroupCommand.JOIN_REQUEST.equals(commandName)) {
			String groupName = (String) command.getContent();
			handleJoinRequest(currentNode, sender, groupName);
			return;
		} else if (GroupCommand.JOIN_REQUEST_RESPONSE.equals(commandName)) {
			handleJoinRequestAck(
					currentNode, sender,
					(GroupDescriptor) command.getContent());
		}
	}

	
/*
	private void handleLeaderReplacement(Node currentNode, String groupName) {
		GroupManager manager = getOrCreateManager(currentNode, groupName);
		//the current node is a follower of the groupName but now he should become
		//the leader, since the leader is dead
		GroupDescriptor groupDescriptor = manager.getFollowedGroup();
		groupDescriptor.setLeader(currentNode);
		manager.setLeadedGroup(groupDescriptor);
		//no need to send ack, since we update the groupdescriptor!
		broadcastGroupCreatedOrChanged(currentNode, groupDescriptor);
	}
*/
	// TODO use the routing
	@Deprecated
	public void handleNeighbourLost(Node currentNode, Node lostNode) {
		//current node checks all its known groups for the lost node
		//get its manager for each and check if it is following them or leading them
		
		for(String groupName: managers.keySet()){
			GroupManager manager = managers.get(groupName);
			GroupDescriptor leadedGroup = manager.getLeadedGroup();
			if (leadedGroup != null) {
				if (leadedGroup.isFollower(lostNode)) {
					leadedGroup.removeFollower(lostNode);
			 		broadcastGroupCreatedOrChanged(currentNode, leadedGroup);
				}
			}
			
			GroupDescriptor followedGroup = manager.getLeadedGroup();
			if (followedGroup != null) {
				if (followedGroup.isFollower(lostNode)) {
					// The leader will do
				} else {
					ArrayList<Node> otherFollowers = new ArrayList<Node>(followedGroup.getFollowers());
					otherFollowers.remove(currentNode);
					followSibling(currentNode, followedGroup, otherFollowers);
				}
			}
		}
	}

	/**
	 * Selects and join a sibling of a given group.
	 * This is useful when a leader has collapsed.
	 */
    private void followSibling(Node currentNode, GroupDescriptor followedGroup,
			List<Node> otherFollowers) {
    	
    	String groupName = followedGroup.getName();
    	if (otherFollowers.isEmpty()) {
    		knownGroups.remove(groupName, followedGroup);
    		managers.get(groupName).setFollowedGroup(null);
    		findGroupToFollow(currentNode, groupName);
    	}
		// TODO Among the various nodes selects the closet
		Node selected = otherFollowers.get(0);
		// the node ask a leader to join him
		GroupCommand command = new GroupCommand
				(GroupCommand.JOIN_REQUEST, followedGroup);
		
		GroupingMessage message = GroupingMessage.createJoinRequest(
						protocolId, currentNode, command);
		try {
			pushDownMessage(currentNode, selected, message);
		} catch(UndeliverableMessageException ex) {
			// The selected leader was unreachable
			// Remove the descriptor from the list and retry
			knownGroups.remove(groupName, followedGroup);
			joinOrCreateGroup(currentNode, groupName);
		}
	}
	
	/*
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
	 */

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
	protected void handleUnreliableRecipientException(Node currentNode,
			UndeliverableMessageException ex)
			throws UndeliverableMessageException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void handleForwardedUnreliableRecipientException(
			Node currentNode, UndeliverableMessageException ex)
			throws UndeliverableMessageException {
		// TODO Auto-generated method stub
		
	}

	


}
