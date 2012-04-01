/**
 * 
 */
package it.polimi.peersim.protocols;

import it.polimi.peersim.prtag.AppGroupManager;
import it.polimi.peersim.prtag.BroadcastContent;
import it.polimi.peersim.prtag.GroupDescriptor;
import it.polimi.peersim.prtag.GroupMessage;
import it.polimi.peersim.prtag.LocalUniverseDescriptor;
import it.polimi.peersim.prtag.TupleMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.HashMultimap;

import peersim.cdsim.CDProtocol;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 */
public class GroupingProtocol implements Transport, EDProtocol, CDProtocol {
	
	
	private static final String UNIVERSE_PROTOCOL = "universe_protocol";
	private static int universeProtocolId;
	
	private static final String ROUTING_PROTOCOL = "routing_protocol";
	private static int routingProtocolId;
	
	private static final String GROUPING_PROTOCOL = "grouping_protocol";
	private static int groupProtocolId;
	
	
	private Node localNode;
	
	// All the managers of this node
	private HashMap<String, AppGroupManager> managers = new 
			HashMap<String, AppGroupManager>();
	
	// All the known group descriptors by group name
	private HashMultimap<String, GroupDescriptor> knownGroups = 
			HashMultimap.create();
	
	
	private HashMultimap<Node, TupleMessage> nodeMessages = 
			HashMultimap.create();
	
	private HashMultimap<GroupDescriptor, TupleMessage> groupMessages = 
			HashMultimap.create();
	
	private HashMultimap<String, TupleMessage> hierarchyMessages = 
			HashMultimap.create();
	
	private AppGroupManager groupmanager;
	
	public GroupingProtocol(String prefix) {
		universeProtocolId = Configuration.getPid(
				prefix + "." + UNIVERSE_PROTOCOL);
		routingProtocolId = Configuration.getPid(
				prefix + "." + ROUTING_PROTOCOL);
		groupProtocolId = Configuration.getPid(
				prefix + "." + GROUPING_PROTOCOL);
	}
	
	@Override
	public Object clone() {
		GroupingProtocol inp = null;
        try {
        	inp = (GroupingProtocol) super.clone();
        	inp.managers = new HashMap<String, AppGroupManager>(this.managers);
        } catch (CloneNotSupportedException e) {
        } // never happens
        return inp;
	}
	

	public void initialize(Node currentNode){//, DiscoveryProtocol discoveryProtocol) {
		localNode = currentNode;
	}
	

	public void removeExpiredMessages() {
		// remove all the messages whose timestamp is expired
	}
	
	public void removeGroupForLostNodes() {
		// ???????????????????
		// TODO remove groups for a lost node
	}
	
	public AppGroupManager getOrCreateManager(String groupName) {
		
		if(!managers.containsKey(groupName)){
		   groupmanager = new AppGroupManager(groupName);
		   managers.put(groupName, groupmanager);
		   return groupmanager;
		}
		
		return managers.get(groupName);
	}
	
	
	private GroupDescriptor createGroupDescriptor(String groupName) {
		if(knownGroups.get(groupName).isEmpty()) {
			//u make a new group 
			GroupDescriptor leadedGroupdesciptor = new GroupDescriptor(
					UUID.randomUUID(), groupName, localNode);
			knownGroups.put(groupName, leadedGroupdesciptor);
			groupmanager.setLeadedGroup(leadedGroupdesciptor);
			return leadedGroupdesciptor;
			}
		else {throw new AssertionError("it already has a groupdescriptor");}
	}

	public void joinOrCreateGroup(String name) {
		//immediately check u have a manager for that group
		groupmanager = getOrCreateManager(name);
		// TODO
		GroupDescriptor groupDescriptor = null;
		// if no group exist with that name create a new one and
		// broadcast to everyone
		if(knownGroups.get(name).isEmpty()){
			groupDescriptor = createGroupDescriptor(name); 
		    broadcastGroupCreatedOrChanged(groupDescriptor);
		}
		else{
			// if a known group exist ask to join
			Set<GroupDescriptor> descriptors = knownGroups.get(name);
			Node leader = null;
			for(GroupDescriptor descriptor: descriptors){
			   // TODO (optional) use the routing protocol to find the closer leader
		       leader = descriptor.getLeader();
			   }
			if(leader == null){
				throw new AssertionError("if leader is null why we have a groupdescriptor");
			}
		     askLeaderToJoin(leader, name);
		}
	}
	

	public void broadcastGroupCreatedOrChanged(GroupDescriptor descriptor) {
		// Use the  universe protocol to send the changes
		GroupMessage message = GroupMessage.createUpdateDescriptor(localNode, descriptor);	
		BroadcastContent content = new BroadcastContent(localNode, groupProtocolId, message);
		UniverseProtocol protocol = (UniverseProtocol)localNode.
				getProtocol(universeProtocolId);
		//System.out.println("gdb1"+message.getBody() );
		protocol.broadCast(content);
	}
	
	public void broadcastGroupDeleted(GroupDescriptor descriptor) {
		// Use the  universe protocol to send the changes
		GroupMessage message = GroupMessage.createDeleteDescriptor(localNode, descriptor);
		BroadcastContent content = new BroadcastContent(localNode, groupProtocolId, message);
		UniverseProtocol protocol = (UniverseProtocol)localNode.getProtocol(universeProtocolId);
		protocol.broadCast(content);
	}
	
	public void askLeaderToJoin(Node leader, String groupName) {
		// the node ask a leader to join him
		GroupMessage message = GroupMessage.createJoinRequest(localNode, groupName);
		RoutingProtocol protocol = (RoutingProtocol)localNode.getProtocol(routingProtocolId);
		protocol.send(localNode, leader, message, groupProtocolId);
	}
	
	public void handleJoinRequest(Node follower, String groupName) {
		// a node asked this leader to join him
		// TODO add to the leaded group of that group and get the updated group descriptor
		// TODO send the updated group descriptor to all the other followers
		GroupDescriptor groupDescriptor = null;
		//System.out.println("8888888888888888888888");
		//GroupDescriptor groupDescriptor = groupmanager.getLeadedGroup();
		broadcastGroupCreatedOrChanged(groupDescriptor);
		
		GroupMessage message = GroupMessage.createJoinRequestAck(localNode, groupDescriptor);
		RoutingProtocol protocol = (RoutingProtocol)localNode.getProtocol(routingProtocolId);
		protocol.send(localNode, follower, message, groupProtocolId);
	}

	public void handleJoinRequestAck(Node leader, GroupDescriptor groupDescriptor) {
		// the leader has responded
		// add to the group manager the leader
		groupmanager.setFollowedGroup(groupDescriptor);
		String groupName = groupDescriptor.getFriendlyName();
		knownGroups.put(groupName, groupDescriptor);
		
	}
	
	@Override
	public void processEvent(Node currentNode, int pid, Object event) {
		if (event instanceof GroupMessage) {
			GroupMessage message = (GroupMessage) event;
			System.out.println("GROUP:: " + localNode.getID() + " <- " + 
					message.getSender().getID() + 
					" message " + ((GroupMessage) event).getHead());
			
			if (GroupMessage.UPDATE_DESCRIPTOR.equals(message.getHead())) {
				System.out.println("UPDATE_DESCRIPTOR node " + message.getSender().getID());
				handleLocalGroupDescriptorChanged((GroupDescriptor) message.getBody());
				return;
			}
			
			if (GroupMessage.DELETE_DESCRIPTOR.equals(message.getHead())) {
				handleLocalGroupDescriptorChanged((GroupDescriptor) message.getBody());
				return;
			}
			
			if (GroupMessage.JOIN_REQUEST.equals(message.getHead())) {
				//leader is the local node
				getLatency(localNode, message.getSender());
				handleJoinRequest(message.getSender(),
						(String) message.getBody());
				return;
			}
			
			if (GroupMessage.JOIN_REQUEST_ACK.equals(message.getHead())) {
				System.out.println("gdh " + message.getHead());
				System.out.println("gdb " + message.getBody());
				getLatency(localNode, message.getSender());
				handleJoinRequestAck(message.getSender(),
						(GroupDescriptor) message.getBody());
				return;
			}
		} else {
			throw new AssertionError(
					"The universe protocol should only receive GroupMessage");
		}
	}

	private void handleLocalGroupDescriptorChanged(GroupDescriptor remotegroupDescriptor) {
		
		Node groupLeader = remotegroupDescriptor.getLeader();
		List<Node> groupFollowers = remotegroupDescriptor.getFollowers();
		String groupName = remotegroupDescriptor.getFriendlyName();
		// we check if we have the manager for that!?
		groupmanager = getOrCreateManager(groupName);
		//does it replace it ?!
		//update the descriptor
  		knownGroups.put(groupName, remotegroupDescriptor);
  		//if there is no manager, we should create one
  		
		
		// If it is a groupleader update the descriptor
		if(localNode == groupLeader){
			groupmanager.setLeadedGroup(remotegroupDescriptor);
			//leader should inform its followers of the updated groupDescriptor.
			/*for(Node followers: groupmanager.getLeadedGroup().getFollowers()){
				GroupingProtocol protocol = (GroupingProtocol)followers.getProtocol(groupProtocolId);
				protocol.groupmanager.setFollowedGroup(remotegroupDescriptor);
				protocol.
			}*/
		}
		
		if(groupFollowers.contains(localNode)){
			groupmanager.setFollowedGroup(remotegroupDescriptor);
			knownGroups.put(groupName, remotegroupDescriptor);
			System.out.println("*@*@*@*@*@ node"+localNode.getID() );
		}
		
		else{
			knownGroups.put(groupName, remotegroupDescriptor);
		}
	}

	@Override
	public long getLatency(Node arg0, Node arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void send(Node src, Node dest, Object msg, int pid) {
		getLatency(src, dest);
		GroupingProtocol remoteProtocol = (GroupingProtocol) 
				dest.getProtocol(pid);
		remoteProtocol.processEvent(src, pid, msg);
	}

	@Override
	public void nextCycle(Node arg0, int arg1) {
		//if (CDState.getCycle() < 5)
            //return;
		
		UniverseProtocol protocol = (UniverseProtocol)localNode.
				getProtocol(universeProtocolId);
		removeExpiredMessages();
		
		
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

}
