/**
 * 
 */
package it.polimi.rtag;

import java.beans.PropertyChangeEvent;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import com.google.common.collect.HashMultimap;

import polimi.reds.MessageID;
import polimi.reds.NodeDescriptor;
import polimi.reds.broker.overlay.AlreadyNeighborException;
import polimi.reds.broker.overlay.NotRunningException;
import polimi.reds.broker.overlay.SimpleTopologyManager;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class GroupAwareTopologyManager extends SimpleTopologyManager 
		implements GroupChangeListener {

	private HashMultimap<NodeDescriptor, UUID> groupsByNode = HashMultimap.create();
	
	/**
	 * 
	 */
	public GroupAwareTopologyManager() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * NOTE: this event will be only received if 
	 * oldGroupDescript.equals(NewGroupDescript) == false
	 * therefore the GroupDescriptequals method implies
	 * that {@link GroupDescriptor#equals(Object)} will return
	 * <code>true</code> if the descriptor has been updated.
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		GroupDescriptor oldGroupDescriptor = (GroupDescriptor) evt.getOldValue();
		GroupDescriptor newGroupDescriptor = (GroupDescriptor) evt.getNewValue();
		// TODO do something
		if (oldGroupDescriptor == null) {
			// A new group has been created/added
			addGroupDescriptor(newGroupDescriptor);
			return;
		} else if (newGroupDescriptor == null) {
			// A group has been removed/dismantled
			removeGroupDescriptor(oldGroupDescriptor);
		} else {
			// A group has been updated
			updateGroupDescriptor(oldGroupDescriptor, newGroupDescriptor);
		}	
	}
	
	private void addNodeForGroup(NodeDescriptor nodeDescriptor, GroupDescriptor groupDescriptor) {
		if (nodeDescriptor == null || groupDescriptor == null) {
			// fail silently
			return;
		}
		synchronized (groupsByNode) {
			groupsByNode.put(nodeDescriptor, groupDescriptor.getUniqueId());
		}
		
		if (!isNeighborOf(nodeDescriptor)) {
			NodeDescriptor desc = null;
			for (String url: nodeDescriptor.getUrls()) {
				try {
					desc = addNeighbor(url);
					if (desc != null) {
						break;
					}
				} catch (AlreadyNeighborException e) {
					break;
				} catch (Exception e) {
					// Try the next one
					e.printStackTrace();
				}
			} 
		}
	}
	
	private void addGroupDescriptor(GroupDescriptor groupDescriptor) {
		addNodeForGroup(groupDescriptor.getLeader(), groupDescriptor);
		addNodeForGroup(groupDescriptor.getParentLeader(), groupDescriptor);
		for (NodeDescriptor nodeDescriptor: groupDescriptor.getFollowers()) {
			addNodeForGroup(nodeDescriptor, groupDescriptor);
		}
	}
	
	private void removeNodeFromGroup(NodeDescriptor nodeDescriptor, GroupDescriptor groupDescriptor) {
		synchronized (groupsByNode) {
			groupsByNode.remove(nodeDescriptor, groupDescriptor);
			if (groupsByNode.containsKey(nodeDescriptor)) {
				removeNeighbor(nodeDescriptor);
			}
		}
	}

	private void removeGroupDescriptor(GroupDescriptor groupDescriptor) {
		removeNodeFromGroup(groupDescriptor.getLeader(), groupDescriptor);
		removeNodeFromGroup(groupDescriptor.getParentLeader(), groupDescriptor);
		for (NodeDescriptor nodeDescriptor: groupDescriptor.getFollowers()) {
			removeNodeFromGroup(nodeDescriptor, groupDescriptor);
		}
	}
	
	private void updateGroupDescriptor(GroupDescriptor oldGroupDescriptor,
			GroupDescriptor newGroupDescriptor) {
		
		ArrayList<NodeDescriptor> oldMembers = oldGroupDescriptor.getMembers();
		oldMembers.add(oldGroupDescriptor.getParentLeader());
		
		ArrayList<NodeDescriptor> newMembers = newGroupDescriptor.getMembers();
		newMembers.add(newGroupDescriptor.getParentLeader());
		
		for (int i = oldMembers.size() - 1; i >= 0; i--) {
			NodeDescriptor desc = oldMembers.get(i);
			if (newMembers.contains(desc)) {
				newMembers.remove(desc);
				oldMembers.remove(i);
			}
		}
		
		for (int i = newMembers.size() - 1; i >= 0; i--) {
			NodeDescriptor desc = newMembers.get(i);
			if (oldMembers.contains(desc)) {
				oldMembers.remove(desc);
				newMembers.remove(i);
			}
		}
		
		// Delete all the group memebers in oldMembers which are not in newMembers
		for (NodeDescriptor node: oldMembers) {
			removeNodeFromGroup(node, newGroupDescriptor);
		}
		
		// Add all the group memebers in newMembers which are not in oldMembers
		for (NodeDescriptor node: newMembers) {
			addNodeForGroup(node, newGroupDescriptor);
		}
	}
}
