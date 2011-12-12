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
	
	public boolean shouldRequestToJoin(GroupDescriptor remoteGroup);
	
	//public boolean shouldAcceptToJoin(GroupDescriptor remoteGroup);
	
	public boolean shouldAcceptJoinRequest(NodeDescriptor remoteNode);
	
	public boolean shouldSplitTo(GroupDescriptor remoteGroup);
	
	public boolean shouldAcceptToCreateAChild();
	
	public NodeDescriptor[] followerToSplit(GroupDescriptor remoteGroup);
	
	public boolean shouldAcceptToMigrate(GroupDescriptor remoteGroup);
	
	public NodeDescriptor electNewLeader();

}
