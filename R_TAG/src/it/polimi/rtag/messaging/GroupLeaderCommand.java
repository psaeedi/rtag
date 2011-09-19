/**
 * 
 */
package it.polimi.rtag.messaging;

import it.polimi.rtag.GroupDescriptor;
import polimi.reds.Message;
import polimi.reds.broker.overlay.REDSMarshaller;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)</p>.
 * 
 * Defines all the commands that a group leader can send to
 * its followers or to other leaders.
 */
public class GroupLeaderCommand extends Message {
	
	/* Old A3 stuff
	public static final String DELETE_GROUP_ONLY = "DEL_GO";
	public static final String DELETE_GROUP_RECURSIVELY = "DEL_REC";
	public static final String DIVIDE_GROUP = "DIVIDE";
	public static final String LEAVE_GROUP = "LEAVE";
	*/
	
	/**
	 * A leader suggests another leader to merge their groups.</p>
	 * This normally happens as a result of a 
	 * {@link MessageSubjects#GROUP_CREATED_NOTIFICATION} message or
	 * if the leader wants to re-order its group.</p>
	 * 
	 * The recipient will reply with {@link GroupLeaderCommandAck#KO} if
	 * it rejects the merge, if it is not the group leader or
	 * if it has already a parent group.</p>
	 * The recipient will reply with {@link GroupLeaderCommandAck#OK}
	 * if it has accepted the caller as it new group parent.</p>
	 * 
	 * Note that since the two leaders are managing two
	 * different group they will have a different group id.
	 * The merge its possible only if the two groups tuple
	 * description matches.
	 * 
	 * TODO implement this in the Node
	 */
	public static final String MERGE_GROUPS = "MERGE_GROUPS";

	/**
	 * The group leader ask a remote node to join its group.
	 * If the remote node leads a group matching the current one
	 * the remote group should leave its group and join this one. 
	 */
	public static final String JOIN_MY_GROUP = "JOIN_MY_GROUP";
	
	/**
	 * Sent by the group leader to notify its followers that
	 * something has changed in the group descriptor. 
	 */
	public static final String UPDATE_DESCRIPTOR = "UPDATE_DESCRIPTOR";
	
	// TODO add other actions
	
	// TODO create static factory methods
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4659012389818436165L;
	private GroupDescriptor groupDescriptor;
	private String command;
	
	public static GroupLeaderCommand createMergeGroupCommand(GroupDescriptor groupDescriptor) {
		if (groupDescriptor == null) {
			throw new IllegalArgumentException("Group descriptor cannot be null.");
		}
		return new GroupLeaderCommand(groupDescriptor, MERGE_GROUPS);
	}
	
	public static GroupLeaderCommand createJoinMyGroupCommand(GroupDescriptor groupDescriptor) {
		if (groupDescriptor == null) {
			throw new IllegalArgumentException("Group descriptor cannot be null.");
		}
		return new GroupLeaderCommand(groupDescriptor, JOIN_MY_GROUP);
	}
	
	public static GroupLeaderCommand createUpdateCommand(GroupDescriptor groupDescriptor) {
		if (groupDescriptor == null) {
			throw new IllegalArgumentException("Group descriptor cannot be null.");
		}
		return new GroupLeaderCommand(groupDescriptor, UPDATE_DESCRIPTOR);
	}
	
	/**
	 * @param groupDescriptor
	 * @param command
	 */
	private GroupLeaderCommand(GroupDescriptor groupDescriptor, String command) {
		createID();
		this.groupDescriptor = groupDescriptor;
		this.command = command;
	}

	/**
	 * @return the groupDescriptor
	 */
	public GroupDescriptor getGroupDescriptor() {
		return groupDescriptor;
	}

	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}
}
