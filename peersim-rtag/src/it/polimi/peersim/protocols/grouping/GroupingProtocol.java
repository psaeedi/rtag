/**
 * 
 */
package it.polimi.peersim.protocols.grouping;

import it.polimi.peersim.messages.BaseMessage;
import it.polimi.peersim.protocols.ForwardingProtocol;
import it.polimi.peersim.protocols.UniverseProtocol;
import it.polimi.peersim.prtag.UndeliverableMessageException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.HashMultimap;

import peersim.cdsim.CDProtocol;
import peersim.cdsim.CDState;
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
	
	private static final String BROADCAST_BEACON = "broadcast_beacon";
	protected final int broadcastBeacon;
	
	private static final String LAST_CYCLE = "last_cycle";
	protected final int lastCycle;
	
	private static final String GROUP_MAX_SIZE = "group_max_size";
	protected final int groupMaxSize;
	private static final String GROUP_MIN_SIZE = "group_min_size";
	protected final int groupMinSize;
	
	private static final String JOIN_TIMEOUT = "join_time_out";
	protected final int joinTimeout;
	
	private static final String LOAD_BALANCE_CYCLE = "load_balance_cycle";
	protected final int loadBalanceCycle;
	
	// All the managers of this node
	private HashMap<String, GroupManager> managers = new 
			HashMap<String, GroupManager>();
	
	// All the known group descriptors by group name
	private HashMultimap<String, GroupBeacon> beacons = 
			HashMultimap.create();
	
	public GroupingProtocol(String prefix) {
		super(prefix);
		universeProtocolId = Configuration.getPid(
				prefix + "." + UNIVERSE_PROTOCOL);
		broadcastBeacon = Configuration.getInt(
				prefix + "." + BROADCAST_BEACON, 10);
		lastCycle = Configuration.getInt(
				prefix + "." + LAST_CYCLE, 10);
		groupMaxSize = Configuration.getInt(
				prefix + "." + GROUP_MAX_SIZE, 10);
		groupMinSize = Configuration.getInt(
				prefix + "." + GROUP_MIN_SIZE, 5);
		joinTimeout = Configuration.getInt(
				prefix + "." + JOIN_TIMEOUT, 30);
		loadBalanceCycle = Configuration.getInt(
				prefix + "." +  LOAD_BALANCE_CYCLE, 50);
    }
	
	@Override
	public Object clone() {
		GroupingProtocol clone = null;
		clone = (GroupingProtocol) super.clone();
		clone.managers = new HashMap<String, GroupManager>(managers);
		clone.beacons = HashMultimap.create(beacons);
		return clone;
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
		if (GroupingMessage.GROUP_COMMAND.equals(head)) {
			handleGroupCommand(currentNode, sender,
					(GroupCommand)message.getContent());
			return null;
		}
		
		if (GroupingMessage.WRAP.equals(head)) {
			return (BaseMessage) message.getContent();
		}
		return null;
	}

	@Override
	protected void handleUnreliableRecipientException(Node currentNode,
			UndeliverableMessageException ex)
			throws UndeliverableMessageException {
		System.err.println(
				"GroupingProtocol.handleUnreliableRecipientException " +
				"not implemented yet.");
	}

	@Override
	protected void handleForwardedUnreliableRecipientException(
			Node currentNode, UndeliverableMessageException ex)
			throws UndeliverableMessageException {
		System.err.println(
				"GroupingProtocol.handleForwardedUnreliableRecipientException " +
				"not implemented yet.");
	}

	@Override
	public void nextCycle(Node currentNode, int pid) {
		int currentCycle = CDState.getCycle();
		
		cleanExpiredBeacons();
		followGroupsIfNotFollowing(currentNode);
		
		if (currentCycle % broadcastBeacon == 0) {
			broadcastBeacons(currentNode);
		}
		cleanExpiredJoinRequests();
		
		if (currentCycle % 10 == 0){
			handleCongestedLeaders(currentNode);
		}
		
		if (currentCycle % 20 == 0){
			handleUnderpopulatedGroups(currentNode);
		}
		
		if (currentCycle % 2 == 0){
			pushUpdatedDescriptors(currentNode);
		}
		
		// Just for debug
		if (currentCycle == lastCycle) {
			for (String name: managers.keySet()) {
				GroupManager manager = managers.get(name);
				System.out.println("Node " + currentNode.getID() + " L " + manager.getLeadedGroup());
				System.out.println("Node " + currentNode.getID() + " F " + manager.getFollowedGroup());
			}
		}
	}
	 
	private void pushUpdatedDescriptors(Node currentNode) {
		for (String groupName: managers.keySet()) {
			GroupManager manager = managers.get(groupName);
			if (manager.isLeadedGroupChanged()) {
				pushUpdatedDescriptor(currentNode, manager.getLeadedGroup());
				manager.setLeadedGroupChanged(false);
			}
		}
	}

	/**
	 * For each group check if it is underpopulated. 
	 */
	private void handleUnderpopulatedGroups(Node currentNode) {
		for (String groupName: managers.keySet()) {
			GroupManager manager = managers.get(groupName);
			if (manager.getLeaderBeingJoined() != null) {
				// already joining a new leader
				System.out.println(currentNode.getID() + " Already joining a leader " + manager.getLeaderBeingJoined().getID());
				continue;
			}
			GroupDescriptor followedGroup = manager.getFollowedGroup();
			if (followedGroup == null) {
				continue;
			}
			if (followedGroup.getFollowers().size() < groupMinSize) {
				handleUnderpopulatedGroup(currentNode, followedGroup);
			}
		}
	}
	
	
	private void handleUnderpopulatedGroup(Node currentNode,
			GroupDescriptor followedGroup) {
		Node parentLeader = followedGroup.getParentLeader();
		String groupName = followedGroup.getName();
		if (parentLeader == null) {
			return;
		}
		try {
			System.out.println("handleUnderpopulatedGroup: " + currentNode.getID() +
					" from: " + followedGroup.getLeader().getID() +
					" to: " + parentLeader.getID());
			pushJoinRequest(currentNode, parentLeader, groupName);
		} catch (UndeliverableMessageException e) {
			// TODO Shall we retry?
			e.printStackTrace();
		}
	}

	/**
	 * For each group check if the leader is congested. 
	 */
	private void handleCongestedLeaders(Node currentNode) {
		for (String groupName: managers.keySet()) {
			GroupManager manager = managers.get(groupName);
			if (manager.getLeaderBeingJoined() != null) {
				// already joining a new leader
				continue;
			}
			GroupDescriptor followedGroup = manager.getFollowedGroup();
			if (followedGroup == null) {
				continue;
			}
			if (followedGroup.getFollowers().size() > groupMaxSize) {
				handleCongestedLeader(currentNode, followedGroup);
			}
		}
	}

	private void handleCongestedLeader(Node currentNode,
			GroupDescriptor followedGroup) {
		
		int nodePositionInGroup = followedGroup.getFollowers().indexOf(currentNode);
		if (nodePositionInGroup < 0) {
			throw new AssertionError("Node not in group?");
		}
		if (nodePositionInGroup < (groupMaxSize / 2)) {
			return;
		}
		// Equally distribute the exceeding nodes between the ones who will stay
		Node newLeader = followedGroup.getFollowers().get(nodePositionInGroup % (groupMaxSize / 2));
		try {
			System.out.println("handleCongestedLeader: " + currentNode.getID() +
					" from: " + followedGroup.getLeader().getID() +
					" to: " + newLeader.getID());
			pushJoinRequest(currentNode, newLeader, followedGroup.getName());
		} catch (UndeliverableMessageException e) {
			// TODO Shall we retry?
			e.printStackTrace();
		}
	}

	public boolean isInGroup(String groupName) {
		return managers.containsKey(groupName);
	}
	
	private void cleanExpiredJoinRequests() {
		// Check all the group manager for pending join
		// requests which have not been answered.		
		int currentCycle = CDState.getCycle();
		for (String groupName: managers.keySet()) {
			GroupManager manager = managers.get(groupName);
			if (manager.getLeaderBeingJoined() == null) {
				return;
			}
			if (manager.getLeaderBeingJoinedCycle() + joinTimeout < currentCycle) {
				manager.resetLeaderBeingJoinedCycle();
			}
		}
	}

	private void broadcastBeacons(Node currentNode) {
		for (String groupName: managers.keySet()) {
			GroupManager manager = managers.get(groupName);
			GroupDescriptor leadedGroup = manager.getLeadedGroup();
			if (leadedGroup == null) {
				continue;
			}
			GroupBeacon beacon = new GroupBeacon(groupName, currentNode, CDState.getCycle() + broadcastBeacon);
			broadcastBeacon(currentNode, beacon);
		}
	}

	private void cleanExpiredBeacons() {
		int currentCycle = CDState.getCycle();
		HashMap<String, ArrayList<GroupBeacon>> beaconsToRemove = new HashMap<String, ArrayList<GroupBeacon>>();
		for (String groupName: beacons.keys()) {
			ArrayList<GroupBeacon> toRemove = new ArrayList<GroupBeacon>();
			Set<GroupBeacon> beaconSet = beacons.get(groupName);
			for (GroupBeacon b: beaconSet) {
				if (b.getExpireCycle() >= currentCycle) {
					toRemove.add(b);
				}
			}
			beaconsToRemove.put(groupName, toRemove);
		}
		for (String key: beaconsToRemove.keySet()) {
			for (GroupBeacon b: beaconsToRemove.get(key)) {
				beacons.remove(key, b);
			}
		}

	}

	public void joinOrCreateGroup(Node currentNode, String groupName) {
		if (managers.get(groupName) != null) {
			System.err.println("Already in group: " + groupName + ". Aborting...");
			return;
		}
		
		// If no group exist with that name or 
		// if it does u don't know it yet
		// create a new one and
		// broadcast to everyone
		Set<GroupBeacon> beaconsForGroup = beacons.get(groupName);
		if (beaconsForGroup.isEmpty()) {
			System.out.println("---------------Group::[Node " + currentNode.getID() + 
					"] [creating group " + groupName+"]" + " has no known groups");
			// No groups with that name exists in the network
			// so it creates a groupdescriptor
			GroupDescriptor groupDescriptor = new GroupDescriptor(
					UUID.randomUUID(), groupName, currentNode); 	
			GroupBeacon beacon = new GroupBeacon(groupName, currentNode, CDState.getCycle());
			beacons.put(groupName, beacon);
			
			GroupManager groupManager = getOrCreateManager(currentNode, groupName);
			groupManager.setLeadedGroup(groupDescriptor);
			System.out.println("*!*!*!*!Group::[Node " + currentNode.getID() + 
					"] [creating group " + groupName+"]"+"setleaded group"+ groupManager.
					getLeadedGroup().getLeader().getID());
		    broadcastBeacon(currentNode, beacon);
		 } else {
			 // if a known group exist ask to join
			 
			 // Create an empty group
			 GroupManager groupManager = getOrCreateManager(currentNode, groupName);
			 Iterator<GroupBeacon> iterator = beaconsForGroup.iterator();
			 do {
				 GroupBeacon beacon = iterator.next();
				 try {
					pushJoinRequest(currentNode, beacon.getLeader(), beacon.getGroupName());
					break;
				 } catch (UndeliverableMessageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				 }
			 } while (iterator.hasNext());
		}
	}

	private void broadcastBeacon(Node currentNode, GroupBeacon beacon) {
		GroupingMessage message = GroupingMessage.createGroupCommand(
				protocolId, currentNode,
				GroupCommand.createAddOrUpdateBeacon(beacon));
		UniverseProtocol protocol = (UniverseProtocol)currentNode.getProtocol(
				universeProtocolId);
		protocol.sendBroadCast(currentNode, message);
	}

	private GroupManager getOrCreateManager(Node currentNode, String groupName) {
		if(!managers.containsKey(groupName)){
			GroupManager groupmanager = 
					new GroupManager(groupName, currentNode);
		   managers.put(groupName, groupmanager);
		   return groupmanager;
		}
		return managers.get(groupName);
	}
	
	private void handleGroupCommand(Node currentNode, Node sender,
			GroupCommand command) {
		String commandName = command.getCommand();
		if (GroupCommand.ADD_OR_UPDATE_BEACON.equals(commandName)) {
			handleAddOrUpdateBeacon(currentNode,
					(GroupBeacon)command.getContent());
		} else if (GroupCommand.PREMATURALY_DISCARD_BEACON.equals(commandName)) {
			handlePrematurelyDiscardedBeacon(currentNode,
					(GroupBeacon)command.getContent());
		} else if (GroupCommand.JOIN_REQUEST.equals(commandName)) {
			handleJoinRequest(currentNode, sender,
					(String)command.getContent());
		}  else if (GroupCommand.JOIN_RESPONSE_YES.equals(commandName)) {
			handleJoinResponseYes(currentNode, sender,
					(GroupDescriptor)command.getContent());
		}  else if (GroupCommand.JOIN_RESPONSE_NO.equals(commandName)) {
			handleJoinResponseNo(currentNode, sender,
					(String)command.getContent());				
		}  else if (GroupCommand.NOTIFY_LEAVE.equals(commandName)) {
			handleNotifyLeave(currentNode, sender,
					(String)command.getContent());			
		} else if (GroupCommand.UPDATE_DESCRIPTOR.equals(commandName)) {
			handleUpdateDescriptor(currentNode,
					(GroupDescriptor)command.getContent());
		} else if (GroupCommand.DELETE_DESCRIPTOR.equals(commandName)) {
			handleDeleteDescriptor(currentNode,
					(GroupDescriptor)command.getContent());
		}
		
	}
	

	private void handleDeleteDescriptor(Node currentNode,
			GroupDescriptor descriptor) {
		GroupManager manager = managers.get(descriptor.getName());
		Node remoteLeader = descriptor.getLeader();
		
		if (manager.getFollowedGroup().getLeader().getID() != remoteLeader.getID()) {
			System.err.println("Wrong follower leader!");
			return;
		}
		// DO not send leave notify
		//manager.setFollowedGroup(currentNode, null, false);
		System.err.println("Not implemented yet");
	}

	private void handleUpdateDescriptor(Node currentNode,
			GroupDescriptor descriptor) {
		GroupManager manager = managers.get(descriptor.getName());
		Node remoteLeader = descriptor.getLeader();
		
		if (manager.getFollowedGroup() == null) {
			System.err.println(currentNode.getID() + 
					" not following: " + descriptor);
			return;
		} else if (manager.getFollowedGroup().getLeader().getID() != remoteLeader.getID()) {
			System.err.println(currentNode.getID() + " following: " + 
					manager.getFollowedGroup().getLeader().getID() +
					" wrong follower leader for update: " + descriptor);
			return;
		}
		
		Node parentLeader = descriptor.getParentLeader();
		if (parentLeader != null && currentNode.getID() == parentLeader.getID()) {
			// The current node is both follower and parent leader.
			pushLeaveNotify(currentNode, remoteLeader, descriptor.getName());
			manager.setFollowedGroup(currentNode, null);
		} else {
			manager.setFollowedGroup(currentNode, descriptor);
		}
		
	}

	private void handleNotifyLeave(Node currentNode, Node sender, String groupName) {
		GroupManager manager = managers.get(groupName);
		if (manager == null) {
			System.err.println("handleNotifyLeave -> Manager null for group: " + groupName);
			return;
		}
		GroupDescriptor leadedGroup = manager.getLeadedGroup();
		if (leadedGroup == null) {
			System.err.println("handleNotifyLeave: leaded group is null");
			return;
		}
		leadedGroup.removeFollower(sender);
		if (!leadedGroup.getFollowers().isEmpty()) {
			manager.setLeadedGroupChanged(true);
		} else {
			// become a pure follower
			if (manager.getFollowedGroup() != null) {
				manager.setLeadedGroup(null);
			}
		}
	}

	@Deprecated
	private Node getFollowerWithNoLeadedGroup(Node currentNode, String groupName) {
		Node followerWithLeadedGroup = null;
		GroupManager manager = managers.get(groupName);
		if(manager.getFollowedGroup()!=null){
			for (Node follower: manager.getFollowedGroup().getFollowers()) {
				if(follower!=currentNode){
				followerWithLeadedGroup = follower;
				}
				if(!beacons.containsEntry(groupName, follower) && currentNode!=follower)
				return follower;
			}
			if(followerWithLeadedGroup!=currentNode){	
			  return followerWithLeadedGroup;
			  }
		}
		
		else if(manager.getFollowedGroup()==null){
			throw new RuntimeException("No other follower Why its group was congested then!");
		}
		return followerWithLeadedGroup;
	}

	private void handleJoinResponseNo(Node currentNode, Node sender, String groupName) {
		GroupManager manager = managers.get(groupName);
		manager.resetLeaderBeingJoinedCycle();
		if (manager.getFollowedGroup() != null) {
			return;
		}
		Set<GroupBeacon> beaconsForGroup = beacons.get(groupName);
		Iterator<GroupBeacon> iterator = beaconsForGroup.iterator();
		do {
			GroupBeacon beacon = iterator.next();
			if (beacon.getLeader().getID() == sender.getID()) {
				continue;
			}
			try {
				pushJoinRequest(currentNode, beacon.getLeader(), beacon.getGroupName());
				break;
			} catch (UndeliverableMessageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (iterator.hasNext());
	}

	private void pushLeaveNotify(Node currentNode, Node leader,
			String groupName) {
		GroupingMessage message = GroupingMessage.createGroupCommand(
				protocolId, currentNode,
				GroupCommand.createLeaveNotify(groupName));
		try {
			pushDownMessage(currentNode, leader, message);
		} catch (UndeliverableMessageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void handleJoinResponseYes(Node currentNode, Node sender,
			GroupDescriptor descriptor) {
		GroupManager manager = getOrCreateManager(currentNode, descriptor.getName());
		// If the request was timedout (the joined leader is different than the sender)
		// we must send a leave notice.
		Node leaderBeingJoined = manager.getLeaderBeingJoined();
		if (leaderBeingJoined != null && leaderBeingJoined.getID() != sender.getID()) {
			pushLeaveNotify(currentNode, sender, descriptor.getName());
			return;
		}
		
		manager.setFollowedGroup(currentNode, descriptor);
	}

	private void handleJoinRequest(Node currentNode, Node sender,
			String groupName) {
		GroupManager manager = managers.get(groupName);
		if (manager == null) {
			GroupingMessage message = GroupingMessage.createGroupCommand(
					protocolId, currentNode,
					GroupCommand.createJoinResponseNo(groupName));
			try {
				pushDownMessage(currentNode, sender, message);
			} catch (UndeliverableMessageException e) {
				// Nothing to be done
			}
		} else {
			GroupDescriptor leadedGroup = manager.getOrCreateLeadedGroup();
			if (!leadedGroup.isFollower(sender)) {
				leadedGroup.addFollower(sender);
				
				GroupingMessage message = GroupingMessage.createGroupCommand(
						protocolId, currentNode,
						GroupCommand.createJoinResponseYes(leadedGroup));
				try {
					pushDownMessage(currentNode, sender, message);
					manager.setLeadedGroupChanged(true);
				} catch (UndeliverableMessageException e) {
					leadedGroup.removeFollower(sender);
					// Nothing to be done
				}
			}
		}
	}

	private void pushUpdatedDescriptor(
			Node currentNode, GroupDescriptor leadedGroup) {
		
		for (Node followers: leadedGroup.getFollowers()) {
			GroupingMessage updateDescriptorMsg =
					GroupingMessage.createGroupCommand(protocolId, currentNode,
    						GroupCommand.createUpdateDescriptor(leadedGroup));
    		try {
    			pushDownMessage(currentNode, followers, updateDescriptorMsg);
    		} catch(UndeliverableMessageException ex) {
    			// TODO 
    		}
		}
		
	}

	private void handleAddOrUpdateBeacon(
			Node currentNode, GroupBeacon beacon) {
		String groupName = beacon.getGroupName();
		Node remoteLeader = beacon.getLeader();
		
		if (remoteLeader.getID() == currentNode.getID()) {
			// This is an echo
			return;
		}
		
		beacons.put(groupName, beacon);
	
		/*
		if (currentNode.getID() < remoteLeader.getID()) {
			return;
		}
		GroupManager manager = managers.get(groupName);
		if (manager != null) {
			GroupDescriptor leadedGroup = manager.getLeadedGroup();
			GroupDescriptor followedGroup = manager.getFollowedGroup();
			if (followedGroup == null && leadedGroup != null
					&& !leadedGroup.isFollower(remoteLeader)) {
				if (manager.getLeaderBeingJoined() != null) {
					return;
				}
				try {
					pushJoinRequest(currentNode, remoteLeader, groupName);
				} catch (UndeliverableMessageException e) {
					manager.resetLeaderBeingJoinedCycle();
					// Nothing to be done
				}
			}
		}*/
	}
	
	private void followGroupsIfNotFollowing(Node currentNode) {
		for (String groupName: managers.keySet()) {
			GroupManager manager = managers.get(groupName);
			if (manager.getLeaderBeingJoined() != null) {
				continue;
			}
			if (manager.getFollowedGroup() != null) {
				continue;
			}
			for (GroupBeacon beacon: beacons.get(groupName)) {
				Node remoteLeader = beacon.getLeader();
				/*if (currentNode.getID() > remoteLeader.getID()) {
					return;
				}*/
				if (remoteLeader.getID() == currentNode.getID()) {
					continue;
				}
				GroupDescriptor leadedGroup = manager.getLeadedGroup();
				if (leadedGroup != null
						&& leadedGroup.isFollower(remoteLeader)) {
					continue;
				}
				
				try {
					pushJoinRequest(currentNode, remoteLeader, groupName);
					break;
				} catch (UndeliverableMessageException e) {
					manager.resetLeaderBeingJoinedCycle();
					// Nothing to be done
					continue;
				}
			}
		}
	}

	private void pushJoinRequest(Node currentNode, Node remoteLeader,
			String groupName) throws UndeliverableMessageException {
		GroupManager manager = managers.get(groupName);
		if (manager.getLeaderBeingJoined() != null) {
			// already joining a leader
			throw new AssertionError("asd");
			/*System.err.println("pushJoinRequest -> " + currentNode.getID() + 
					" already joining leader: " + manager.getLeaderBeingJoined().getID() +
					" for group: " + groupName);
			return;*/
		}
		
		GroupingMessage message = GroupingMessage.createGroupCommand(
				protocolId, currentNode,
				GroupCommand.createJoinRequest(groupName));
		pushDownMessage(currentNode, remoteLeader, message);
		manager.setLeaderBeingJoined(remoteLeader, CDState.getCycle());
	}

	private void handlePrematurelyDiscardedBeacon(
			Node currentNode, GroupBeacon beacon) {
		beacons.remove(beacon.getGroupName(), beacon);
	}

	
	private class GroupManager {		
		private String name;
		private Node ownerNode;
		private GroupDescriptor followedGroup;
		private GroupDescriptor leadedGroup;
		
		private Node leaderBeingJoined;
		private int leaderBeingJoinedCycle = -1;
		
		private boolean leadedGroupChanged = false;
		
		public GroupManager(String name, Node ownerNode) {
			super();
			this.name = name;
			this.ownerNode = ownerNode;
		}

		public GroupDescriptor getFollowedGroup() {
			return followedGroup;
		}
		
		public void setFollowedGroup(Node currentNode, GroupDescriptor followedGroup) {
			if (this.followedGroup != null && !this.followedGroup.equals(followedGroup)) {
				pushLeaveNotify(currentNode, this.followedGroup.getLeader(),
						this.followedGroup.getName());
			}
			this.followedGroup = followedGroup;
			
			if (leadedGroup != null) {
				if (leadedGroup.getFollowers().isEmpty()) {
					// Become a pure follower
					setLeadedGroup(null);
				} else {
					Node parent = (this.followedGroup != null) ? this.followedGroup.getLeader() : null;
					Node oldParent = leadedGroup.getParentLeader();
					if ((parent == null && oldParent != null) ||
							(parent != null && oldParent == null) ||
							(parent != null && oldParent != null && 
									parent.getID() != oldParent.getID())) {
						leadedGroup.setParentLeader(parent);
						setLeadedGroupChanged(true);
					}
				}
			}
			
			if (this.followedGroup != null && this.leaderBeingJoined != null) {
				if (this.followedGroup.getLeader().getID() == this.leaderBeingJoined.getID()) {
					resetLeaderBeingJoinedCycle();
				}
			}
		}
		
		public GroupDescriptor getOrCreateLeadedGroup() {
			if (leadedGroup == null) {
				leadedGroup = new GroupDescriptor(UUID.randomUUID(), name, ownerNode);
				if (followedGroup != null) {
					leadedGroup.setParentLeader(followedGroup.getLeader());
				}
			}
			
			return leadedGroup;
		}

		public GroupDescriptor getLeadedGroup() {
			return leadedGroup;
		}
		
		public void setLeadedGroup(GroupDescriptor leadedGroup) {
			this.leadedGroup = leadedGroup;
			if (this.leadedGroup == null) {
				leadedGroupChanged = false;
			}
		}
		
		public Node getLeaderBeingJoined() {
			return leaderBeingJoined;
		}

		public void setLeaderBeingJoined(Node leaderBeingJoined, int cycle) {
			if (this.leaderBeingJoined != null) {
				throw new AssertionError("Already joining another leader.");
			}
			if (leaderBeingJoined == null) {
				throw new AssertionError("Cannot follow null leader.");
			}
			this.leaderBeingJoined = leaderBeingJoined;
			this.leaderBeingJoinedCycle = cycle;
		}

		public int getLeaderBeingJoinedCycle() {
			return leaderBeingJoinedCycle;
		}
		
		
		public void resetLeaderBeingJoinedCycle() {
			this.leaderBeingJoined = null;
			this.leaderBeingJoinedCycle = -1;
		}

		public boolean isLeadedGroupChanged() {
			return leadedGroupChanged;
		}

		public void setLeadedGroupChanged(boolean leadedGroupChanged) {
			this.leadedGroupChanged = leadedGroupChanged;
			if (leadedGroupChanged && this.leadedGroup == null) {
				throw new AssertionError("NUll not changed");
			}
		}
		
		
	}

}
