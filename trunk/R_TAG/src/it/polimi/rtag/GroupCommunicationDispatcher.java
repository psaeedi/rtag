/**
 * 
 */
package it.polimi.rtag;

import java.util.*;
import java.io.Serializable;

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
	PacketListener, GroupDiscoveredNotificationListener{

	private static final String[] SUBJECTS = {
		GROUP_CREATED_NOTIFICATION,
		GROUP_LEADER_COMMAND,
		GROUP_LEADER_COMMAND_ACK,
		GROUP_FOLLOWER_COMMAND,
		GROUP_FOLLOWER_COMMAND_ACK,
		GROUP_COORDINATION_COMMAND,
		GROUP_COORDINATION_COMMAND_ACK,
		GROUP_DISCOVERED_NOTIFICATION
	};
	
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
	
	public void addGroupManager(GroupCommunicationManager manager) {
		GroupDescriptor groupDescriptor = manager.getGroupDescriptor();
		if (groupDescriptor.isLeader(node.getID())) {
			synchronized (leadedGroups) {
				leadedGroups.add(manager);				
			}
		} else {
			synchronized (followedGroups) {
				followedGroups.add(manager);				
			}
		}
	}
	
	public GroupCommunicationManager getLeadedGroupByUUID(UUID query) {
		synchronized (leadedGroups) {
			for (GroupCommunicationManager manager: leadedGroups) {
				GroupDescriptor localGroup = manager.getGroupDescriptor();
				if (localGroup.getUniqueId().equals(query)) {
					return manager;
				}
			}
			return null;
		}
	}
	
	public GroupCommunicationManager getFollowedGroupByUUID(UUID query) {
		synchronized (followedGroups) {
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
		synchronized (leadedGroups) {
			for (GroupCommunicationManager manager: leadedGroups) {
				GroupDescriptor localGroup = manager.getGroupDescriptor();
				if (localGroup.getFriendlyName().equals(query)) {
					return manager;
				}
			}
			return null;
		}
	}
	
	public GroupCommunicationManager getFollowedGroupByFriendlyName(String query) {
		synchronized (followedGroups) {
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
		// We create a queue for this dispatcher
		overlay.setTrafficClass(this.getClass().toString(),
				this.getClass().toString());
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
	@Override
	public void handleGroupDiscovered(NodeDescriptor sender,
			GroupDescriptor groupDescriptor) {
		GroupCommunicationManager manager = getLeadedGroupByFriendlyName(
				groupDescriptor.getFriendlyName());
		if (manager != null) {
			// A group matching a leaded group has been found.
			// The manager will attempt to create a hierarchy.
			manager.handleGroupDiscovered(sender, groupDescriptor);
			return;
		} 
		
		manager = getFollowedGroupByFriendlyName(
				groupDescriptor.getFriendlyName());
		if (manager != null) {
			// A group matching a followed group has been found.
			// The manager will attempt to create a hierarchy.
			manager.handleGroupDiscovered(sender, groupDescriptor);
			return;
		} 
	}
	
	@Override
	public void notifyPacketArrived(String subject, NodeDescriptor sender,
			Serializable packet) {
		
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
		} else if (GROUP_CREATED_NOTIFICATION.equals(subject)) {
			handleMessageGroupCreatedNotification(sender, (GroupDescriptor)packet);
		} else if (GROUP_DISCOVERED_NOTIFICATION.equals(subject)) {
			handleGroupDiscovered(sender, (GroupDescriptor)packet);
		}
	}


	private void handleMessageGroupCreatedNotification(NodeDescriptor sender,
			GroupDescriptor groupDescriptor) {
		GroupCommunicationManager manager = getLeadedGroupByFriendlyName(
				groupDescriptor.getFriendlyName());
		if (manager != null) {
			// A group matching a leaded group has been found.
			// The manager will attempt to create a hierarchy.
			manager.handleMessageGroupCreatedNotification(sender, groupDescriptor);
		}
	}


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
	public ArrayList<GroupCommunicationManager> getLeadedGroups() {
		return leadedGroups;
	}

	/**
	 * @return the followedGroups
	 */
	public ArrayList<GroupCommunicationManager> getFollowedGroups() {
		return followedGroups;
	}

	public void reassignGroup(GroupCommunicationManager manager) {
		if (manager.getGroupDescriptor().isLeader(node.getID())) {
			if (followedGroups.contains(manager)) {
				followedGroups.remove(manager);
				leadedGroups.add(manager);
			}
			else {
				addGroupManager(manager);
			}
		} else  {
			if (leadedGroups.contains(manager)) {
				leadedGroups.remove(manager);
				followedGroups.add(manager);
			}
			else {
				addGroupManager(manager);
			}
		}
	}
	
	public void removeGroup(GroupCommunicationManager groupCommunicationManager) {
		if (followedGroups.contains(groupCommunicationManager)) {
			followedGroups.remove(groupCommunicationManager);
		}
		if (leadedGroups.contains(groupCommunicationManager)) {
			leadedGroups.remove(groupCommunicationManager);
		}
		overlay.removeNeighborhoodChangeListener(groupCommunicationManager);
	}
	
	public GroupDescriptor getLocalUniverse() {
		synchronized (leadedGroups) {
			for (GroupCommunicationManager manager: leadedGroups) {
				GroupDescriptor localGroup = manager.getGroupDescriptor();
				if (localGroup.isUniverse()) {
					return localGroup;
				}
			}
		}
		synchronized (followedGroups) {
			for (GroupCommunicationManager manager: followedGroups) {
				GroupDescriptor localGroup = manager.getGroupDescriptor();
				if (localGroup.isUniverse()) {
					return localGroup;
				}
			}
		}
		return null;
	}
	
	public GroupDescriptor getGroupWithName(String friendlyName) {
		GroupCommunicationManager manager = null;
		synchronized (leadedGroups) {
			manager = getLeadedGroupByFriendlyName(friendlyName);
			if (manager != null) {
				return manager.getGroupDescriptor();
			}
		}
		synchronized (followedGroups) {
			manager = getFollowedGroupByFriendlyName(friendlyName);
			if (manager != null) {
				return manager.getGroupDescriptor();
			}
		}
		return null;
	}

}
