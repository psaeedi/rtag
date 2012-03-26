/**
 * 
 */
package it.polimi.peersim.protocols;

import it.polimi.peersim.prtag.AppGroupManager;
import it.polimi.peersim.prtag.GroupDescriptor;
import it.polimi.peersim.prtag.GroupMessage;
import it.polimi.peersim.prtag.LocalUniverseDescriptor;
import it.polimi.peersim.prtag.TupleMessage;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 */
public class AppGroupProtocol implements Transport, EDProtocol, CDProtocol {
	
	
	private static final String UNIVERSE_PROTOCOL = "universe_protocol";
	private static int universeProtocolId;
	
	private static final String APPGROUP_PROTOCOL = "appgroup_protocol";
	private static int appGroupProtocolId;
	
	
	private Node localNode;
	
	// All the managers of this node
	private HashMap<String, AppGroupManager> managers = new HashMap<String, AppGroupManager>();
	
	// All the known group descriptors by group name
	private HashMultimap<String, GroupDescriptor> knownGroups = 
			HashMultimap.create();
	
	
	private HashMultimap<Node, TupleMessage> nodeMessages = 
			HashMultimap.create();
	
	private HashMultimap<GroupDescriptor, TupleMessage> groupMessages = 
			HashMultimap.create();
	
	private HashMultimap<String, TupleMessage> hierarchyMessages = 
			HashMultimap.create();
	
	public AppGroupProtocol(String prefix) {
		universeProtocolId = Configuration.getPid(
				prefix + "." + UNIVERSE_PROTOCOL);
		appGroupProtocolId = Configuration.getPid(
				prefix + "." + APPGROUP_PROTOCOL);
	}
	
	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	public void removeExpiredMessages() {
		// remove all the messages whose timestamp is expired
	}
	
	public void removeGroupForLostNodes() {
		// ???????????????????
		// TODO remove groups for a lost node
	}

	public void joinOrCreateGroup(String name) {
		// Create a new manager if it does not exist
		// TODO if no group exist with that name create a new one and
		// send a broadcast with a group created message - broadcast the group descriptor
		//broadcastGroupCreatedOrChanged
		
		// if a known group exist ask to join
		// TODO (optional) use the routing protocol to find the closer leader
		//askLeaderToJoin
	}
	
	public void broadcastGroupCreatedOrChanged(GroupDescriptor descriptor) {
		// Use the  universe protocol to send the changes
	}
	
	public void broadcastGroupDeleted(GroupDescriptor descriptor) {
		// Use the  universe protocol to send the changes
	}
	
	public void askLeaderToJoin(String friendlyName) {
		// the node ask a leader to join him
	}
	
	public void handleJoinRequest(String friendlyName) {
		// a node asked this leader to join him
	}

	public void handleJoinRequestAck(String friendlyName) {
		// the leader has responded
	}
	
	@Override
	public void processEvent(Node node, int pid, Object event) {
		
		GroupMessage message = (GroupMessage) event;
		System.out.println(node.getID() + "->" + localNode.getID() + " message " + ((GroupMessage) event).getHead());
		
		if (GroupMessage.UPDATE_DESCRIPTOR.equals(message.getHead())) {
			handleLocalGroupDescriptorChanged((GroupDescriptor)message.getBody());
			return;
		}else {
			throw new AssertionError(
					"The universe protocol should only receive UniverseMessages");
		}
		
		
	}

	private void handleLocalGroupDescriptorChanged(GroupDescriptor groupDescriptor) {
		Node universeLeader = groupDescriptor.getLeader();
		//TODO leader should inform its followers of the updated groupdescriptor.
		
	}

	@Override
	public long getLatency(Node arg0, Node arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void send(Node src, Node dest, Object msg, int pid) {
		getLatency(src, dest);
		AppGroupProtocol remoteProtocol = (AppGroupProtocol) 
				dest.getProtocol(pid);
		remoteProtocol.processEvent(src, pid, msg);
	}

	@Override
	public void nextCycle(Node arg0, int arg1) {
		// TODO Load balancing
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
