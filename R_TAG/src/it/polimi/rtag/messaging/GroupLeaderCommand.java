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
	 * A group leader send a leave notification every time a node
	 * has left or will leave the group. The recipients use this
	 * message to update their node descriptor by removing the node
	 * leaving. Leaders are notified of a leaving node by receiving a
	 * {@link GroupFollowerCommand#LEAVING_NOTICE} message and
	 * will receive events from REDS for collapsed nodes.</p>
	 * 
	 * If the leader is the one leaving then the followers will have either
	 * to join the parent group if it exists or to select a new leader.
	 * The same procedure will be performed when a group leader collapses.</p>
	 * 
	 * TODO implement this in the node
	 * TODO define an election/bet interface
	 */
	public static final String LEAVE_NOTIFICATION = "LEAVE_NOTIFICATION";
	
	// TODO add other actions
	
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
