/**
 * 
 */
package it.polimi.rtag;

import java.util.*;

import polimi.reds.NodeDescriptor;
import polimi.reds.broker.overlay.Overlay;

import it.polimi.rtag.messaging.TupleMessageAck;
import it.polimi.rtag.messaging.TupleNodeNotification;


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
	private TupleSpaceManager tupleSpaceManager;
	
	public TupleSpaceManager getTupleSpaceManager() {
		return tupleSpaceManager;
	}

	public void setTupleSpaceManager(TupleSpaceManager tupleSpaceManager) {
		this.tupleSpaceManager = tupleSpaceManager;
	}

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
		System.err.println("AAAA addGroupManager node " + node.getNodeDescriptor() + " greoup " + manager.getGroupDescriptor().getFriendlyName());
		
		
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
				GroupCommunicationManager alreadyFollowed = getFollowedGroupByFriendlyName(groupDescriptor.getFriendlyName());
				if (alreadyFollowed != null) {
					throw new RuntimeException(node.getNodeDescriptor() + " cannot follow " +
							groupDescriptor +
							" because it is already following a group matching: " +
							alreadyFollowed.getGroupDescriptor());
				}
				followedGroups.add(manager);
				overlay.addNeighborhoodChangeListener(manager);
				notifyGroupManagerWasAdded(manager);
				notifyNewGroupManagerOfExistingManagers(manager);
			}
		}
		tupleSpaceManager.handleNewLocalGroupCreated(manager);
	}
	
	/*
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
	}*/
	
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

	public GroupCommunicationManager getLeadedGroupByFriendlyName(String query) {
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
	 * @return the leadedGroups
	 */
	public ArrayList<GroupCommunicationManager> getLeadedGroups() {
		synchronized (lock) {
			return new ArrayList<GroupCommunicationManager>(leadedGroups);	
		}
	}

	/**
	 * @return the followedGroups
	 */
	public ArrayList<GroupCommunicationManager> getFollowedGroups() {
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
	
	void removeGroupManager(GroupCommunicationManager manager) {
		boolean wasRemoved = false;
		synchronized (lock) {
			if (followedGroups.contains(manager)) {
				followedGroups.remove(manager);
				wasRemoved = true;
			}
			if (!wasRemoved && leadedGroups.contains(manager)) {
				leadedGroups.remove(manager);
				wasRemoved = true;
			}
		}
		// Disconnect the manager
		if (wasRemoved) {
			overlay.removeNeighborhoodChangeListener(manager);
			notifyGroupManagerWasRemoved(manager);
			node.getTopologyManager().removeNodesForGroup(
					manager.getGroupDescriptor());
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
	
	GroupCommunicationManager getGroupManagerForHierarchy(
			String hierarchyName) {
		synchronized (lock) {
			GroupCommunicationManager manager = null;
			manager = getLeadedGroupByFriendlyName(hierarchyName);
			if (manager != null) {
				return manager;
			}
			manager = getFollowedGroupByFriendlyName(hierarchyName);
			if (manager != null) {
				return manager;
			}
		}
		return null;
	}
	
	GroupDescriptor getGroupForHierarchy(String friendlyName) {
		GroupCommunicationManager manager = getGroupManagerForHierarchy(friendlyName);
		if (manager != null) {
			return manager.getGroupDescriptor();
		}
		return null;
	}

	void removeAllGroupsAndDisconnect() {
		synchronized (lock) {
			for (int i = leadedGroups.size() - 1; i > -1; i--) {
				GroupCommunicationManager manager = leadedGroups.get(i);
				removeGroupManager(manager);
			}
			for (int i = leadedGroups.size() - 1; i > -1; i--) {
				GroupCommunicationManager manager = followedGroups.get(i);
				removeGroupManager(manager);
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
				removeGroupManager(manager);
			}
			manager = getFollowedGroupByFriendlyName(friendlyName);
			if (manager != null) {
				removeGroupManager(manager);
			}
		}
	}

	/**
	 * Create a new group and inform the network
	 */
	GroupDescriptor joinGroupAndNotifyNetwork(String friendlyName) {
		GroupDescriptor descriptor = getGroupForHierarchy(friendlyName);
		if (descriptor != null) {
			// Already in that group
			return descriptor;
		}
		try {
			GroupCommunicationManager manager =
					GroupCommunicationManager.createGroupCommunicationManager(
							node, UUID.randomUUID(), friendlyName);
			addGroupManager(manager);
			
			GroupDescriptor parentGroup = 
					tupleSpaceManager.getLeaderForHierarchy(friendlyName);
			if (parentGroup != null) {
				manager.connectIfNotConnected(parentGroup.getLeader());
			}
			tupleSpaceManager.setLeaderForHierarchy(manager.getGroupDescriptor());
			return manager.getGroupDescriptor();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	void deleteGroup(String friendlyName) {
		GroupCommunicationManager manager = 
			getGroupManagerForHierarchy(friendlyName);
		if (manager == null) {
			throw new RuntimeException(
					"Cannot delete a group of which the node is not member.");
		}
		manager.deleteGroup();
		removeGroupManager(manager);
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

	public GroupCommunicationManager getGroupManagerForDescriptor(
			GroupDescriptor recipient) {
		synchronized (lock) {
			for (GroupCommunicationManager manager: leadedGroups) {
				if (manager.getGroupDescriptor()
						.getUniqueId().equals(recipient.getUniqueId())) {
					return manager;
				}
			}
			for (GroupCommunicationManager manager: followedGroups) {
				if (manager.getGroupDescriptor()
						.getUniqueId().equals(recipient.getUniqueId())) {
					return manager;
				}
			}
		}
		return null;
	}

	public void handleNodeMessage(TupleNodeNotification message,
			NodeDescriptor sender) {
		String command = message.getCommand();
		if (TupleNodeNotification.NOTIFY_GROUP_EXISTS.equals(command)) {
			// Do nothing
			/*
			GroupDescriptor remoteGroup = (GroupDescriptor)message.getContent();
			GroupCommunicationManager manager = 
					getGroupManagerForHierarchy(remoteGroup.getFriendlyName());
			if (manager != null) {
				if (node.getNodeDescriptor().getID().compareTo(sender.getID()) > 0) {
					return;
					//Only one should take the initiative
				}
				manager.handleRemoteGroupDiscovered(remoteGroup);
			} else {
				System.out.println("handleNodeMessage"+node.getNodeDescriptor() +
						" Manager null for group: " + remoteGroup);
			}*/
			// TODO ssend group and hierarchy tuples
			
		} else if (TupleNodeNotification.ALLOW_TO_JOIN_GROUP.equals(command)) {
			GroupDescriptor remoteGroup = (GroupDescriptor)message.getContent();
			GroupCommunicationManager manager = getGroupManagerForDescriptor(remoteGroup);
			if (manager != null) {
				manager.handleRequestToJoin(message, sender);
			} else {
				if (!remoteGroup.getLeader().equals(node.getNodeDescriptor())) {
					throw new RuntimeException("handleNodeMessage node " + 
						   node.getNodeDescriptor() + "not leader of " + remoteGroup);
				}
				
				// the source group has been dismantled
				System.err.println("handleNodeMessage " + node.getNodeDescriptor() + 
						" Manager null for group: " + remoteGroup);
			}
		}
	}
	
	public void handleNodeMessageAck(TupleMessageAck message,
			NodeDescriptor sender) {
		// TODO the control is done twice, here and in the manager...
		TupleNodeNotification notification = (TupleNodeNotification)message.getOriginalMessage();
		GroupDescriptor remoteGroup = (GroupDescriptor)notification.getContent();
		GroupCommunicationManager manager = 
				getLeadedGroupByFriendlyName(remoteGroup.getFriendlyName());
		if (manager != null) {
			manager.handleTupleMessageAck(message, sender);
		} else {
			System.err.println(node.getNodeDescriptor() + 
					" Manager null for group: " + remoteGroup);
		}
	}
	

}
