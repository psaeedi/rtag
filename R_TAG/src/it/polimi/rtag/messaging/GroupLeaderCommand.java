/**
 * 
 */
package it.polimi.rtag.messaging;

import it.polimi.rtag.GroupDescriptor;
import polimi.reds.Message;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class GroupLeaderCommand extends Message {
	
	public static final String DELETE_GROUP_ONLY = "DEL_GO";
	public static final String DELETE_GROUP_RECURSIVELY = "DEL_REC";
	public static final String DIVIDE_GROUP = "DIVIDE";
	public static final String LEAVE_GROUP = "LEAVE";
	
	// TODO create static factory methods
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4659012389818436165L;
	private GroupDescriptor groupDescriptor;
	private String command;
	
	/**
	 * @param groupDescriptor
	 * @param command
	 */
	private GroupLeaderCommand(GroupDescriptor groupDescriptor, String command) {
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
