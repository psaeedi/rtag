/**
 * 
 */
package it.polimi.rtag.messaging;

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
	public static final String COMMUNICATION = "COMM";
	
	/**
	 * A node which has received a message with subject {@link MessageSubjects#COMMUNICATION}
	 * has to respond with an ack to the sender.
	 * 
	 * This subject is sent with a message of type {@link Ack}.
	 */
	public static final String ACK = "ACK";

	/**
	 * A node ask the leader to join a group of which it is the leader.
	 * 
	 * This subject is sent with a message of type {@link JoinGroupRequest}. 
	 */
	public static final String JOIN_GROUP_REQUEST = "JOIN_GROUP_REQUEST";
	
	/**
	 * A node respond to a join group request by sending a join group response.
	 * 
	 * This subject is sent with a message of type {@link JoinGroupResponse}. 
	 */
	public static final String JOIN_GROUP_RESPONSE = "JOIN_GROUP_RESPONSE";
	
	/**
	 * A group leader send this message to control its followers. 
	 * 
	 * This subject is sent with a message of type {@link GroupLeaderCommand}.
	 */
	public static final String GROUP_LEADER_COMMAND = "GROUP_LEADER_COMMAND";
	
	
	/**
	 * A group leader receives this message to update its followers list. 
	 * 
	 * This subject is sent with a message of type {@link GroupLeaderCommandAck}.
	 */
	
	public static final String GROUP_LEADER_COMMAND_ACK = "GROUP_LEADER_COMMAND_ACK";
	
	
	// TODO A leader propose another leader to merge their groups
	//public static final String MERGE_GROUPS = "MERGE_GROUPS";

	// TODO JOIN MY GROUP a leader can invite/suggest a node to join a certain group?
	
	private MessageSubjects() {
		// Contant collection. This should not be instantiated.
	}

}
