/**
 * 
 */
package it.polimi.rtag;

import polimi.reds.NodeDescriptor;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 * 
 * Gives to a {@link GroupCommunicationManager} indication 
 * in how to coordinate a group.
 */
public interface GroupCoordinationStrategy {

	//public boolean shouldInviteToJoin(GroupDescriptor remoteGroup);
	
	public boolean shouldInviteToJoin(GroupDescriptor remoteGroup);
	
	//public boolean shouldAcceptToJoin(GroupDescriptor remoteGroup);
	
	public boolean shouldAcceptToJoin(GroupDescriptor remoteGroup);
	
	public NodeDescriptor shouldSplitToNode();
	
	public boolean shouldAcceptToCreateAChild();
	
	public boolean shouldSuggestToMigrate(GroupDescriptor remoteGroup,
			NodeDescriptor remoteFollower);
	
	public boolean shouldAcceptToMigrate(GroupDescriptor remoteGroup);
	
	public NodeDescriptor electNewLeader();

}
