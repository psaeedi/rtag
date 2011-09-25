/**
 * 
 */
package it.polimi.rtag;

import static it.polimi.rtag.messaging.MessageSubjects.*;

import it.polimi.rtag.messaging.GroupCoordinationCommand;
import it.polimi.rtag.messaging.GroupCoordinationCommandAck;
import it.polimi.rtag.messaging.GroupFollowerCommand;
import it.polimi.rtag.messaging.GroupFollowerCommandAck;
import it.polimi.rtag.messaging.GroupLeaderCommand;
import it.polimi.rtag.messaging.GroupLeaderCommandAck;

import java.io.Serializable;
import java.util.ArrayList;

import polimi.reds.NodeDescriptor;
import polimi.reds.broker.overlay.Overlay;
import polimi.reds.broker.overlay.PacketListener;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 * This class receives all the group communications and forward
 * them to the most suitable {@link GroupCommunicationManager}.
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
	
	// TODO use a better collection
	// TODO syncronize them
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
			leadedGroups.add(manager);
		} else {
			followedGroups.add(manager);
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
	 * @see it.polimi.rtag.GroupDiscoveredNotificationListener#handleGroupDiscovered(polimi.reds.NodeDescriptor, it.polimi.rtag.GroupDescriptor)
	 */
	@Override
	public void handleGroupDiscovered(NodeDescriptor sender,
			GroupDescriptor groupDescriptor) {
		for (GroupCommunicationManager manager: leadedGroups) {
			GroupDescriptor localGroup = manager.getGroupDescriptor();
			if (localGroup.matches(groupDescriptor)) {
				// A group matching a leaded group has been found.
				// The manager will attempt to create a hierarchy.
				manager.handleGroupDiscovered(sender, groupDescriptor);
				// Only one group should match
				return;
			}
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
		for (GroupCommunicationManager manager: leadedGroups) {
			GroupDescriptor localGroup = manager.getGroupDescriptor();
			if (localGroup.matches(groupDescriptor)) {
				// A group matching a leaded group has been found.
				// The manager will attempt to create a hierarchy.
				manager.handleMessageGroupCreatedNotification(sender, groupDescriptor);
				// Only one group should match
				return;
			}
		}
	}


	private void handleMessageGroupCoordinationCommandAck(
			NodeDescriptor sender, GroupCoordinationCommandAck packet) {
		// Only a leader can receive an ack
		for (GroupCommunicationManager manager: leadedGroups) {
			GroupDescriptor localGroup = manager.getGroupDescriptor();
			if (localGroup.matches(packet.getGroupDescriptor())) {
				// A group matching a leaded group has been found.
				// The manager will attempt to create a hierarchy.
				manager.handleMessageGroupCoordinationCommandAck(sender, packet);
				// Only one group should match
				return;
			}
		}
	}


	private void handleMessageGroupCoordinationCommand(
			NodeDescriptor sender, GroupCoordinationCommand packet) {
		for (GroupCommunicationManager manager: leadedGroups) {
			GroupDescriptor localGroup = manager.getGroupDescriptor();
			if (localGroup.matches(packet.getGroupDescriptor())) {
				// A group matching a leaded group has been found.
				// The manager will attempt to create a hierarchy.
				manager.handleMessageGroupCoordinationCommand(sender, packet);
				// Only one group should match
				return;
			}
		}
		// also a follower can receive a coordination command
		for (GroupCommunicationManager manager: followedGroups) {
			GroupDescriptor localGroup = manager.getGroupDescriptor();
			if (localGroup.matches(packet.getGroupDescriptor())) {
				// A group matching a leaded group has been found.
				// The manager will attempt to create a hierarchy.
				manager.handleMessageGroupCoordinationCommand(sender, packet);
				// Only one group should match
				return;
			}
		}
	}


	private void handleMessageGroupLeaderCommandAck(NodeDescriptor sender,
			GroupLeaderCommandAck packet) {
		for (GroupCommunicationManager manager: leadedGroups) {
			GroupDescriptor localGroup = manager.getGroupDescriptor();
			if (localGroup.equals(packet.getGroupDescriptor())) {
				// A group matching a leaded group has been found.
				// The manager will attempt to create a hierarchy.
				manager.handleMessageGroupLeaderCommandAck(sender, packet);
				// Only one group should match
				return;
			}
		}
	}


	private void handleMessageGroupLeaderCommand(NodeDescriptor sender,
			GroupLeaderCommand packet) {
		for (GroupCommunicationManager manager: followedGroups) {
			GroupDescriptor localGroup = manager.getGroupDescriptor();
			if (localGroup.equals(packet.getGroupDescriptor())) {
				// A group matching a leaded group has been found.
				// The manager will attempt to create a hierarchy.
				manager.handleMessageGroupLeaderCommand(sender, packet);
				// Only one group should match
				return;
			}
		}
	}


	private void handleMessageGroupFollowerCommandAck(NodeDescriptor sender,
			GroupFollowerCommandAck packet) {
		for (GroupCommunicationManager manager: followedGroups) {
			GroupDescriptor localGroup = manager.getGroupDescriptor();
			if (localGroup.equals(packet.getGroupDescriptor())) {
				// A group matching a leaded group has been found.
				// The manager will attempt to create a hierarchy.
				manager.handleMessageGroupFollowerCommandAck(sender, packet);
				// Only one group should match
				return;
			}
		}
	}


	private void handleMessageGroupFollowerCommand(NodeDescriptor sender,
			GroupFollowerCommand packet) {
		for (GroupCommunicationManager manager: leadedGroups) {
			GroupDescriptor localGroup = manager.getGroupDescriptor();
			if (localGroup.equals(packet.getGroupDescriptor())) {
				// A group matching a leaded group has been found.
				// The manager will attempt to create a hierarchy.
				manager.handleMessageGroupFollowerCommand(sender, packet);
				// Only one group should match
				return;
			}
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
	
	
	public GroupDescriptor leadedGroupMathing(String friendlyName) {
		for (GroupCommunicationManager manager: leadedGroups) {
			GroupDescriptor localGroup = manager.getGroupDescriptor();
			if (localGroup.getFriendlyName().equals(friendlyName)) {
				return localGroup;
			}
		}
		return null;
	}
	
	public GroupDescriptor followedGroupMathing(String friendlyName) {
		for (GroupCommunicationManager manager: followedGroups) {
			GroupDescriptor localGroup = manager.getGroupDescriptor();
			if (localGroup.getFriendlyName().equals(friendlyName)) {
				return localGroup;
			}
		}
		return null;
	}
	
	public GroupDescriptor getGroupMatching(String friendlyName) {
		GroupDescriptor localGroup = null;
		if ((localGroup = leadedGroupMathing(friendlyName)) != null) {
			return localGroup;
		} else {
			return followedGroupMathing(friendlyName);
		}
	}
	
	public GroupDescriptor getLocalUniverse() {
		for (GroupCommunicationManager manager: leadedGroups) {
			GroupDescriptor localGroup = manager.getGroupDescriptor();
			if (localGroup.isUniverse()) {
				return localGroup;
			}
		}
		for (GroupCommunicationManager manager: followedGroups) {
			GroupDescriptor localGroup = manager.getGroupDescriptor();
			if (localGroup.isUniverse()) {
				return localGroup;
			}
		}
		return null;
	}

}
