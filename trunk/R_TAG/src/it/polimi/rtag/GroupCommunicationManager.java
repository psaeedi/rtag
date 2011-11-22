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
import it.polimi.rtag.messaging.TupleMessage;
import it.polimi.rtag.messaging.TupleMessage.Scope;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

import polimi.reds.Message;
import polimi.reds.MessageID;
import polimi.reds.NodeDescriptor;
import polimi.reds.broker.overlay.NeighborhoodChangeListener;
import polimi.reds.broker.overlay.Overlay;

import static it.polimi.rtag.messaging.MessageSubjects.*;
import static it.polimi.rtag.messaging.GroupLeaderCommand.*;
import static it.polimi.rtag.messaging.GroupFollowerCommand.*;
import static it.polimi.rtag.messaging.GroupCoordinationCommand.*;

/**
 * Handles the communication of a group and also keep the group descriptor updated.
 * 
 * @see {@link NeighborhoodChangeListener, PacketListener}
 * 
 * @author Panteha Saeedi (saeedi@elet.polimi.it).
 */
public class GroupCommunicationManager implements NeighborhoodChangeListener {

	/**
	 * The {@link GroupCommunicationManager} managing a
	 * parent group if any.
	 */
	private GroupCommunicationManager followedParentManager;
	
	/**
	 * The {@link GroupCommunicationManager} managing a
	 * child group if any.
	 */
	private GroupCommunicationManager leadedChildManager;
	
	private Node node;
	
	private GroupDescriptor groupDescriptor;
	private NodeDescriptor currentNodeDescriptor;
	private Overlay overlay;
	private GroupCoordinationStrategy coordinationStrategy;
	
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
	public static GroupCommunicationManager createGroupCommunicationManager(Node node, 
			 UUID uniqueId, String friendlyName) {
		
		GroupDescriptor groupDescriptor = new GroupDescriptor(uniqueId, 
				friendlyName, node.getNodeDescriptor());
		return createGroupCommunicationManager(node, groupDescriptor);
	}
	
	
	/**
	 * Wraps a given group for communication manager for the given node.
	 * 
	 * @param node
	 * @param groupDescriptor
	 * @return
	 */
	public static GroupCommunicationManager createGroupCommunicationManager(Node node, 
			GroupDescriptor groupDescriptor) {
		
		GroupCommunicationManager manager = new GroupCommunicationManager(
				node, groupDescriptor, node.getOverlay());
		return manager;
	}
	
	public static GroupCommunicationManager createChildGroupCommunicationManager(Node node,
			GroupDescriptor parentGroupDescriptor) {
		GroupDescriptor groupDescriptor = new GroupDescriptor(UUID.randomUUID(), 
				parentGroupDescriptor.getFriendlyName(),
				node.getNodeDescriptor(), parentGroupDescriptor.getLeader());
		if (parentGroupDescriptor.isUniverse()) {
			groupDescriptor.isUniverse();
		}
		return createGroupCommunicationManager(node, groupDescriptor);
	}
	
	public static GroupCommunicationManager createUniverseCommunicationManager(Node node) {
		
		GroupDescriptor groupDescriptor = GroupDescriptor.createUniverse(node);
		return createGroupCommunicationManager(node, groupDescriptor);
	}
	
	/**
	 * Instances of this classes should only be created using
	 * the factory methods.
	 * 
	 * @param groupDescriptor
	 * @param currentNodeDescriptor
	 * @param overlay
	 */
	private GroupCommunicationManager(
			Node node, 
			GroupDescriptor groupDescriptor,
			Overlay overlay) {
		this.node = node;
		this.currentNodeDescriptor = node.getNodeDescriptor();
		setOverlay(overlay);
		setGroupDescriptor(groupDescriptor);		
	}
	
	public boolean isLeader() {
		return groupDescriptor.isLeader(currentNodeDescriptor);
	}
	
	
	/**
	 * Connects this manager with the other manager of this hierarchy if any.
	 * 
	 * @param manager
	 */
	protected void handleGroupManagerAdded(GroupCommunicationManager manager) {
		if (this.equals(manager)) {
			throw new RuntimeException(
					"A manager should not receive notifications " +
					"about itself. This should NOT have happened.");
		}
		GroupDescriptor otherGroupDescriptor = manager.getGroupDescriptor();
		if (!groupDescriptor.getFriendlyName().equals(
				otherGroupDescriptor.getFriendlyName())) {
		 return;
		}
		if (isLeader()) {
			if (manager.isLeader()) {
				throw new RuntimeException(
						"A node should only lead a group of the same " +
						"hierarchy. This should NOT have happened.");
			}
			if (followedParentManager != null) {
				throw new RuntimeException(
						"There is already a parent manager." +
						"This should NOT have happened.");
			}
			// We add the child manager
			followedParentManager = manager;
			return;
		} else {
			if (!manager.isLeader()) {
				throw new RuntimeException(
						"A node should only follow a group of the same " +
						"hierarchy. This should NOT have happened.");
			}
			if (leadedChildManager != null) {
				throw new RuntimeException(
						"There is already a child manager." +
						"This should NOT have happened.");
			}
			// We add the child manager
			leadedChildManager = manager;
			return;
		}

	}
	
	/**
	 * Notify this manager that a manager of the same hierarchy
	 * has been removed.
	 * 
	 * @param manager
	 */
	public void handleGroupManagerRemoved(GroupCommunicationManager manager) {
		if (this.equals(manager)) {
			throw new RuntimeException(
					"A manager should not receive notifications about itself.");
		}
		if (!manager.groupDescriptor.getFriendlyName().
				equals(groupDescriptor.getFriendlyName())) {
			throw new RuntimeException(
				"A manager should not receive notifications about groups of other hierarchies.");		
		}
		
		if (manager.equals(followedParentManager)) {
			followedParentManager = null;
			return;
		}
		if (manager.equals(leadedChildManager)) {
			leadedChildManager = null;
			return;
		}
	}

	
	/**
	 * Notify this group manager that a new node has been discovered. 
	 * If the new discovered node is
	 * already a group member (which can happen in case of internal delay)
	 * nothing is done. Otherwise if the new node is not a member of the group
	 * the leader will send it a {@link MessageSubjects#GROUP_DISCOVERED_NOTIFICATION}.
	 * 
	 * @see polimi.reds.broker.overlay.NeighborhoodChangeListener#notifyNeighborAdded(polimi.reds.NodeDescriptor, java.io.Serializable)
	 */
	@Override
	public void notifyNeighborAdded(NodeDescriptor addedNode, Serializable reconfigurationInfo) {
		if (followedParentManager != null) {
			// The followedParentManager will do this.
			return;
		}

		if (groupDescriptor.isMember(addedNode) ||
				groupDescriptor.isParentLeader(addedNode)) {
			// Already a member, nothing to be done.
			return;
		}
		
		try {
			// Notify the other node of the existence of this group
			overlay.send(MessageSubjects.GROUP_DISCOVERED_NOTIFICATION, 
					groupDescriptor, addedNode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * If the
	 * current node is not a follower of this group, then ignore the message.</p>
	 * If the removed node is not a leader, we remove the node from the descriptor.
	 * If the current node is a leader it sends the updated group descriptor.</p>
	 * 
	 * If the removed node was the leader, a new leader is required to be selected.</p>
	 * 
	 * @see polimi.reds.broker.overlay.NeighborhoodChangeListener#notifyNeighborDead(polimi.reds.NodeDescriptor, java.io.Serializable)
	 */
	@Override
	public void notifyNeighborDead(NodeDescriptor deadNode, Serializable reconfigurationInfo) {
		if (currentNodeDescriptor.equals(deadNode)) {
			throw new RuntimeException("Notified of its own death");
		}
		
		// Also the topology receives this event independently.
		// Do NOT invoke it twice
		System.out.println("GM " + groupDescriptor.getFriendlyName() + " for " + 
				currentNodeDescriptor + " has been notified that " + 
				deadNode + " is dead. " + reconfigurationInfo);
		
		if (!groupDescriptor.isMember(deadNode)) {
			// Not a group member
			return;
		}
		
		// If the dead node is the parent group simply set it to null
		if (groupDescriptor.isParentLeader(deadNode)) {
			groupDescriptor.setParentLeader(null);
			
			if (isLeader()) {
				GroupLeaderCommand updateCommand = 
						GroupLeaderCommand.createUpdateCommand(groupDescriptor);
				sendMessageToFollowers(updateCommand);
			} 
			
			return;
		}
				
		
		if (!groupDescriptor.isLeader(deadNode)) {
			groupDescriptor.removeFollower(deadNode);
			if (isLeader()) {
				GroupLeaderCommand updateCommand = 
						GroupLeaderCommand.createUpdateCommand(groupDescriptor);
				sendMessageToFollowers(updateCommand);
			} 
			return;
		} else {
			// promote a new leader!
			groupDescriptor.setLeader(null);
						
			NodeDescriptor newLeader = coordinationStrategy.electNewLeader();
			groupDescriptor.setLeader(newLeader);
			
			if (currentNodeDescriptor.equals(newLeader)) {
				// If the new leader was also a child leader we
				// need to close this group and move all the 
				// nodes to the other one.
				if (leadedChildManager != null) {
					leadedChildManager.migrateAllFollowersAndAskAdoption(groupDescriptor);
					node.getGroupCommunicationDispatcher().removeGroup(this);
				} else {
					node.getGroupCommunicationDispatcher().reassignGroup(this);
					// if the dead leader was part of a hierarchy
					// the current node should join the parent group
					NodeDescriptor parent = groupDescriptor.getParentLeader();
					if (parent != null) {
						GroupCoordinationCommand command = 
							GroupCoordinationCommand.createAdoptGroupCommand(groupDescriptor);
						sendCoordinationCommand(command, parent);
					}
				}
			} else {
				// Notify the children of the update
				// TODO??
			}
			return;
		}	
	}
	
	/**
	 * When a leader crash and the new leader was already a child leader.
	 */
	private void migrateAllFollowersAndAskAdoption(GroupDescriptor remoteGroup) {
		for (NodeDescriptor node: remoteGroup.getFollowers()) {
			GroupCoordinationCommand command = GroupCoordinationCommand
					.createMigrateToGroupCommand(groupDescriptor);
			sendCoordinationCommand(command, node);
		}
		if (groupDescriptor.getParentLeader() != null) {
			return;
		}
		// Join the parent group
		NodeDescriptor parent = remoteGroup.getParentLeader();
		if (parent != null) {
			GroupCoordinationCommand command = 
				GroupCoordinationCommand.createAdoptGroupCommand(groupDescriptor);
			sendCoordinationCommand(command, parent);
		}
	}


	/** 
	 * Notify this group manager that a new node has been removed. 
	 * @see polimi.reds.broker.overlay.NeighborhoodChangeListener#notifyNeighborRemoved(polimi.reds.NodeDescriptor)
	 */
	@Override
	public void notifyNeighborRemoved(NodeDescriptor removedNode) {
		// Do nothing
		System.out.println("************ REMOVED: "+ removedNode);
		notifyNeighborDead(removedNode, null);
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
	}
	
	/**
	 * Starts sending a broadcast. If this node is a follower it
	 * delegates its leader. If the node is a leader then it
	 * forwards the message to its followers and to the parent
	 * if it exits.
	 * 
	 * @param subject
	 * @param message
	 */
	void sendGroupcast(String subject, Serializable message,
			LeadedChildCommandDelegate delegate) {
		handleGroupast(subject, message, delegate, this.currentNodeDescriptor);
	}
	
	private void handleGroupast(String subject, Serializable message,
			LeadedChildCommandDelegate delegate, NodeDescriptor sender) {
		if (isLeader()) {
			// Forward to the parent and to the follower
			sendMessageToParent(subject, message, sender);
			sendMessageToFollowers(subject, message, sender);
			if (delegate != null) {
				delegate.invokeCommand(this, message);
			}
		} else {
			// Forward to the leader
			sendMessageToLeader(subject, message, sender);
			if (leadedChildManager != null) {
				leadedChildManager.handleGroupast(subject, message, delegate, sender);
				return;
			}
		}
	}
	
	private void sendMessageToLeader(String subject, Serializable message, NodeDescriptor sender) {
		if (isLeader()) {
			throw new RuntimeException("The leader should not talk to itself.");
		}
		NodeDescriptor leader = groupDescriptor.getLeader();
		if (leader.equals(sender)) {
			return;
		}
		sendMessage(subject, message, leader);
	}


	private void sendMessageToFollowers(String subject, Serializable message, NodeDescriptor sender) {
		if (!isLeader()) {
			throw new RuntimeException("Only a leader should broadcast to followers");
		}
		for (NodeDescriptor follower: groupDescriptor.getFollowers()) {
			if (sender.equals(follower)) {
				continue;
			}
			sendMessage(subject, message, follower);
		}
	}


	private void sendMessageToParent(String subject, Serializable message, NodeDescriptor sender) {
		if (!isLeader()) {
			throw new RuntimeException("Only a leader should talk to the parent");
		}
		NodeDescriptor parent = groupDescriptor.getParentLeader();
		if (parent == null) {
			// The parent is null
			return;
		}
		if (parent.equals(sender)) {
			return;
		}
		sendMessage(subject, message, parent);
	}

/*
	public void sendMessageGroupCreatedNotification(NodeDescriptor sender,
			GroupDescriptor packet) {
		if (!getGroupDescriptor().isUniverse()) {
			throw new RuntimeException(
					"Only universe can forward group creation messages.");
		}
		
		// We should forward the message to everyone beside the sender
		if (isLeader()) {
			for (NodeDescriptor follower: groupDescriptor.getFollowers()) {
				if (follower.equals(sender)) {
					continue;
				}
				connectIfNotConnected(follower);
				try {
					overlay.send(GROUP_CREATED_NOTIFICATION, packet, follower);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (followedParentManager != null) {
				followedParentManager.sendMessageGroupCreatedNotification(sender, packet);
			}
		} else {
			NodeDescriptor leader = groupDescriptor.getLeader();
			if (!leader.equals(sender)) {
				connectIfNotConnected(leader);
				try {
					overlay.send(GROUP_CREATED_NOTIFICATION, packet, leader);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (leadedChildManager != null) {
				leadedChildManager.sendMessageGroupCreatedNotification(sender, packet);
			}
		}
	}
	*/
	
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
	/*
	public void handleMessageGroupCreatedNotification(NodeDescriptor sender,
			GroupDescriptor remoteGroupDescriptor) {
		
		if (isLeader() && getGroupDescriptor().isUniverse()) {
			// Notify the rest of the network
			sendMessageGroupCreatedNotification(sender, remoteGroupDescriptor);
			return;
		}
		
		if (!groupDescriptor.hasSameName(remoteGroupDescriptor)) {
			// The two group does not match
			return;
		}
		
		if (groupDescriptor.getUniqueId().equals(remoteGroupDescriptor.getUniqueId())) {
			// Same group
			return;
		}
		
		NodeDescriptor remoteLeader = remoteGroupDescriptor.getLeader();	
		if (currentNodeDescriptor.equals(remoteLeader)) {
			// we have discovered our child
			return;
		}
		
		// We first attempt to merge then to join
		if (coordinationStrategy.shouldInviteToMerge(remoteGroupDescriptor)) {
			GroupCoordinationCommand command = 
					GroupCoordinationCommand.createMergeGroupCommand(groupDescriptor);
			sendCoordinationCommand(command, remoteLeader);
		} else if (coordinationStrategy.shouldInviteToJoin(remoteGroupDescriptor)) {
			GroupCoordinationCommand command = 
					GroupCoordinationCommand.createJoinMyGroupCommand(groupDescriptor);
			sendCoordinationCommand(command, remoteLeader);
		} else {
			// Since this call is not simmetrical if we do not anything about it
			// We should send the same message to the leader of the remote group.
			connectIfNotConnected(remoteLeader);
			try {
				overlay.send(GROUP_DISCOVERED_NOTIFICATION, 
						remoteGroupDescriptor, remoteLeader);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
*/

	public void handleMessageGroupFollowerCommandAck(NodeDescriptor sender,
			GroupFollowerCommandAck message) {
			if (isLeader()) {
				return;
			}
			
			Message msg = null;
			synchronized (pendingMessages) {
				msg = pendingMessages.get(message.getOriginalMessage());
				if (msg == null) {
					// The original message was not in the table
					// maybe it was expired?
					// Maybe it was for another group
					return;
				}
				// Remove the pending message
				pendingMessages.remove(msg.getID());
			}
			
			
			GroupFollowerCommand command = (GroupFollowerCommand)msg;
			String commandType = command.getCommand();
			String responseType = message.getResponse();
			if (LEAVING_NOTICE.equals(commandType)) {
				// Nothing to be done
				return;
			} if (ASK_DELETE_GROUP.equals(commandType)) {
				// Nothing to be done
				return;
			} else {
				// unhandled commands..
			}
	}


	public void handleMessageGroupFollowerCommand(NodeDescriptor sender,
			GroupFollowerCommand message) {
		GroupDescriptor remoteGroup = message.getGroupDescriptor();
		if (!groupDescriptor.getUniqueId().equals(remoteGroup.getUniqueId())) {
			throw new RuntimeException("The remote and local groups do not match.");
		}
		
		GroupFollowerCommandAck commandAck = null;
		
		if (!groupDescriptor.isFollower(sender) || 
				!groupDescriptor.isLeader(currentNodeDescriptor)) {
			// The sender is not the group leader
			commandAck = GroupFollowerCommandAck.createKoCommand(
					message.getID(), groupDescriptor);
			sendMessage(MessageSubjects.GROUP_FOLLOWER_COMMAND_ACK, 
					commandAck, sender);
			return;
		}
		
		String commandType = message.getCommand();
		if (LEAVING_NOTICE.equals(commandType)) {
			// Update the current group
			handleFollowerRemoved(sender);
			
			// Sending ack
			sendOkFollowerCommandAck(message.getID(), sender);
			return;
		} else if (ASK_DELETE_GROUP.equals(commandType)) {
			sendOkFollowerCommandAck(message.getID(), sender);
			deleteGroup();	
		} else {
			// Other commands...
		}
	}

	private void sendOkFollowerCommandAck(MessageID id, NodeDescriptor sender) {
		GroupFollowerCommandAck commandAck = GroupFollowerCommandAck.createOkCommand(
				id, groupDescriptor);
		sendMessage(MessageSubjects.GROUP_FOLLOWER_COMMAND_ACK, 
				commandAck, sender);
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
		if (!isLeader()) {
			return;
		}
		
		Message msg = null;
		synchronized (pendingMessages) {
			msg = pendingMessages.get(message.getOriginalMessage());
			if (msg == null) {
				// The original message was not in the table
				// maybe it was expired?
				// Maybe it was for another group
				return;
			}
			// Remove the pending message
			pendingMessages.remove(msg.getID());
		}
		
		
		GroupLeaderCommand command = (GroupLeaderCommand)msg;
		String commandType = command.getCommand();
		String responseType = message.getResponse();
		if (UPDATE_DESCRIPTOR.equals(commandType)) {
			// Nothing to be done
			return;
		} else if (CREATE_CHILD_GROUP.equals(commandType)) {
			if (GroupLeaderCommandAck.OK.equals(responseType)) {
				// The other node will create a new group and will ask
				// the other followers to migrate.
				return;
			} else if (GroupLeaderCommandAck.KO.equals(responseType)) {
				createChildIfNecessary();
				return;
			}
		} else if (DELETE_GROUP.equals(commandType)) {
			throw new RuntimeException(
					"DELETE_GROUP aks are not sent and should not be received");
		} else {
			// unhandled commands..
		}
	}
	
	private void sendMessage(String subject, 
			Serializable message, NodeDescriptor recipient) {
		recipient = connectIfNotConnected(recipient);
		try {
			if (recipient == null) {
				System.err.println("CANNOT CONNECT TO: " + recipient);
				return;
			}
			System.out.println("GM " + groupDescriptor.getFriendlyName() + " " +
					currentNodeDescriptor + " sending " + message + " to" +
					recipient);
			overlay.send(subject, message, recipient);
		} catch (Exception e) {
			System.err.println("Catched: " + e.getMessage());
			e.printStackTrace();
		} 
	}
	
	private void sendCoordinationCommand(
			GroupCoordinationCommand message, NodeDescriptor recipient) {
		sendMessage(GROUP_COORDINATION_COMMAND, message, recipient);
		synchronized (pendingMessages) {
			pendingMessages.put(message.getID(), message);
		}
		
	}
	
	private void sendLeaderCommand(
			GroupLeaderCommand message, NodeDescriptor recipient) {
		sendMessage(GROUP_LEADER_COMMAND, message, recipient);
		synchronized (pendingMessages) {
			pendingMessages.put(message.getID(), message);
		}
	}
	
	private void sendFollowerCommand(
			GroupFollowerCommand message, NodeDescriptor recipient) {
		sendMessage(GROUP_FOLLOWER_COMMAND, message, recipient);
		synchronized (pendingMessages) {
			pendingMessages.put(message.getID(), message);
		}
		
	}
	
	private void sendMessageToFollowers(GroupLeaderCommand message) {
		for (NodeDescriptor follower: groupDescriptor.getFollowers()) {
			if (currentNodeDescriptor.equals(follower)) {
				throw new RuntimeException(
						"Trying to talk to itself. " +
						"This should NOT happen.");
			}
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
		if (UPDATE_DESCRIPTOR.equals(commandType)) {
			// Update the descriptor
			// An event is fired. The topology manager should use this event
			// to close unused connections/links
			setGroupDescriptor(remoteGroup);
			
			commandAck = GroupLeaderCommandAck.createOkCommand(
					message.getID(), groupDescriptor);
			sendMessage(GROUP_LEADER_COMMAND_ACK, commandAck, sender);
			return;
		} else if (CREATE_CHILD_GROUP.equals(commandType)) {
			// This follower has been suggested to create a child
			if (leadedChildManager == null && coordinationStrategy.shouldAcceptToCreateAChild()) {
				commandAck = GroupLeaderCommandAck.createOkCommand(
						message.getID(), groupDescriptor);
				sendMessage(GROUP_LEADER_COMMAND_ACK, commandAck, sender);
				// This node has accepted to create a child group
				GroupCommunicationManager childManager =
						GroupCommunicationManager.createChildGroupCommunicationManager(node, groupDescriptor);
				node.getGroupCommunicationDispatcher().addGroupManager(childManager);
				// The new manager should invite the other follower to join it
				childManager.inviteFollowersToMigrate(groupDescriptor);
			} else {
				commandAck = GroupLeaderCommandAck.createKoCommand(
						message.getID(), groupDescriptor);
				sendMessage(GROUP_LEADER_COMMAND_ACK, commandAck, sender);
			}
			return;
		} else if (DELETE_GROUP.equals(commandType)) {
			handleGroupast(GROUP_FOLLOWER_COMMAND, message,
					new DeleteGroupCommandDelegate(), sender);
		} else {
			// unhandled commands..
			throw new RuntimeException("Unknown GroupLeaderCommand: " + commandType);
		}
	}


	private void inviteFollowersToMigrate(GroupDescriptor remoteGroup) {
		for (NodeDescriptor follower: remoteGroup.getFollowers()) {
			if (currentNodeDescriptor.equals(follower)) {
				// Skip itself
				continue;
			}

			if (remoteGroup.getLeader() == null || 
					coordinationStrategy.shouldSuggestToMigrate(remoteGroup, follower)) {
				GroupCoordinationCommand command = 
					GroupCoordinationCommand.createMigrateToGroupCommand(groupDescriptor);
				sendCoordinationCommand(command, follower);
			}
		}
	}


	/**
	 * When a group leader receives a group discovered message and the
	 * current node is the leader then the manager can either merge the
	 * other group or join it as a member. </p>
	 * TODO comment this more nicely
	 */
	public void handleGroupDiscovered(NodeDescriptor sender, 
			GroupDescriptor remoteGroupDescriptor) {
		if (!groupDescriptor.isUniverse()) {
			return;
		}
		handleGroupast(GROUP_DISCOVERED_NOTIFICATION, remoteGroupDescriptor,
				new GroupDiscoveredCommandDelegate(), sender);
	}

	public void handleMatchingGroupDiscovered(GroupDescriptor remoteGroupDescriptor) {
		if (!groupDescriptor.hasSameName(remoteGroupDescriptor)) {
			// The two group does not match
			return;
		}
		
		if (groupDescriptor.getUniqueId().equals(remoteGroupDescriptor.getUniqueId())) {
			// Same group
			return;
		}
		
		NodeDescriptor remoteLeader = remoteGroupDescriptor.getLeader();	
		if (currentNodeDescriptor.equals(remoteLeader)) {
			// we have discovered our child
			return;
		}
		
		if (!isLeader()) {
			try {
				//connectIfNotConnected(groupDescriptor.getLeader());
				overlay.send(MessageSubjects.GROUP_DISCOVERED_NOTIFICATION, 
						remoteGroupDescriptor, groupDescriptor.getLeader());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			return;
		}
		
		// We first attempt to merge then to join
		if (coordinationStrategy.shouldInviteToMerge(remoteGroupDescriptor)) {
			GroupCoordinationCommand command = 
					GroupCoordinationCommand.createMergeGroupCommand(groupDescriptor);
			sendCoordinationCommand(command, remoteLeader);
			return;
		} else if (coordinationStrategy.shouldInviteToJoin(remoteGroupDescriptor)) {
			GroupCoordinationCommand command = 
					GroupCoordinationCommand.createJoinMyGroupCommand(groupDescriptor);
			sendCoordinationCommand(command, remoteLeader);
			return;
		} else {
			try {
				//connectIfNotConnected(groupDescriptor.getLeader());
				overlay.send(MessageSubjects.GROUP_DISCOVERED_NOTIFICATION, 
						groupDescriptor, remoteGroupDescriptor.getLeader());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			return;
		}
	}
	
	private NodeDescriptor connectIfNotConnected(NodeDescriptor descriptor) {
		if (currentNodeDescriptor.equals(descriptor)) {
			throw new RuntimeException("Trying to talk to itself. " +
					"This should definitely NOT happen.");
		}

		try {
			return node.getTopologyManager().addNeighborForGroup(
					descriptor, groupDescriptor);
		} catch (Exception e) {
			return null;
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
		if (!isLeader()) {
			return;
		}
		
		Message msg = null;
		synchronized (pendingMessages) {
			// Check if the command was sent by this node
			msg = pendingMessages.get(message.getOriginalMessage());
			if (msg == null) {
				// The original message was not in the table
				// maybe it was expired?
				// Maybe it was for another group
				return;
			}
			// Remove the pending message
			pendingMessages.remove(msg.getID());
		}
		

		GroupCoordinationCommand command = (GroupCoordinationCommand)msg;
		String commandType = command.getCommand();
		String responseType = message.getResponse();
		if (MERGE_GROUPS.equals(commandType)) {
			// This node has sent a merge group request
			// to another leader.
			if (GroupCoordinationCommandAck.OK.equals(responseType)) {
				// The remote leader has accepted to become a child.
				// We add it to the followers and notify the other followers
				handleFollowerJustJoined(sender);
				return;
			} else if (GroupLeaderCommandAck.KO.equals(responseType)) {
				// It refused
				return;
			}
		} else if (JOIN_MY_GROUP.equals(commandType)) {
			// This node has sent a join group request
			// to another leader.
			if (GroupCoordinationCommandAck.OK.equals(responseType)) {
				// The remote leader has accepted to become a follower.
				// We add it to the followers and notify the other followers
				// its group becomes the child group.
				handleFollowerJustJoined(sender);
				return;
			} else if (GroupLeaderCommandAck.KO.equals(responseType)) {
				System.out.println("<<<<<<<<<<<<<<sending a merge :/");
				// It refused.
				// we may try with a merge
				GroupCoordinationCommand sendAttempt = 
						GroupCoordinationCommand.createMergeGroupCommand(groupDescriptor);
				sendCoordinationCommand(sendAttempt, sender);
				return;
			}
		} else if (MIGRATE_TO_GROUP.equals(commandType)) {
			if (GroupCoordinationCommandAck.OK.equals(responseType)) {
				// The remote leader has accepted to become a follower.
				// We add it to the followers and notify the other followers
				//it should leave its other group
				handleFollowerJustJoined(sender);
				return;
			} else if (GroupLeaderCommandAck.KO.equals(responseType)) {
				// It refused
				return;
			}
		} else if (ADOPT_GROUP.equals(commandType)) {
			if (GroupCoordinationCommandAck.OK.equals(responseType)) {
				// The remote leader has adopted this group
				handleParentLeaderChange(sender);
				
				// The child leader (which is the current node)
				// Should become a follower of the remote group.
				GroupDescriptor parentGroup = message.getGroupDescriptor();
				// The parent has already added this node as a follower of its group.
				if (!parentGroup.isMember(currentNodeDescriptor)) {
					throw new RuntimeException("The adopteee should have already been added.");
				}
				GroupCommunicationManager manager = 
						GroupCommunicationManager.createGroupCommunicationManager(node, parentGroup);
				node.getGroupCommunicationDispatcher().addGroupManager(manager);
			} else if (GroupLeaderCommandAck.KO.equals(responseType)) {
				// It refused
				handleParentLeaderChange(null);
			}
			GroupLeaderCommand updateCommand = GroupLeaderCommand.createUpdateCommand(groupDescriptor);
			sendMessageToFollowers(updateCommand);
			return;
		}else {
			// unhandled commands..
			throw new RuntimeException("Unknown GroupCoordinationCommand: " + commandType);
		}
	}

	
	/**
	 * The current leader is about to add a new follower to the group.
	 * 
	 * 
	 * @param follower
	 */
	private void handleFollowerJustJoined(NodeDescriptor follower) {
		groupDescriptor.addFollower(follower);
		GroupLeaderCommand command = GroupLeaderCommand.createUpdateCommand(groupDescriptor);
		sendMessageToFollowers(command);
		createChildIfNecessary();
		// We ask the child manger to see if it can get some followers
		if (leadedChildManager != null) {
			leadedChildManager.inviteFollowersToMigrate(groupDescriptor);
		}
	}
	
	/**
	 * A follower has been removed.
	 * Remove it from the {@link GroupAwareTopologyManager} and notify the other
	 * followers. 
	 */
	private void handleFollowerRemoved(NodeDescriptor follower) {
		groupDescriptor.removeFollower(follower);
		node.getTopologyManager().removeNeighboorForGroup(
				follower, groupDescriptor);
		GroupLeaderCommand updateCommand = 
				GroupLeaderCommand.createUpdateCommand(groupDescriptor);
		sendMessageToFollowers(updateCommand);
	}
	
	private void handleParentLeaderChange(NodeDescriptor descriptor) {
		NodeDescriptor oldParent = groupDescriptor.getParentLeader();
		groupDescriptor.setParentLeader(descriptor);
		// If the parent has changed we can close its connection
		if (oldParent != null && !oldParent.equals(descriptor)) {
			node.getTopologyManager().removeNeighboorForGroup(
					oldParent, groupDescriptor);
		}
		if (isLeader()) {
			// Propagate the change
			GroupLeaderCommand updateCommand = 
				GroupLeaderCommand.createUpdateCommand(groupDescriptor);
			sendMessageToFollowers(updateCommand);
		}
	}
	
	
	private void createChildIfNecessary() {
		NodeDescriptor recipient = coordinationStrategy.shouldSplitToNode();
		if (recipient != null) {
			GroupLeaderCommand command = GroupLeaderCommand.createCreateChildCommand(groupDescriptor);
			sendLeaderCommand(command, recipient);
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
			if (isLeader() && followedParentManager == null && 
					coordinationStrategy.shouldAcceptToMerge(remoteGroup)) {
				// The current node has accepted the remote as a parent node
				// From now on the current node will be both leader of his current
				// group and follower of his parent group.
				remoteGroup.addFollower(currentNodeDescriptor);
				node.getGroupCommunicationDispatcher().addGroupManager(
						GroupCommunicationManager.createGroupCommunicationManager(node, remoteGroup));
				commandAck = GroupCoordinationCommandAck.createOkCommand(message.getID(), groupDescriptor);
				sendMessage(GROUP_COORDINATION_COMMAND_ACK, commandAck, sender);
				
				// Update the descriptor and send updates to all his followers
				handleParentLeaderChange(sender);
				return;
			} else {
				commandAck = GroupCoordinationCommandAck.createKoCommand(message.getID(), groupDescriptor);
				sendMessage(GROUP_COORDINATION_COMMAND_ACK, commandAck, sender);
				return;
			}	
		} else if (JOIN_MY_GROUP.equals(commandType)) {
			// If this node is leader of a child group it will refuse
			if (followedParentManager != null) {
				// Refuse
				commandAck = GroupCoordinationCommandAck.createKoCommand(message.getID(), groupDescriptor);
				sendMessage(MessageSubjects.GROUP_COORDINATION_COMMAND_ACK, commandAck, sender);
				return;
			}
			
			if (groupDescriptor.isLeader(currentNodeDescriptor) && 
					coordinationStrategy.shouldAcceptToJoin(remoteGroup)) {
				// Destroy this group and join the other one as member
				node.getGroupCommunicationDispatcher().removeGroup(this);
				
				remoteGroup.addFollower(currentNodeDescriptor);
				node.getGroupCommunicationDispatcher().addGroupManager(
						GroupCommunicationManager.createGroupCommunicationManager(node, remoteGroup));
				
				commandAck = GroupCoordinationCommandAck.createOkCommand(message.getID(), groupDescriptor);
				sendMessage(GROUP_COORDINATION_COMMAND_ACK, commandAck, sender);
				// The leader will then groupcast an updated descriptor
				return;
			} else {
				// A remote node has asked this node do dismantle this group
				// And join its group but this group is not empty...
				commandAck = GroupCoordinationCommandAck.createKoCommand(message.getID(), groupDescriptor);
				sendMessage(MessageSubjects.GROUP_COORDINATION_COMMAND_ACK, commandAck, sender);
				return;
			}
		} else if (MIGRATE_TO_GROUP.equals(commandType)) {
			if (!groupDescriptor.isLeader(currentNodeDescriptor) && 
					coordinationStrategy.shouldAcceptToMigrate(remoteGroup)) {
				// Send the leader a message saying that this node will
				// leave this group
				GroupFollowerCommand followerCommand = 
						GroupFollowerCommand.createLeavingNoticeCommand(
								groupDescriptor);
				sendFollowerCommand(followerCommand, groupDescriptor.getLeader());
				
				node.getGroupCommunicationDispatcher().removeGroup(this);
				
				remoteGroup.addFollower(currentNodeDescriptor);
				node.getGroupCommunicationDispatcher().addGroupManager(
						GroupCommunicationManager.createGroupCommunicationManager(node, remoteGroup));
				
				commandAck = GroupCoordinationCommandAck.createOkCommand(message.getID(), groupDescriptor);
				sendMessage(GROUP_COORDINATION_COMMAND_ACK, commandAck, sender);
				// The leader will then groupcast an updated descriptor
				return;
			} else {
				// A remote node has asked this node do migrate to its group
				commandAck = GroupCoordinationCommandAck.createKoCommand(message.getID(), groupDescriptor);
				sendMessage(MessageSubjects.GROUP_COORDINATION_COMMAND_ACK, commandAck, sender);
				return;
			}
		} else if (ADOPT_GROUP.equals(commandType)) {
			if (isLeader()) {
				handleFollowerJustJoined(sender);
				commandAck = GroupCoordinationCommandAck.createOkCommand(message.getID(), groupDescriptor);
				sendMessage(GROUP_COORDINATION_COMMAND_ACK, commandAck, sender);
				return;
			} else {
				commandAck = GroupCoordinationCommandAck.createKoCommand(message.getID(), groupDescriptor);
				sendMessage(MessageSubjects.GROUP_COORDINATION_COMMAND_ACK, commandAck, sender);
				return;
			}
		} else {
			// unhandled commands..
			throw new RuntimeException("Unknown GroupCoordinationCommand: " + commandType);
		}
	}


	/**
	 * @param groupDescriptor the groupDescriptor to set
	 */
	private void setGroupDescriptor(GroupDescriptor groupDescriptor) {
		this.groupDescriptor = groupDescriptor;
		coordinationStrategy = new LoadBalancingGroupCoordinationStrategy(groupDescriptor);
	}

	public void deleteGroup() {
		if (isLeader()) {
			// Propagates down
			GroupLeaderCommand forwardedMessage = 
				GroupLeaderCommand.createDeleteCommand(groupDescriptor);
			sendMessageToFollowers(forwardedMessage);
			// Propagates UP
			if (followedParentManager != null) {
				followedParentManager.deleteGroup();
			}
		} else {
			// ask the leader
			GroupFollowerCommand command =
				GroupFollowerCommand.createDeleteCommand(groupDescriptor);
			sendFollowerCommand(command, groupDescriptor.getLeader());
		}
	}

	interface LeadedChildCommandDelegate {
		public void invokeCommand(GroupCommunicationManager manager, Serializable message);
	}
	
	class DeleteGroupCommandDelegate implements LeadedChildCommandDelegate {

		@Override
		public void invokeCommand(GroupCommunicationManager manager, Serializable message) {
			node.getGroupCommunicationDispatcher().removeGroup(manager);
		}
	}
	
	class GroupDiscoveredCommandDelegate implements LeadedChildCommandDelegate {

		@Override
		public void invokeCommand(GroupCommunicationManager manager, Serializable message) {
			GroupDescriptor discoveredGroup = (GroupDescriptor)message;
			GroupCommunicationManager matchingManager = node.getGroupCommunicationDispatcher().getGroupManagerWithName(
					discoveredGroup.getFriendlyName());
			if (matchingManager != null) {
				matchingManager.handleMatchingGroupDiscovered(discoveredGroup);
			}
		}
	}

	void forwardTupleMessage(TupleMessage message, NodeDescriptor sender) {
		Scope scope = message.getScope();
		if (scope == Scope.GROUP) {
			GroupDescriptor recipient = (GroupDescriptor)message.getRecipient();
			if (!groupDescriptor.equals(recipient)) {
				// Not for this group
				return;
			}
		}
		
		if(isLeader()) {
			sendTupleToFollowers(message, sender);
		} else {
			sendTupleToLeader(message, sender);
		}
		// TODO if Scope == NETWORK this must be a universe group.
		if (scope == Scope.HIERARCHY || scope == Scope.NETWORK) {
			if (followedParentManager != null) {
				followedParentManager.forwardTupleMessage(message, sender);
			}
		} 
	}


	private void sendTupleToFollowers(TupleMessage message,
			NodeDescriptor sender) {
		for (NodeDescriptor follower: groupDescriptor.getFollowers()) {
				if (sender.equals(follower)) {
					continue;
				}
				sendMessage(TupleMessage.TUPLE_MESSAGE, message, follower);
			}
	}


	private void sendTupleToLeader(TupleMessage message, NodeDescriptor sender) {
		NodeDescriptor leader = groupDescriptor.getLeader();
		if (leader.equals(sender)) {
			return;
		}
		sendMessage(TupleMessage.TUPLE_MESSAGE, message, leader);
	}
}
