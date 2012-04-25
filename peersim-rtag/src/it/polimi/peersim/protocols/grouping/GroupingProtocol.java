/**
 * 
 */
package it.polimi.peersim.protocols.grouping;

import it.polimi.peersim.messages.BaseMessage;
import it.polimi.peersim.protocols.ForwardingProtocol;
import it.polimi.peersim.protocols.UniverseProtocol;
import it.polimi.peersim.prtag.UndeliverableMessageException;

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
		System.out.println("GROUP:: " + currentNode.getID() + " <- " + 
				message.getSender().getID() + 
				" message " + head);
		
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
	public void nextCycle(Node arg0, int arg1) {
		cleanExpiredBeacons();
		broadcastBeacons();
		cleanExpiredJoinRequests();
	}
	
	private void cleanExpiredJoinRequests() {
		// Check all the group manager for pending join requests which have not ben answered.
		
		System.err.println(
				"GroupingProtocol.cleanExpiredJoinRequests " +
				"not implemented yet.");
	}

	private void broadcastBeacons() {
		System.err.println(
				"GroupingProtocol.broadcastBeacons " +
				"not implemented yet.");
	}

	private void cleanExpiredBeacons() {
		System.err.println(
				"GroupingProtocol.cleanExpiredBeacons " +
				"not implemented yet.");
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
		}
	}
	
	private void handleJoinResponseNo(Node currentNode, Node sender, String groupName) {
		GroupManager manager = managers.get(groupName);
		manager.setLeaderBeingJoined(null, -1);
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

	private void handleJoinResponseYes(Node currentNode, Node sender,
			GroupDescriptor descriptor) {
		GroupManager manager = getOrCreateManager(currentNode, descriptor.getName());
		if (manager.getFollowedGroup() != null) {
			throw new AssertionError(
					"Followed group should be null when asking to join: " +
					manager.getFollowedGroup());
		}
		manager.setFollowedGroup(descriptor);
		GroupDescriptor leadedGroup = manager.getLeadedGroup();
		if (leadedGroup != null) {
			leadedGroup.setParentLeader(descriptor.getLeader());
			pushUpdatedDescriptorToFollowers(currentNode, leadedGroup);
		}
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
			if (leadedGroup != null
					&& !leadedGroup.isFollower(sender)) {
				
				leadedGroup.addFollower(sender);
				
				GroupingMessage message = GroupingMessage.createGroupCommand(
						protocolId, currentNode,
						GroupCommand.createJoinResponseYes(leadedGroup));
				try {
					pushDownMessage(currentNode, sender, message);
					pushUpdatedDescriptorToFollowers(currentNode, leadedGroup);
				} catch (UndeliverableMessageException e) {
					// Nothing to be done
				}
			}
		}
	}

	private void pushUpdatedDescriptorToFollowers(
			Node currentNode, GroupDescriptor leadedGroup) {
		System.err.println(
				"GroupingProtocol.pushUpdatedDescriptorToFollowers " +
				"not implemented yet.");
	}

	private void handleAddOrUpdateBeacon(
			Node currentNode, GroupBeacon beacon) {
		String groupName = beacon.getGroupName();
		Node remoteLeader = beacon.getLeader();
		
		if (remoteLeader.getID() == currentNode.getID()) {
			// This is an echo
			return;
		}
		
		beacons.put(beacon.getGroupName(), beacon);
		
		if (currentNode.getID() < remoteLeader.getID()) {
			return;
		}
		
		GroupManager manager = managers.get(groupName);
		if (manager != null) {
			GroupDescriptor leadedGroup = manager.getLeadedGroup();
			GroupDescriptor followedGroup = manager.getFollowedGroup();
			if (followedGroup == null && leadedGroup != null
					&& !leadedGroup.isFollower(remoteLeader)) {
				try {
					pushJoinRequest(currentNode, remoteLeader, groupName);
				} catch (UndeliverableMessageException e) {
					// Nothing to be done
				}
			}
		}
	}

	private void pushJoinRequest(Node currentNode, Node remoteLeader,
			String groupName) throws UndeliverableMessageException {
		GroupManager manager = managers.get(groupName);
		if (manager.getLeaderBeingJoined() != null) {
			// already joining a leader
			return;
		}
		
		GroupingMessage message = GroupingMessage.createGroupCommand(protocolId, currentNode,
				GroupCommand.createJoinRequest(groupName));
		pushDownMessage(currentNode, remoteLeader, message);
		manager.setLeaderBeingJoined(remoteLeader, CDState.getCycle());
	}

	private void handlePrematurelyDiscardedBeacon(
			Node currentNode, GroupBeacon beacon) {
		beacons.remove(beacon.getGroupName(), beacon);
	}
}
