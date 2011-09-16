/**
 * 
 */
package it.polimi.rtag.messaging;

import polimi.reds.broker.routing.Router;
import it.polimi.rtag.GroupDescriptor;

/**
 * @author panteha
 * Dfeine a set of possible message subjects.
 * Message subjects are used to identify which kind of message
 * was sent or received.
 */
public class MessageSubjects {

	/**
	 * A node has sent another node a communication message.
	 * The receiver should check if the recipient tuple identify a
	 * single cast, an anycast a multicast or a broadcast then it should
	 * forward the message accordingly.
	 * 
	 * This subject is sent with a message of type {@link TupleMessage}.
	 */
	public static final String PUBLISH = Router.PUBLISH;
	
	/**
	 * A node which has received a message with subject {@link MessageSubjects#PUBLISH}
	 * has to respond with an ack to the sender.
	 * 
	 * This subject is sent with a message of type {@link Ack}.
	 */
	public static final String REPLY = Router.REPLY;

	
	/**
	 * A node notify its local universe leader that it has
	 * created a new group. </p>
	 * The local universe leader will forward this notification to all its
	 * followers and to its parent. If a group leader of a group with the same
	 * tuple definition is reached then that group leader may invite the new leader
	 * to join its group sending a {@link GroupLeaderCommand#MERGE_GROUPS}
	 * message.</p>
	 * 
	 * Beside further group re-organization this message has no direct
	 * acknowledgment.
	 * 
	 * TODO create a class to encapsulate this message with the creator descriptor
	 * TODO add methods to handle this class in the Node
	 */
	public static final String GROUP_CREATED_NOTIFICATION = "G_CREATED_N";
	
	
	/**
	 * A group leader send this message to control its followers. 
	 * 
	 * This subject is sent with a message of type {@link GroupLeaderCommand}.
	 */
	public static final String GROUP_LEADER_COMMAND = "GROUP_LEADER_COMMAND";
	
	/**
	 * A group leader receives this message as a response to an issued command.
	 * 
	 * This subject is sent with a message of type {@link GroupLeaderCommandAck}.
	 */
	public static final String GROUP_LEADER_COMMAND_ACK = "GROUP_LEADER_COMMAND_ACK";
	
	/**
	 * A group follower uses this message either to notify its leader or to
	 * communicate with its peer. 
	 */
	public static final String GROUP_FOLLOWER_COMMAND = "GROUP_FOLLOWER_COMMAND";
	
	/**
	 * A group leader or a group follower which has received a follower command
	 * has to reply with its response.
	 */
	public static final String GROUP_FOLLOWER_COMMAND_ACK = "GROUP_FOLLOWER_COMMAND_ACK";
	
	private MessageSubjects() {
		// Contant collection. This should not be instantiated.
	}

}
