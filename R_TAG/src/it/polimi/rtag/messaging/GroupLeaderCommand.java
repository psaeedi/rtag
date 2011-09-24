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
	 * Sent by the group leader to notify its followers that
	 * something has changed in the group descriptor.</p>
	 * 
	 * TODO Followers should NOT send an ACK to this message for
	 * performance reasons.
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
