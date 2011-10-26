/**
 * 
 */
package it.polimi.rtag;

import java.util.*;
import java.io.Serializable;

import lights.Field;
import lights.Tuple;
import lights.TupleSpace;
import lights.interfaces.TupleSpaceException;

import polimi.reds.NodeDescriptor;
import polimi.reds.broker.overlay.Overlay;
import polimi.reds.broker.overlay.PacketListener;

import it.polimi.rtag.messaging.GroupCoordinationCommand;
import it.polimi.rtag.messaging.GroupCoordinationCommandAck;
import it.polimi.rtag.messaging.GroupFollowerCommand;
import it.polimi.rtag.messaging.GroupFollowerCommandAck;
import it.polimi.rtag.messaging.GroupLeaderCommand;
import it.polimi.rtag.messaging.GroupLeaderCommandAck;

import static it.polimi.rtag.messaging.MessageSubjects.*;


/**
 * 
 * This class receives all the group communications and forward
 * them to the most suitable {@link GroupCommunicationManager}.</p>
 * 
 * Each node has a dispatcher , and overlay is connected to the dispatcher.
 * 
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class GroupCommunicationDispatcher implements 
	PacketListener{

	private TupleSpace tupleSpace = new TupleSpace();
	
	private static final String[] SUBJECTS = {
		GROUP_LEADER_COMMAND,
		GROUP_LEADER_COMMAND_ACK,
		GROUP_FOLLOWER_COMMAND,
		GROUP_FOLLOWER_COMMAND_ACK,
		GROUP_COORDINATION_COMMAND,
		GROUP_COORDINATION_COMMAND_ACK,
		GROUP_DISCOVERED_NOTIFICATION
	};
	
	private Object lock = new Object();
	
	private Node node;
	private Overlay overlay;
	
	// TODO synchronize them
	private ArrayList<GroupCommunicationManager> leadedGroups = new ArrayList<GroupCommunicationManager>();
	private ArrayList<GroupCommunicationManager> followedGroups = new ArrayList<GroupCommunicationManager>();
	
	/**
	 * @param overlay
	 */
	public GroupCommunicationDispatcher(Node node) {
		this.node = node;
		setOverlay(node.getOverlay());
	}
	
	private void notifyNewGroupManagerOfExistingManagers(
			GroupCommunicationManager newManager) {
		for (GroupCommunicationManager manager: leadedGroups) {
			if (!manager.equals(newManager)) {
				newManager.handleGroupManagerAdded(manager);
			}
		}
		for (GroupCommunicationManager manager: followedGroups) {
			if (!manager.equals(newManager)) {
				newManager.handleGroupManagerAdded(manager);
			}
		}
	}
	
	private void notifyGroupManagerWasAdded(GroupCommunicationManager newManager) {
		for (GroupCommunicationManager manager: leadedGroups) {
			if (!manager.equals(newManager)) {
				manager.handleGroupManagerAdded(newManager);
			}
		}
		for (GroupCommunicationManager manager: followedGroups) {
			if (!manager.equals(newManager)) {
				manager.handleGroupManagerAdded(newManager);
			}
		}
	}
	
	private void notifyGroupManagerWasRemoved(GroupCommunicationManager newManager) {
		for (GroupCommunicationManager manager: leadedGroups) {
			if (!manager.equals(newManager)) {
				manager.handleGroupManagerRemoved(newManager);
			}
		}
		for (GroupCommunicationManager manager: followedGroups) {
			if (!manager.equals(newManager)) {
				manager.handleGroupManagerRemoved(newManager);
			}
		}
	}
	
	void addGroupManager(GroupCommunicationManager manager) {
		GroupDescriptor groupDescriptor = manager.getGroupDescriptor();	
		synchronized (lock) {
			if (groupDescriptor.isLeader(node.getNodeDescriptor())) {
				if (getLeadedGroupByFriendlyName(groupDescriptor.getFriendlyName()) != null) {
					throw new RuntimeException("Already leading a group matching: " + groupDescriptor);
				}
				leadedGroups.add(manager);
				overlay.addNeighborhoodChangeListener(manager);
				notifyGroupManagerWasAdded(manager);
				notifyNewGroupManagerOfExistingManagers(manager);
			} else {
				if (getFollowedGroupByFriendlyName(groupDescriptor.getFriendlyName()) != null) {
					throw new RuntimeException("Already following a group matching: " + groupDescriptor);
				}
				followedGroups.add(manager);
				overlay.addNeighborhoodChangeListener(manager);
				notifyGroupManagerWasAdded(manager);
				notifyNewGroupManagerOfExistingManagers(manager);
			}
		}
		
	}
	
	GroupCommunicationManager getLeadedGroupByUUID(UUID query) {
		synchronized (lock) {
			for (GroupCommunicationManager manager: leadedGroups) {
				GroupDescriptor localGroup = manager.getGroupDescriptor();
				if (localGroup.getUniqueId().equals(query)) {
					return manager;
				}
			}
			return null;
		}
	}
	
	GroupCommunicationManager getFollowedGroupByUUID(UUID query) {
		synchronized (lock) {
			for (GroupCommunicationManager manager: followedGroups) {
				GroupDescriptor localGroup = manager.getGroupDescriptor();
				if (localGroup.getUniqueId().equals(query)) {
					return manager;
				}
			}
			return null;
		}
	}

	GroupCommunicationManager getLeadedGroupByFriendlyName(String query) {
		synchronized (lock) {
			for (GroupCommunicationManager manager: leadedGroups) {
				GroupDescriptor localGroup = manager.getGroupDescriptor();
				if (localGroup.getFriendlyName().equals(query)) {
					return manager;
				}
			}
			return null;
		}
	}
	
	GroupCommunicationManager getFollowedGroupByFriendlyName(String query) {
		synchronized (lock) {
			for (GroupCommunicationManager manager: followedGroups) {
				GroupDescriptor localGroup = manager.getGroupDescriptor();
				if (localGroup.getFriendlyName().equals(query)) {
					return manager;
				}
			}
			return null;
		}
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
		for (String subject: SUBJECTS) {
			overlay.addPacketListener(this, subject);
		}
	}


	/**
	 * The dispatcher will forward the group discovery notification
	 * to the leaded group which matches the discovered one, if any.
	 * 
	 * @param sender the node sending the group notification
	 * @param groupDescriptor the remote group descriptor
	 * 
	 * @see it.polimi.rtag.GroupDiscoveredNotificationListener#handleGroupDiscovered
	 * (polimi.reds.NodeDescriptor, it.polimi.rtag.GroupDescriptor)
	 */
	public void handleGroupDiscovered(NodeDescriptor sender,
			GroupDescriptor groupDescriptor) {
		
		updateTupleSpace(groupDescriptor);
		
		GroupCommunicationManager manager = getLocalUniverseManager();
		manager.handleGroupDiscovered(sender, groupDescriptor);
	}
	
	void addToTupleSpace(GroupDescriptor groupDescriptor) {
		Tuple tuple = new Tuple();
		tuple.add(new Field().setValue(groupDescriptor.getLeader()))
				.add(new Field().setValue(groupDescriptor.getFriendlyName()));
		try {
			tupleSpace.out(tuple);
		} catch (TupleSpaceException e) {
			e.printStackTrace();
		}
	}
	
	void updateTupleSpace(GroupDescriptor groupDescriptor) {
		removeFromTupleSpace(groupDescriptor);
		addToTupleSpace(groupDescriptor);
	}

	void removeFromTupleSpace(GroupDescriptor groupDescriptor) {
		Tuple query = new Tuple();
		query.add(new Field().setType(NodeDescriptor.class))
			.add(new Field().setValue(groupDescriptor.getFriendlyName()));
			
		// Remove the tuple if it exist
		try {
			tupleSpace.rdp(query);
		} catch (TupleSpaceException e) {
			e.printStackTrace();
		}
	}
	
	NodeDescriptor queryTupleSpace(String friendlyName) {
		// TODO implement this
		Tuple query = new Tuple();
		query.add(new Field().setType(NodeDescriptor.class))
			.add(new Field().setValue(friendlyName));
		
		// TODO retuirn the leader
		return null;
	}
	
	
	@Override
	public void notifyPacketArrived(String subject, NodeDescriptor sender,
			Serializable packet) {
		
		// TODO return ko if a message was not handled?
		
		if (GROUP_FOLLOWER_COMMAND.equals(subject)) {
			handleMessageGroupFollowerCommand(sender, (GroupFollowerCommand)packet);
		} else if (GROUP_FOLLOWER_COMMAND_ACK.equals(subject)) {
			handleMessageGroupFollowerCommandAck(sender, (GroupFollowerCommandAck)packet);
		} else if (GROUP_LEADER_COMMAND.equals(subject)) {
			handleMessageGroupLeaderCommand(sender, (GroupLeaderCommand)packet);
		} else if (GROUP_LEADER_COMMAND_ACK.equals(subject)) {
			handleMessageGroupLeaderCommandAck(sender, (GroupLeaderCommandAck)packet);
		} else if (GROUP_COORDINATION_COMMAND.equals(subject)) {
			handleMessageGroupCoordinationCommand(sender, (GroupCoordinationCommand)packet);
		}else if (GROUP_COORDINATION_COMMAND_ACK.equals(subject)) {
			handleMessageGroupCoordinationCommandAck(sender, (GroupCoordinationCommandAck)packet);
		} else if (GROUP_DISCOVERED_NOTIFICATION.equals(subject)) {
			handleGroupDiscovered(sender, (GroupDescriptor)packet);
		}
	}


	/*
	private void handleMessageGroupCreatedNotification(NodeDescriptor sender,
			GroupDescriptor groupDescriptor) {
		GroupCommunicationManager manager = getLeadedGroupByFriendlyName(
				groupDescriptor.getFriendlyName());
		if (manager != null) {
			// A group matching a leaded group has been found.
			// The manager will attempt to create a hierarchy.
			manager.handleMessageGroupCreatedNotification(sender, groupDescriptor);
		} else {
			// we use the universe to propagate this message
			getLocalUniverseManager()
					.handleMessageGroupCreatedNotification(sender, groupDescriptor);
		}
	}*/


	private void handleMessageGroupCoordinationCommandAck(
			NodeDescriptor sender, GroupCoordinationCommandAck packet) {
		// Only a leader can receive an ack
		GroupCommunicationManager manager = getLeadedGroupByFriendlyName(
				packet.getGroupDescriptor().getFriendlyName());
		if (manager != null) {
			manager.handleMessageGroupCoordinationCommandAck(sender, packet);
		}
	}


	private void handleMessageGroupCoordinationCommand(
			NodeDescriptor sender, GroupCoordinationCommand packet) {
		GroupDescriptor remoteDescriptor = packet.getGroupDescriptor();
		// TODO add recipient group in coordination messages
		
		// Attempt to find a leaded group
		GroupCommunicationManager manager = getLeadedGroupByFriendlyName(
				remoteDescriptor.getFriendlyName());
		if (manager != null) {
			manager.handleMessageGroupCoordinationCommand(sender, packet);
			return;
		}
		// Try to find a followed group
		manager = getFollowedGroupByFriendlyName(
				remoteDescriptor.getFriendlyName());
		if (manager != null) {
			manager.handleMessageGroupCoordinationCommand(sender, packet);
			return;
		}
		
	}


	private void handleMessageGroupLeaderCommandAck(NodeDescriptor sender,
			GroupLeaderCommandAck packet) {
		GroupDescriptor remoteDescriptor = packet.getGroupDescriptor();
		// TODO add recipient group in coordination messages
		
		// Attempt to find a leaded group
		GroupCommunicationManager manager = getLeadedGroupByUUID(
				remoteDescriptor.getUniqueId());
		if (manager != null) {
			manager.handleMessageGroupLeaderCommandAck(sender, packet);
			return;
		}
	}


	private void handleMessageGroupLeaderCommand(NodeDescriptor sender,
			GroupLeaderCommand packet) {
		
		GroupDescriptor remoteDescriptor = packet.getGroupDescriptor();
		// TODO add recipient group in coordination messages
		
		// Attempt to find a leaded group
		GroupCommunicationManager manager = getFollowedGroupByUUID(
				remoteDescriptor.getUniqueId());
		if (manager != null) {
			manager.handleMessageGroupLeaderCommand(sender, packet);
			return;
		}
	}


	private void handleMessageGroupFollowerCommandAck(NodeDescriptor sender,
			GroupFollowerCommandAck packet) {
		GroupDescriptor remoteDescriptor = packet.getGroupDescriptor();
		// TODO add recipient group in coordination messages
		
		// Attempt to find a leaded group
		GroupCommunicationManager manager = getFollowedGroupByUUID(
				remoteDescriptor.getUniqueId());
		if (manager != null) {
			manager.handleMessageGroupFollowerCommandAck(sender, packet);
			return;
		}
	}


	private void handleMessageGroupFollowerCommand(NodeDescriptor sender,
			GroupFollowerCommand packet) {
		
		GroupDescriptor remoteDescriptor = packet.getGroupDescriptor();
		// TODO add recipient group in coordination messages
		
		// Attempt to find a leaded group
		GroupCommunicationManager manager = getLeadedGroupByUUID(
				remoteDescriptor.getUniqueId());
		if (manager != null) {
			manager.handleMessageGroupFollowerCommand(sender, packet);
			return;
		}
	}

	/**
	 * @return the leadedGroups
	 */
	ArrayList<GroupCommunicationManager> getLeadedGroups() {
		synchronized (lock) {
			return new ArrayList<GroupCommunicationManager>(leadedGroups);	
		}
	}

	/**
	 * @return the followedGroups
	 */
	ArrayList<GroupCommunicationManager> getFollowedGroups() {
		synchronized (lock) {
			return new ArrayList<GroupCommunicationManager>(followedGroups);			
		}
	}

	void reassignGroup(GroupCommunicationManager manager) {
		synchronized (lock) {
			if (manager.isLeader()) {
				if (followedGroups.contains(manager)) {
					followedGroups.remove(manager);
					notifyGroupManagerWasRemoved(manager);
					leadedGroups.add(manager);
					notifyGroupManagerWasAdded(manager);
				}
				else {
					//addGroupManager(manager);
					throw new RuntimeException(
						"What is this???.");
				}
			} else  {
				throw new RuntimeException(
						"Reassign group should only be invoked by a promoted leader.");
			}
		}
	}
	
	void removeGroup(GroupCommunicationManager manager) {
		boolean wasRemoved = false;
		synchronized (lock) {
			if (followedGroups.contains(manager)) {
				followedGroups.remove(manager);
				wasRemoved = true;
			}
			if (leadedGroups.contains(manager)) {
				leadedGroups.remove(manager);
				wasRemoved = true;
			}
			// Disconnect the manager
			if (wasRemoved) {
				overlay.removeNeighborhoodChangeListener(manager);
				notifyGroupManagerWasRemoved(manager);
				node.getTopologyManager().removeNodesForGroup(
						manager.getGroupDescriptor());
			}
		}
	}
	
	GroupCommunicationManager getLocalUniverseManager() {
		synchronized (lock) {
			for (GroupCommunicationManager manager: leadedGroups) {
				GroupDescriptor localGroup = manager.getGroupDescriptor();
				if (localGroup.isUniverse()) {
					return manager;
				}
			}
			for (GroupCommunicationManager manager: followedGroups) {
				GroupDescriptor localGroup = manager.getGroupDescriptor();
				if (localGroup.isUniverse()) {
					return manager;
				}
			}
		}
		return null;
	}
	
	GroupDescriptor getLocalUniverse() {
		GroupCommunicationManager manager = getLocalUniverseManager();
		if (manager != null) {
			return manager.getGroupDescriptor();
		} else {
			return null;
		}
	}
	
	GroupCommunicationManager getGroupManagerWithName(String friendlyName) {
		synchronized (lock) {
			GroupCommunicationManager manager = null;
			manager = getLeadedGroupByFriendlyName(friendlyName);
			if (manager != null) {
				return manager;
			}
			manager = getFollowedGroupByFriendlyName(friendlyName);
			if (manager != null) {
				return manager;
			}
		}
		return null;
	}
	
	GroupDescriptor getGroupWithName(String friendlyName) {
		GroupCommunicationManager manager = getGroupManagerWithName(friendlyName);
		if (manager != null) {
			return manager.getGroupDescriptor();
		}
		return null;
	}

	void removeAllGroupsAndDisconnect() {
		synchronized (lock) {
			for (int i = leadedGroups.size() - 1; i > -1; i--) {
				GroupCommunicationManager manager = leadedGroups.get(i);
				removeGroup(manager);
			}
			for (int i = leadedGroups.size() - 1; i > -1; i--) {
				GroupCommunicationManager manager = followedGroups.get(i);
				removeGroup(manager);
			}
		}
		for (String subject: SUBJECTS) {
			overlay.removePacketListener(this, subject);
		}
	}

	/**
	 * Removes all the groups matching the given name.
	 * This is invoked by the node when the application wants
	 * to leave a group.  
	 */
	void leaveGroupsWithName(String friendlyName) {
		synchronized (lock) {
			GroupCommunicationManager manager = null;
			manager = getLeadedGroupByFriendlyName(friendlyName);
			if (manager != null) {
				removeGroup(manager);
			}
			manager = getFollowedGroupByFriendlyName(friendlyName);
			if (manager != null) {
				removeGroup(manager);
			}
		}
	}

	/**
	 * Create a new group and inform the network
	 */
	GroupDescriptor joinGroupAndNotifyNetwork(String friendlyName) {
		GroupDescriptor descriptor = getGroupWithName(friendlyName);
		if (descriptor != null) {
			// Already in that group
			return descriptor;
		}
		try {
			GroupCommunicationManager manager =
					GroupCommunicationManager.createGroupCommunicationManager(
							node, UUID.randomUUID(), friendlyName);
			addGroupManager(manager);

			getLocalUniverseManager().sendGroupcast(GROUP_DISCOVERED_NOTIFICATION, 
					manager.getGroupDescriptor(), null);
			return manager.getGroupDescriptor();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	void deleteGroup(String friendlyName) {
		GroupCommunicationManager manager = 
			getGroupManagerWithName(friendlyName);
		if (manager == null) {
			throw new RuntimeException(
					"Cannot delete a group of which the node is not member.");
		}
		manager.deleteGroup();
		removeGroup(manager);
	}

	List<GroupDescriptor> getAllGroups() {
		ArrayList<GroupDescriptor> groups = new ArrayList<GroupDescriptor>();
		synchronized (lock) {
			for (GroupCommunicationManager manager: leadedGroups) {
				groups.add(manager.getGroupDescriptor());
			}
			for (GroupCommunicationManager manager: followedGroups) {
				groups.add(manager.getGroupDescriptor());
			}
		}
		return groups;
	}

}
