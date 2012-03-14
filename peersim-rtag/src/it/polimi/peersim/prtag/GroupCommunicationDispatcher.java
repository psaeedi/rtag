package it.polimi.peersim.prtag;

import java.util.ArrayList;

public class GroupCommunicationDispatcher {
	
	private Object lock = new Object();
	
	private ArrayList<GroupCommunicationManager> leadedGroups = new ArrayList<GroupCommunicationManager>();
	private ArrayList<GroupCommunicationManager> followedGroups = new ArrayList<GroupCommunicationManager>();

	public GroupDescriptor joinGroupAndNotifyNetwork(String friendlyName) {
		GroupDescriptor descriptor = getGroupForHierarchy(friendlyName);
		if (descriptor != null) {
			// Already in that group
			return descriptor;
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
	

	public void leaveGroupsWithName(String friendlyName) {
		// TODO Auto-generated method stub
		
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

}
