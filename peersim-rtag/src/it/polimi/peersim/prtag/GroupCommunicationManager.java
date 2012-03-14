package it.polimi.peersim.prtag;

import peersim.core.Node;

public class GroupCommunicationManager {
	/**
	 * The {@link GroupCommunicationManager} managing a
	 * child group if any.
	 */
	private GroupCommunicationManager leadedChildManager;
	/**
	 * The {@link GroupCommunicationManager} managing a
	 * parent group if any.
	 */
	private GroupCommunicationManager followedParentManager;
	private GroupDescriptor groupDescriptor;

	
	public static GroupCommunicationManager createGroupCommunicationManager(Node node, 
			GroupDescriptor groupDescriptor) {
		//TODO : implement
		return null;
	}
	
	
	public void notifyNeighborAdded(Node addedNode) {
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
			// TODO Notify the other node of the existence of this group
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public GroupDescriptor getGroupDescriptor() {
		return groupDescriptor;
	}




}
