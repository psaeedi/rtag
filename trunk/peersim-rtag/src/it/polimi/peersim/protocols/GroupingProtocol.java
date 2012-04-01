/**
 * 
 */
package it.polimi.peersim.protocols;

import it.polimi.peersim.messages.BaseMessage;
import it.polimi.peersim.messages.GroupingMessage;
import it.polimi.peersim.prtag.AppGroupManager;
import it.polimi.peersim.prtag.GroupDescriptor;

import java.io.Serializable;
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
		if (groupManager.getFollowedGroup() != null) {
			throw new AssertionError(
					"This node is already following a group called: " + name);
		}
		
		// if no group exist with that name create a new one and
		// broadcast to everyone
		if (knownGroups.get(name).isEmpty()) {
			GroupDescriptor groupDescriptor = new GroupDescriptor(
					UUID.randomUUID(), name, currentNode); 
			groupManager.setFollowedGroup(groupDescriptor);
		    broadcastGroupCreatedOrChanged(currentNode, groupDescriptor);
		} else {
			// if a known group exist ask to join
			Set<GroupDescriptor> descriptors = knownGroups.get(name);
			Node leader = null;
			for(GroupDescriptor descriptor: descriptors){
		       leader = descriptor.getLeader();
		       // TODO (optional) use the routing protocol to find the closer leader
		       if (leader != null) {
		    	   break;
		       }
			}
			if (leader == null) {
				throw new AssertionError(
						"If leader is null why we have a groupdescriptor");
			}
		    askLeaderToJoin(currentNode, leader, name);
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
	
	public void askLeaderToJoin(Node currentNode, Node leader, String groupName) {
		// the node ask a leader to join him
		GroupingMessage message = GroupingMessage.createJoinRequest(
				protocolId, currentNode, groupName);
		pushDownMessage(currentNode, leader, message);
	}
	
	public void handleJoinRequest(Node currentNode, Node follower, String groupName) {
		// a node asked this leader to join him
		// TODO add to the leaded group of that group and get the updated group descriptor
		// TODO send the updated group descriptor to all the other followers
		AppGroupManager manager = getOrCreateManager(currentNode, groupName);
		GroupDescriptor groupDescriptor = manager.getOrCreateLeadedGroup();
		groupDescriptor.addFollower(currentNode);
		
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
  		//if there is no manager, we should create one
  		
		
		// If it is a groupleader update the descriptor
		if(currentNode == groupLeader){
			manager.setLeadedGroup(remotegroupDescriptor);
			//leader should inform its followers of the updated groupDescriptor.
			/*for(Node followers: groupmanager.getLeadedGroup().getFollowers()){
				GroupingProtocol protocol = (GroupingProtocol)followers.getProtocol(groupProtocolId);
				protocol.groupmanager.setFollowedGroup(remotegroupDescriptor);
				protocol.
			}*/
		}
		
		if(groupFollowers.contains(currentNode)){
			manager.setFollowedGroup(remotegroupDescriptor);
			knownGroups.put(groupName, remotegroupDescriptor);
			System.out.println("*@*@*@*@*@ node"+currentNode.getID() );
		} else {
			knownGroups.put(groupName, remotegroupDescriptor);
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
		
		if (GroupingMessage.JOIN_REQUEST.equals(head)) {
			//leader is the local node
			handleJoinRequest(
					currentNode,
					message.getSender(),
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

}
