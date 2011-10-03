/**
 * 
 */
package it.polimi.rtag.messaging;

import it.polimi.rtag.GroupDescriptor;
import polimi.reds.Message;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)</p>.
 *
 */
public class GroupFollowerCommand extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4481850960336362688L;
	
	/**
	 * A node notify its leader that it will leave a certain group. 
	 * TODO add a {@link GroupDescriptor} field to this class.
	 */
	public static final String LEAVING_NOTICE = "LEAVING_NOTICE";
	
	private GroupDescriptor groupDescriptor;
	private String command;
	
	public static GroupFollowerCommand createLeavingNoticeCommand(GroupDescriptor groupDescriptor) {
		if (groupDescriptor == null) {
			throw new IllegalArgumentException("Group descriptor cannot be null.");
		}
		return new GroupFollowerCommand(groupDescriptor, LEAVING_NOTICE);
	}
	
	/**
	 * @param groupDescriptor
	 * @param command
	 */
	private GroupFollowerCommand(GroupDescriptor groupDescriptor, String command) {
		createID();
		this.groupDescriptor = groupDescriptor;
		this.command = command;
	}
	
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
