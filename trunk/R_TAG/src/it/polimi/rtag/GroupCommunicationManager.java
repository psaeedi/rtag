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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.UUID;

import lights.Tuple;

import polimi.reds.Message;
import polimi.reds.MessageID;
import polimi.reds.NodeDescriptor;
import polimi.reds.broker.overlay.AlreadyNeighborException;
import polimi.reds.broker.overlay.NeighborhoodChangeListener;
import polimi.reds.broker.overlay.NotRunningException;
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
public class GroupCommunicationManager implements NeighborhoodChangeListener, 
		GroupDiscoveredNotificationListener, GroupChangeListener {

	private Node node;
	
	private GroupDescriptor groupDescriptor;
	private NodeDescriptor currentNodeDescriptor;
	private Overlay overlay;
	private GroupCoordinationStrategy coordinationStrategy;
	
	private HashMap<MessageID, Message> pendingMessages = new HashMap<MessageID, Message>();
	
	PropertyChangeSupport groupChangeSupport;
	
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
			 UUID uniqueId, String friendlyName, Tuple description) {
		
		GroupDescriptor groupDescriptor = new GroupDescriptor(uniqueId, 
				friendlyName, node.getID(), description);
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
		// TODO implement this
		/*manager.groupChangeSupport.addPropertyChangeListener(UPDATE_DESCRIPTOR,
				node.getTopologyManager());*/
		return manager;
	}
	
	public static GroupCommunicationManager createChildGroupCommunicationManager(Node node,
			GroupDescriptor parentGroupDescriptor) {
		GroupDescriptor groupDescriptor = new GroupDescriptor(UUID.randomUUID(), parentGroupDescriptor.getFriendlyName(),
				node.getID(), parentGroupDescriptor.getDescription(), parentGroupDescriptor.getLeader());
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
		this.currentNodeDescriptor = node.getID();
		groupChangeSupport = new PropertyChangeSupport(this);
		setOverlay(overlay);
		setGroupDescriptor(groupDescriptor);		
	}
	
	public boolean isLeader() {
		return groupDescriptor.isLeader(currentNodeDescriptor);
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
		System.out.println("GM for " + currentNodeDescriptor + " notifyNeighborAdded " + addedNode);

		if (groupDescriptor.isMember(addedNode)) {
			// Already a member, nothing to be done.
			return;
		}
		
		try {
			overlay.send(MessageSubjects.GROUP_DISCOVERED_NOTIFICATION, 
					groupDescriptor, addedNode);
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
		System.out.println("GM " + groupDescriptor.getUniqueId() + " for " + 
				currentNodeDescriptor + " has been notified that " + 
				deadNode + " is dead");
		
		// If the dead node is the parent group simply set it to null
		if (groupDescriptor.isParentLeader(deadNode)) {
			groupDescriptor.setParentLeader(null);
			groupChangeSupport.firePropertyChange(UPDATE_DESCRIPTOR,
					null, groupDescriptor);
			
			if (isLeader()) {
				GroupLeaderCommand command = GroupLeaderCommand
						.createUpdateCommand(groupDescriptor);
				sendMessageToFollowers(command);
			}
			return;
		}
		
		if (!groupDescriptor.isMember(deadNode)) {
			// Not a group member
			return;
		}
		
		if (deadNode.equals(currentNodeDescriptor)) {
			// The current node is the dead one....
			return;
		}
		
		if (!groupDescriptor.isLeader(deadNode)) {
			groupDescriptor.removeFollower(deadNode);
			groupChangeSupport.firePropertyChange(UPDATE_DESCRIPTOR, null, groupDescriptor);
			if (isLeader()) {
				GroupLeaderCommand command = GroupLeaderCommand.createUpdateCommand(groupDescriptor);
				sendMessageToFollowers(command);
			} 
			return;
		} else {
			// promote a new leader!
			groupDescriptor.setLeader(null);
			//groupChangeSupport.firePropertyChange(UPDATE_DESCRIPTOR, null, groupDescriptor);
			
			NodeDescriptor newLeader = coordinationStrategy.electNewLeader();
			groupDescriptor.setLeader(newLeader);
			
			if (currentNodeDescriptor.equals(newLeader)) {
				// If the new leader was also a child leader we
				// need to close this group and move all the 
				// nodes to the other one.
				GroupCommunicationManager childManager = node.getGroupCommunicationDispatcher()
						.getLeadedGroupByFriendlyName(groupDescriptor.getFriendlyName());
				if (childManager != null) {
					childManager.migrateAllFollowers(groupDescriptor);
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
				groupChangeSupport.firePropertyChange(UPDATE_DESCRIPTOR, null, groupDescriptor);	
			}
		}	
	}
	
	/**
	 * When a leader crash and the new leader was already a child leader.
	 */
	private void migrateAllFollowers(GroupDescriptor remoteGroup) {
		for (NodeDescriptor node: remoteGroup.getFollowers()) {
			GroupCoordinationCommand command = GroupCoordinationCommand
					.createMigrateToGroupCommand(groupDescriptor);
			sendCoordinationCommand(command, node);
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
			groupDescriptor.removeFollower(sender);
			GroupLeaderCommand updateCommand = 
					GroupLeaderCommand.createUpdateCommand(groupDescriptor);
			sendMessageToFollowers(updateCommand);
			
			// Sending ack
			commandAck = GroupFollowerCommandAck.createOkCommand(
					message.getID(), groupDescriptor);
			sendMessage(MessageSubjects.GROUP_FOLLOWER_COMMAND_ACK, 
					commandAck, sender);
			return;
		} else {
			// Other commands...
		}
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
		} else {
			// unhandled commands..
		}
	}

	
	
	private void sendMessage(String subject, 
			Message message, NodeDescriptor recipient) {
		try {
			recipient = connectIfNotConnected(recipient);
			if (recipient == null) {
				System.err.println("CANNOT CONNECT TO: " + recipient);
				return;
			}
			System.out.println("GM " + groupDescriptor.getUniqueId() +
					currentNodeDescriptor + " sending " + message + " to" +
					recipient);
			overlay.send(subject, message, recipient);
		} catch (Exception e) {
			System.err.println("Catched: " + e.getMessage());
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
			GroupCommunicationManager leadedGroupManager = node.getGroupCommunicationDispatcher().
				getLeadedGroupByFriendlyName(groupDescriptor.getFriendlyName());
			if (leadedGroupManager == null && coordinationStrategy.shouldAcceptToCreateAChild()) {
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
		} else {
			// unhandled commands..
		}
	}


	private void inviteFollowersToMigrate(GroupDescriptor remoteGroup) {
		System.out.println("_____________inviteFollowersToMigrate1");
		for (NodeDescriptor follower: remoteGroup.getFollowers()) {
			if (remoteGroup.getLeader() == null || 
					coordinationStrategy.shouldSuggestToMigrate(remoteGroup, follower)) {
				System.out.println("_____________inviteFollowersToMigrate2");
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
	@Override
	public void handleGroupDiscovered(NodeDescriptor sender, 
			GroupDescriptor remoteGroupDescriptor) {

		if (!groupDescriptor.hasSameName(remoteGroupDescriptor)) {
			// The two group does not match
			return;
		}
		
		if (groupDescriptor.getUniqueId().equals(remoteGroupDescriptor.getUniqueId())) {
			// Same group
			return;
		}
		
		if (!isLeader()) {
			return;
		}
		
		NodeDescriptor remoteLeader = remoteGroupDescriptor.getLeader();	
		
		// We first attempt to merge then to join
		if (coordinationStrategy.shouldInviteToMerge(remoteGroupDescriptor)) {
			GroupCoordinationCommand command = 
					GroupCoordinationCommand.createMergeGroupCommand(groupDescriptor);
			sendCoordinationCommand(command, remoteLeader);
		} else if (coordinationStrategy.shouldInviteToJoin(remoteGroupDescriptor)) {
			GroupCoordinationCommand command = 
					GroupCoordinationCommand.createJoinMyGroupCommand(groupDescriptor);
			sendCoordinationCommand(command, remoteLeader);
		}
	}
	
	private NodeDescriptor connectIfNotConnected(NodeDescriptor descriptor) {
		// We are receiving a notification, we should then first connect the remote leader.
		if (overlay.isNeighborOf(descriptor)) {
			return descriptor;
		}
		System.out.println("òòòòòòòòòòòòòòòò Connecting to " + descriptor);
		NodeDescriptor node = null;
		for (String url: descriptor.getUrls()) {
			try {
				node = overlay.addNeighbor(url);
			} catch (AlreadyNeighborException ex) {
				return descriptor;
			} catch (Exception ex) {
				continue;
			}
			
		}
		return node;
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
				groupDescriptor.setParentLeader(sender);
				groupChangeSupport.firePropertyChange(UPDATE_DESCRIPTOR, null, groupDescriptor);
				
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
				groupDescriptor.setParentLeader(null);
				groupChangeSupport.firePropertyChange(UPDATE_DESCRIPTOR, null, groupDescriptor);
			}
			GroupLeaderCommand updateCommand = GroupLeaderCommand.createUpdateCommand(groupDescriptor);
			sendMessageToFollowers(updateCommand);
			return;
		}else {
			// unhandled commands..
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
		groupChangeSupport.firePropertyChange(UPDATE_DESCRIPTOR, null, groupDescriptor);
		GroupLeaderCommand command = GroupLeaderCommand.createUpdateCommand(groupDescriptor);
		sendMessageToFollowers(command);
		createChildIfNecessary();
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
			if (isLeader() && coordinationStrategy.shouldAcceptToMerge(remoteGroup)) {
				// The current node has accepted the remote as a parent node
				// From now on the current node will be both leader of his current
				// group and follower of his parent group.
				remoteGroup.addFollower(currentNodeDescriptor);
				node.getGroupCommunicationDispatcher().addGroupManager(
						GroupCommunicationManager.createGroupCommunicationManager(node, remoteGroup));
				commandAck = GroupCoordinationCommandAck.createOkCommand(message.getID(), groupDescriptor);
				sendMessage(GROUP_COORDINATION_COMMAND_ACK, commandAck, sender);
				
				// Update the descriptor and send updates to all his followers
				groupDescriptor.setParentLeader(sender);
				groupChangeSupport.firePropertyChange(UPDATE_DESCRIPTOR, null, groupDescriptor);
				
				GroupLeaderCommand updateCommand = GroupLeaderCommand.createUpdateCommand(groupDescriptor);
				sendMessageToFollowers(updateCommand);
				return;
			} else {
				commandAck = GroupCoordinationCommandAck.createKoCommand(message.getID(), groupDescriptor);
				sendMessage(GROUP_COORDINATION_COMMAND_ACK, commandAck, sender);
				return;
			}	
		} else if (JOIN_MY_GROUP.equals(commandType)) {
			// If this node is leader of a child group it will refuse
			GroupCommunicationManager parentManager = node.getGroupCommunicationDispatcher()
					.getFollowedGroupByFriendlyName(remoteGroup.getFriendlyName());
			if (parentManager != null) {
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
		}
	}


	/**
	 * @param groupDescriptor the groupDescriptor to set
	 */
	private void setGroupDescriptor(GroupDescriptor groupDescriptor) {
		groupChangeSupport.firePropertyChange(UPDATE_DESCRIPTOR, this.groupDescriptor, groupDescriptor);
		this.groupDescriptor = groupDescriptor;
		coordinationStrategy = new ProbabilisticLoadBalancingGroupCoordinationStrategy(groupDescriptor);
	}


	/**
	 * Leaded child groups receive an update every time
	 * their followed father updates.
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent property) {
		if (UPDATE_DESCRIPTOR.equals(property.getPropertyName())) {
			GroupDescriptor fatherDescriptor = (GroupDescriptor)property.getNewValue();
			if (fatherDescriptor == null) {
				return;
			}
			if (!fatherDescriptor.isFollower(currentNodeDescriptor)) {
				throw new RuntimeException("Only followers should receive this update.");
			}
			if (fatherDescriptor.getUniqueId().equals(groupDescriptor.getUniqueId())) {
				throw new RuntimeException("The two groups are not father and son");
			}
			if (!fatherDescriptor.getFriendlyName().equals(groupDescriptor.getFriendlyName())) {
				throw new RuntimeException("The two groups have a different name. Local: " + 
						groupDescriptor.getFriendlyName() +
						" remote: " + fatherDescriptor.getFriendlyName());
			}
			inviteFollowersToMigrate(fatherDescriptor);
		}
	}

}
