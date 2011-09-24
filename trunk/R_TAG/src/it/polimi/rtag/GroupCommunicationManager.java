/**
 */

package it.polimi.rtag;

import it.polimi.rtag.messaging.GroupCoordinationCommand;
import it.polimi.rtag.messaging.GroupCoordinationCommandAck;
import it.polimi.rtag.messaging.GroupFollowerCommand;
import it.polimi.rtag.messaging.GroupFollowerCommandAck;
import it.polimi.rtag.messaging.GroupLeaderCommand;
import it.polimi.rtag.messaging.GroupLeaderCommandAck;
import it.polimi.rtag.messaging.MessageSubjects;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

import lights.Tuple;

import polimi.reds.Message;
import polimi.reds.MessageID;
import polimi.reds.NodeDescriptor;
import polimi.reds.broker.overlay.NeighborhoodChangeListener;
import polimi.reds.broker.overlay.Overlay;

import static it.polimi.rtag.messaging.MessageSubjects.*;
import static it.polimi.rtag.messaging.GroupLeaderCommand.*;
import static it.polimi.rtag.messaging.GroupCoordinationCommand.*;

/**
 * Handles the communication of a group and also keep the group descriptor updated.
 * 
 * @see {@link NeighborhoodChangeListener, PacketListener}
 * 
 * @author Panteha Saeedi (saeedi@elet.polimi.it).
 */
public class GroupCommunicationManager implements NeighborhoodChangeListener, GroupDiscoveredNotificationListener {

	private Node node;
	
	private GroupDescriptor groupDescriptor;
	private NodeDescriptor currentNodeDescriptor;
	private Overlay overlay;
	
	private boolean leader;
	private HashMap<MessageID, Message> pendingMessages = new HashMap<MessageID, Message>();
	

	
	
	/**
	 * Create a new group and the current node becomes a leader.
	 * 
	 * @param node
	 * @param uniqueId
	 * @param friendlyName
	 * @param description
	 * @return
	 */
	public static GroupCommunicationManager createGroup(Node node, 
			 UUID uniqueId, String friendlyName, Tuple description) {
		
		GroupDescriptor groupDescriptor = new GroupDescriptor(uniqueId, 
				friendlyName, node.getID(), description);
		
		GroupCommunicationManager manager = new GroupCommunicationManager(
				node, groupDescriptor, node.getOverlay());
		return manager;
	}
	
	
	/**
	 * Wraps a given group for communicatin manager for the given node.
	 * 
	 * @param node
	 * @param groupDescriptor
	 * @return
	 */
	public static GroupCommunicationManager createGroup(Node node, 
			GroupDescriptor groupDescriptor) {
		
		GroupCommunicationManager manager = new GroupCommunicationManager(
				node, groupDescriptor, node.getOverlay());
		return manager;
	}
	
	public static GroupCommunicationManager createUniverse(Node node) {
		
		GroupDescriptor groupDescriptor = GroupDescriptor.createUniverse(node);
		
		GroupCommunicationManager manager = new GroupCommunicationManager(
				node, groupDescriptor, node.getOverlay());
		return manager;
	}
	
	/**
	 * @param groupDescriptor
	 * @param currentNodeDescriptor
	 * @param overlay
	 */
	public GroupCommunicationManager(
			Node node, 
			GroupDescriptor groupDescriptor,
			Overlay overlay) {
		this.node = node;
		this.groupDescriptor = groupDescriptor;
		this.currentNodeDescriptor = node.getID();
		this.setOverlay(overlay);
		leader = groupDescriptor.isLeader(currentNodeDescriptor);
	}
	
	
	/**
	 * Notify this group manager that a new node has been discovered. If the
	 * current node is a follower then nothing has to be done.
	 * If the node is a leader then it depends. If the new discovered node is
	 * already a group member (which can happen in case of internal delay)
	 * nothing is done. Otherwise if the new node is not a member of the group
	 * the leader will send it a {@link MessageSubjects#GROUP_DISCOVERED_NOTIFICATION}.
	 * 
	 * @see polimi.reds.broker.overlay.NeighborhoodChangeListener#notifyNeighborAdded(polimi.reds.NodeDescriptor, java.io.Serializable)
	 */
	@Override
	public void notifyNeighborAdded(NodeDescriptor addedNode, Serializable reconfigurationInfo) {
		System.out.println("GM for " + currentNodeDescriptor + " notifyNeighborAdded " + addedNode);
		
		if (!leader) {
			// Does nothing.
			return;
		}
		
		if (groupDescriptor.isMember(addedNode)) {
			// Already a member, nothing to be done.
			return;
		}
		
		try {
			overlay.send(MessageSubjects.GROUP_DISCOVERED_NOTIFICATION, groupDescriptor, addedNode);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void notifyNeighborDead(NodeDescriptor deadNode, Serializable reconfigurationInfo) {
		if (!deadNode.equals(groupDescriptor.getLeader())) {
			groupDescriptor.getFollowers().remove(deadNode);
		} else {
			// TODO promote a new leader!
		}
	}
	
	@Override
	public void notifyNeighborRemoved(NodeDescriptor removedNode) {
		if (!removedNode.equals(groupDescriptor.getLeader())) {
			groupDescriptor.getFollowers().remove(removedNode);
		} else {
			// TODO promote a new leader!
		}
	}

	/**
	 * @return the overlay
	 */
	public Overlay getOverlay() {
		return overlay;
	}

	/**
	 * @param overlay the overlay to set
	 */
	private void setOverlay(Overlay overlay) {
		if (this.overlay != null) {
			throw new AssertionError("Overlay already configured");
		}
		if (overlay == null) {
			throw new AssertionError("Overlay cannot be null");
		}
		this.overlay = overlay;
		/*overlay.setTrafficClass(groupDescriptor.getFriendlyName(),
				groupDescriptor.getFriendlyName());*/
		overlay.addNeighborhoodChangeListener(this);
	}
	
	/**
	 * When a node creates a new group the created group descriptor is spread
	 * over the network. Group leaders can use this information to merge similar groups.</p>
	 * 
	 * If the group somehow match with the tuple description of this group 
	 * then the new group should become a child of this.
	 * 
	 * @see {@link MessageSubjects#GROUP_CREATED_NOTIFICATION}
	 * @see {@link GroupLeaderCommand#MERGE_GROUPS}
	 */
	public void handleMessageGroupCreatedNotification(NodeDescriptor sender,
			GroupDescriptor packet) {
		if (!groupDescriptor.isLeader(currentNodeDescriptor)) {
			return;
		}
		
		// TODO send a GroupLeaderCommand#MERGE_GROUPS if opportune
	}


	public void handleMessageGroupFollowerCommandAck(NodeDescriptor sender,
			GroupFollowerCommandAck packet) {
		// TODO Auto-generated method stub
		
	}


	public void handleMessageGroupFollowerCommand(NodeDescriptor sender,
			GroupFollowerCommand packet) {
		// TODO Auto-generated method stub
		
	}


	/**
	 * Handles {@link MessageSubjects#GROUP_LEADER_COMMAND_ACK} messages
	 * received from a follower.
	 * 
	 * @param sender
	 * @param message
	 */
	public void handleMessageGroupLeaderCommandAck(NodeDescriptor sender,
		GroupLeaderCommandAck message) {
		if (!leader) {
			return;
		}
		
		
		// TODO check if the command was sent by this node
		Message msg = pendingMessages.get(message.getOriginalMessage());
		if (msg == null) {
			// The original message was not in the table
			// maybe it was expired?
			// Maybe it was for another group
			return;
		}
		// Remove the pending message
		pendingMessages.remove(msg.getID());
		
		GroupLeaderCommand command = (GroupLeaderCommand)msg;
		String commandType = command.getCommand();
		String responseType = message.getResponse();
		if (UPDATE_DESCRIPTOR.equals(commandType)) {
			// Nothing to be done
			return;
		} else {
			// unhandled commands..
		}
	}

	
	
	private void sendMessage(String subject, 
			Message message, NodeDescriptor recipient) {
		try {
			overlay.send(subject, message, recipient);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private void sendCoordinationCommand(
			GroupCoordinationCommand message, NodeDescriptor recipient) {
		sendMessage(GROUP_COORDINATION_COMMAND, message, recipient);
		pendingMessages.put(message.getID(), message);
	}
	
	private void sendLeaderCommand(
			GroupLeaderCommand message, NodeDescriptor recipient) {
		sendMessage(GROUP_LEADER_COMMAND, message, recipient);
		pendingMessages.put(message.getID(), message);
	}
	
	private void sendFollowerCommand(
			GroupFollowerCommand message, NodeDescriptor recipient) {
		sendMessage(GROUP_FOLLOWER_COMMAND, message, recipient);
		pendingMessages.put(message.getID(), message);
	}
	
	private void sendMessageToFollowers(GroupLeaderCommand message) {
		for (NodeDescriptor follower: groupDescriptor.getFollowers()) {
			System.out.println("GM for " + 
					currentNodeDescriptor + " sending to follower " + 
					follower);
			sendLeaderCommand(message, follower);
		}
	}
	
	protected void cleanPendingMessages() {
		// TODO clean the pendingCommunicationMessages map to avoid storing 
		// values for undelievered messages by removing all the expired ones.
	}
	
	/**
	 * Handles {@link MessageSubjects#GROUP_LEADER_COMMAND} messages by performing
	 * the proper action according to what is commanded by the leader.
	 * 
	 * @param sender
	 * @param message
	 */
	public void handleMessageGroupLeaderCommand(NodeDescriptor sender,
		GroupLeaderCommand message) {
		GroupDescriptor remoteGroup = message.getGroupDescriptor();
		
		GroupLeaderCommandAck commandAck = null;
		
		if (!remoteGroup.isLeader(sender)) {
			// The sender is not the group leader
			commandAck = GroupLeaderCommandAck.createKoCommand(
					message.getID(), groupDescriptor);
			sendMessage(MessageSubjects.GROUP_LEADER_COMMAND_ACK, 
					commandAck, sender);
		}
		
		String commandType = message.getCommand();
		if (MERGE_GROUPS.equals(commandType)) {
			// If the parent is null OK otherwise KO
			if (groupDescriptor.getParentLeader() != null) {
				commandAck = GroupLeaderCommandAck.createKoCommand(
						message.getID(), groupDescriptor);
				sendMessage(GROUP_LEADER_COMMAND_ACK, commandAck, sender);
				return;
			} else {
				// The current node has accepted the remote as a parent node
				// From now on the current node will be both leader of his current
				// group and follower of his parent group.
				remoteGroup.addFollower(currentNodeDescriptor);
				node.getGroupCommunicationDispatcher().addGroupManager(
						GroupCommunicationManager.createGroup(node, remoteGroup));
				commandAck = GroupLeaderCommandAck.createOkCommand(
						message.getID(), groupDescriptor);
				sendMessage(GROUP_LEADER_COMMAND_ACK, commandAck, sender);
				
				// Update the descriptor and send updates to all his followers
				groupDescriptor.setParentLeader(sender);
				GroupLeaderCommand updateCommand = GroupLeaderCommand.
				createUpdateCommand(groupDescriptor);
				sendMessageToFollowers(updateCommand);
				return;
			}	
		} else if (JOIN_MY_GROUP.equals(commandType)) {
			if (groupDescriptor.getFollowers().size() > 0) {
				// A remote node has asked this node do dismantle this group
				// And join its group but this group is not empty...
				commandAck = GroupLeaderCommandAck.createKoCommand(
						message.getID(), groupDescriptor);
				sendMessage(MessageSubjects.GROUP_LEADER_COMMAND_ACK, commandAck, sender);
			} else {
				node.getGroupCommunicationDispatcher().removeGroup(this);
				node.getGroupCommunicationDispatcher().addGroupManager(
						GroupCommunicationManager.createGroup(node, remoteGroup));
				commandAck = GroupLeaderCommandAck.createOkCommand(
						message.getID(), groupDescriptor);
				sendMessage(GROUP_LEADER_COMMAND_ACK, commandAck, sender);
				// The leader will then groupcast an updated descriptor
				return;
			}
		} else if (UPDATE_DESCRIPTOR.equals(commandType)) {
			System.out.println(currentNodeDescriptor + " updating node " + remoteGroup);
			// update the descriptor
			groupDescriptor = remoteGroup;
			
			// TODO if this is a universe group we should change the network topology
			// TODO We should probably add or remove some node...
			commandAck = GroupLeaderCommandAck.createOkCommand(
					message.getID(), groupDescriptor);
			sendMessage(GROUP_LEADER_COMMAND_ACK, commandAck, sender);
		} else {
			// unhandled commands..
		}
	}

	/**
	 * When a group leader receives a group discovered message and the
	 * current node is the leader then the manager can either merge the
	 * other group or join it as a member. </p>
	 * TODO comment this more nicely
	 */
	@Override
	public void handleGroupDiscovered(NodeDescriptor sender, 
			GroupDescriptor remoteGroupDescriptor) {
		if (!leader) {
			// Does nothing.
			return;
		}
		
		if (!groupDescriptor.matches(remoteGroupDescriptor)) {
			// The two group does not match
			return;
		}
		
		// This and the received group matches therefore we need
		// to merge them
		NodeDescriptor localParent = groupDescriptor.getParentLeader();
		NodeDescriptor remoteParent = remoteGroupDescriptor.getParentLeader();
		NodeDescriptor remoteLeader = remoteGroupDescriptor.getLeader();
		
		if (localParent != null && remoteParent == null) {
			if (localParent.equals(remoteGroupDescriptor.getLeader())) {
				// The remote leader was the local parent.
				// This should not be happening
				// TODO think how to avoid the creation of rings
				return;
			}
			// The local group is already part of a chain while the remote one is not
			GroupCoordinationCommand command = null;
			if (remoteGroupDescriptor.getFollowers().size() == 0) {
				command = GroupCoordinationCommand.createJoinMyGroupCommand(groupDescriptor);
			} else {
				command = GroupCoordinationCommand.createMergeGroupCommand(groupDescriptor);
			}
			sendCoordinationCommand(command, remoteLeader);
			return;
		} else if (localParent == null && remoteParent != null) {
			// This node will wait for the remote node to invite it
			// since the remote node is part of a chain.
			return;
		} else if (localParent != null && remoteParent != null) {
			// They are both part of chain
			// Either we forward the events to their parents or we do nothing.
			// TODO think which one will be the best solution.
			return;
		} else { // localParent == null && remoteParent == null
			// Neither the local or the remote group are part of a hierarchy
			// Therefore we can select what to do depending on the number
			// of followers
			int localFollowersCount = groupDescriptor.getFollowers().size();
			int remoteFollowersCount = remoteGroupDescriptor.getFollowers().size();
			if (localFollowersCount < remoteFollowersCount) {
				GroupCoordinationCommand command = 
					GroupCoordinationCommand.createMergeGroupCommand(groupDescriptor);
				sendCoordinationCommand(command, remoteLeader);
			} else if (localFollowersCount > remoteFollowersCount) {
				// The current leader wait for the other one to invite it
				return;
			} else { 
				// they have the same number of followers or no followers
				// we need another logic to decide whom will invite the other
				
				// The one with the higher id will send the invitation
				if (currentNodeDescriptor.getID().compareTo(remoteGroupDescriptor.getLeader().getID()) > 0) {
					return;
				}
				
				GroupCoordinationCommand command = null;
				if (remoteFollowersCount == 0) {
					// They are both empty: one should join the other
					command = GroupCoordinationCommand.createJoinMyGroupCommand(groupDescriptor);
				} else {
					// they are both full: merge
					command = GroupCoordinationCommand.createMergeGroupCommand(groupDescriptor);
				}
				sendCoordinationCommand(command, remoteLeader);
			}	
		}
	}
	
	/**
	 * @return the groupDescriptor
	 */
	public GroupDescriptor getGroupDescriptor() {
		return groupDescriptor;
	}


	public void handleMessageGroupCoordinationCommandAck(NodeDescriptor sender,
			GroupCoordinationCommandAck message) {
		if (!leader) {
			return;
		}
		
		// Check if the command was sent by this node
		Message msg = pendingMessages.get(message.getOriginalMessage());
		if (msg == null) {
			// The original message was not in the table
			// maybe it was expired?
			// Maybe it was for another group
			return;
		}
		// Remove the pending message
		pendingMessages.remove(msg.getID());
		
		GroupCoordinationCommand command = (GroupCoordinationCommand)msg;
		String commandType = command.getCommand();
		String responseType = message.getResponse();
		if (MERGE_GROUPS.equals(commandType)) {
			// This node has sent a merge group request
			// to another leader.
			if (GroupCoordinationCommandAck.OK.equals(responseType)) {
				// The remote leader has accepted to become a child.
				// We add it to the followers and notify the other followers
				groupDescriptor.addFollower(sender);
				GroupLeaderCommand updateCommand = GroupLeaderCommand.createUpdateCommand(groupDescriptor);
				sendMessageToFollowers(updateCommand);
				return;
			} else if (GroupLeaderCommandAck.KO.equals(responseType)) {
				// It refused
				return;
			}
		} else if (JOIN_MY_GROUP.equals(commandType)) {
			// This node has sent a join group request
			// to another leader.
			if (GroupCoordinationCommandAck.OK.equals(responseType)) {
				// The remote leader has accepted to become a child.
				// We add it to the followers and notify the other followers
				groupDescriptor.addFollower(sender);
				GroupLeaderCommand updateCommand = GroupLeaderCommand.createUpdateCommand(groupDescriptor);
				sendMessageToFollowers(updateCommand);
				return;
			} else if (GroupLeaderCommandAck.KO.equals(responseType)) {
				// It refused.
				// we may try with a merge
				GroupCoordinationCommand sendAttempt = GroupCoordinationCommand.createMergeGroupCommand(groupDescriptor);
				sendCoordinationCommand(sendAttempt, sender);
				return;
			}
		} else {
			// unhandled commands..
		}
	}


	public void handleMessageGroupCoordinationCommand(NodeDescriptor sender,
			GroupCoordinationCommand message) {
		GroupDescriptor remoteGroup = message.getGroupDescriptor();
		GroupCoordinationCommandAck commandAck = null;
		
		if (!remoteGroup.isLeader(sender)) {
			// The sender is not the group leader
			commandAck = GroupCoordinationCommandAck.createKoCommand(message.getID(), groupDescriptor);
			sendMessage(MessageSubjects.GROUP_COORDINATION_COMMAND_ACK, commandAck, sender);
		}
		
		String commandType = message.getCommand();
		if (MERGE_GROUPS.equals(commandType)) {
			// If the parent is null OK otherwise KO
			if (groupDescriptor.getParentLeader() != null) {
				commandAck = GroupCoordinationCommandAck.createKoCommand(message.getID(), groupDescriptor);
				sendMessage(GROUP_COORDINATION_COMMAND_ACK, commandAck, sender);
				return;
			} else {
				// The current node has accepted the remote as a parent node
				// From now on the current node will be both leader of his current
				// group and follower of his parent group.
				remoteGroup.addFollower(currentNodeDescriptor);
				node.getGroupCommunicationDispatcher().addGroupManager(
						GroupCommunicationManager.createGroup(node, remoteGroup));
				commandAck = GroupCoordinationCommandAck.createOkCommand(message.getID(), groupDescriptor);
				sendMessage(GROUP_COORDINATION_COMMAND_ACK, commandAck, sender);
				
				// Update the descriptor and send updates to all his followers
				groupDescriptor.setParentLeader(sender);
				GroupLeaderCommand updateCommand = GroupLeaderCommand.createUpdateCommand(groupDescriptor);
				sendMessageToFollowers(updateCommand);
				return;
			}	
		} else if (JOIN_MY_GROUP.equals(commandType)) {
			if (groupDescriptor.getFollowers().size() > 0) {
				// A remote node has asked this node do dismantle this group
				// And join its group but this group is not empty...
				commandAck = GroupCoordinationCommandAck.createKoCommand(message.getID(), groupDescriptor);
				sendMessage(MessageSubjects.GROUP_COORDINATION_COMMAND_ACK, commandAck, sender);
			} else {
				// Destroy this group and join the other one as member
				node.getGroupCommunicationDispatcher().removeGroup(this);
				node.getGroupCommunicationDispatcher().addGroupManager(
						GroupCommunicationManager.createGroup(node, remoteGroup));
				
				commandAck = GroupCoordinationCommandAck.createOkCommand(message.getID(), groupDescriptor);
				sendMessage(GROUP_COORDINATION_COMMAND_ACK, commandAck, sender);
				// The leader will then groupcast an updated descriptor
				return;
			}
		} else {
			// unhandled commands..
		}
	}

}
