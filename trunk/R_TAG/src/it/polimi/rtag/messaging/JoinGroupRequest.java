/**
 * 
 */
package it.polimi.rtag.messaging;

import it.polimi.rtag.GroupDescriptor;
import polimi.reds.Message;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 * Encapsulates in a message the group descriptor which the sender wants to join.
 * The joiner should send an instance of this message to the target group leader.
 * The group leader will respond with a {@link JoinGroupResponse} message.
 */
public class JoinGroupRequest extends Message {

	private GroupDescriptor groupDescriptor;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 466668406448425287L;

	/**
	 * @param groupDescriptor
	 */
	public JoinGroupRequest(GroupDescriptor groupDescriptor) {
		this.groupDescriptor = groupDescriptor;
	}

	/**
	 * @return the groupDescriptor
	 */
	public GroupDescriptor getGroupDescriptor() {
		return groupDescriptor;
	}

}
