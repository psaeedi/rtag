/**
 * 
 */
package it.polimi.rtag;

import it.polimi.rtag.messaging.GroupFollowerCommand;
import it.polimi.rtag.messaging.GroupFollowerCommandAck;
import it.polimi.rtag.messaging.GroupLeaderCommand;
import it.polimi.rtag.messaging.GroupLeaderCommandAck;
import it.polimi.rtag.messaging.MessageSubjects;

import java.io.Serializable;

import lights.Tuple;

import polimi.reds.NodeDescriptor;
import polimi.reds.broker.overlay.NeighborhoodChangeListener;
import polimi.reds.broker.overlay.Overlay;
import polimi.reds.broker.overlay.PacketListener;

import static it.polimi.rtag.messaging.MessageSubjects.*;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class GroupCommunicationManager implements NeighborhoodChangeListener, PacketListener {

	private GroupDescriptor groupDescriptor;
	private NodeDescriptor currentNodeDescriptor;
	private Overlay overlay;
	
	private static final String[] SUBJECTS = {
		GROUP_CREATED_NOTIFICATION,
		GROUP_LEADER_COMMAND,
		GROUP_LEADER_COMMAND_ACK,
		GROUP_FOLLOWER_COMMAND,
		GROUP_FOLLOWER_COMMAND_ACK
	};
	
	
	public static GroupCommunicationManager createGroup(Node node, 
			 String uniqueId, String friendlyName, Tuple description) {
		
		GroupDescriptor groupDescriptor = new GroupDescriptor(uniqueId, 
				friendlyName, node.getID(), description);
		
		GroupCommunicationManager manager = new GroupCommunicationManager(
				node.getID(), groupDescriptor, node.getOverlay());
		return manager;
	}
	
	public static GroupCommunicationManager createGroup(Node node, 
			GroupDescriptor groupDescriptor) {
		
		GroupCommunicationManager manager = new GroupCommunicationManager(
				node.getID(), groupDescriptor, node.getOverlay());
		return manager;
	}
	
	public static GroupCommunicationManager createUniverse(Node node) {
		
		GroupDescriptor groupDescriptor = GroupDescriptor.createUniverse(node);
		
		GroupCommunicationManager manager = new GroupCommunicationManager(
				node.getID(), groupDescriptor, node.getOverlay());
		return manager;
	}
	
	/**
	 * @param groupDescriptor
	 * @param currentNodeDescriptor
	 * @param overlay
	 */
	public GroupCommunicationManager(
			NodeDescriptor currentNodeDescriptor, 
			GroupDescriptor groupDescriptor,
			Overlay overlay) {
		this.groupDescriptor = groupDescriptor;
		this.currentNodeDescriptor = currentNodeDescriptor;
		this.setOverlay(overlay);
	}
	
	
	@Override
	public void notifyNeighborAdded(NodeDescriptor addedNode, Serializable reconfigurationInfo) {
		// Does nothing. Group members will be added in another way
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
		for (String subject: SUBJECTS) {
			overlay.addPacketListener(this, subject);
		}
		overlay.setTrafficClass(groupDescriptor.getFriendlyName(),
				groupDescriptor.getFriendlyName());
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
		} else if (GROUP_CREATED_NOTIFICATION.equals(subject)) {
			handleMessageGroupCreatedNotification(sender, (GroupCreatedCommand)packet);
		}
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
	private void handleMessageGroupCreatedNotification(NodeDescriptor sender,
			GroupCreatedCommand packet) {
		if (!groupDescriptor.isLeader(currentNodeDescriptor)) {
			return;
		}
		
		// TODO send a GroupLeaderCommand#MERGE_GROUPS if opportune
	}


	private void handleMessageGroupFollowerCommandAck(NodeDescriptor sender,
			GroupFollowerCommandAck packet) {
		// TODO Auto-generated method stub
		
	}


	private void handleMessageGroupFollowerCommand(NodeDescriptor sender,
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
	private void handleMessageGroupLeaderCommandAck(NodeDescriptor sender,
		GroupLeaderCommandAck message) {
		// TODO check if the command was sent by this node
		// TODO handle the response
	}

	/**
	 * Handles {@link MessageSubjects#GROUP_LEADER_COMMAND} messages by performing
	 * the proper action according to what is commanded by the leader.
	 * 
	 * @param sender
	 * @param message
	 */
	private void handleMessageGroupLeaderCommand(NodeDescriptor sender,
		GroupLeaderCommand message) {
		GroupDescriptor group = message.getGroupDescriptor();
		// TODO if the current node is not in the group notify the sender
		if (!group.isLeader(sender)) {
			// The sender is not the group leader
			// TODO do something then return.
			return;
		}
		String command = message.getCommand();
		// TODO handle the command 
	}

	/**
	 * @return the groupDescriptor
	 */
	public GroupDescriptor getGroupDescriptor() {
		return groupDescriptor;
	}


}
