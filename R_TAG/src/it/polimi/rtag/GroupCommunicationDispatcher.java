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

import it.polimi.rtag.messaging.TupleGroupCommand;
import it.polimi.rtag.messaging.TupleGroupCommandAck;
import it.polimi.rtag.messaging.TupleMessage;


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
public class GroupCommunicationDispatcher {


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
		this.overlay = node.getOverlay();
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
		
		GroupCommunicationManager manager = getLocalUniverseManager();
		manager.handleGroupDiscovered(sender, groupDescriptor);
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
