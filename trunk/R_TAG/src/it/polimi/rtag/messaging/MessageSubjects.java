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
	
	
	//Node Migrate from its group to a new group.
	public static final String MIGRATE_TO_NEW_GROUP = "MIGRATE_TO_NEW_GROUP";
	
	//Node leaves its group. It no longer follows its 
	//group description,e.g. its status is changed
	public static final String LEAVE_GROUP = "LEAVE_GROUP";
	
	//A leader node ask another leader to merge their group.
	//It also proposes as the new group leader
	public static final String MERGE_GROUPS = "MERGE_GROUPS";
	
	/** The leader of a group notify its followers a group managed by him should
	 *  be deleted but not its subgroups. 
	 */
	public static final String DELETE_GROUP_ONLY = "DELETE_GROUP_ONLY";
	
	/**
	 * A leader notify its followers that a group managed by him has to be deleted
	 * and that all the leaders of its subgroups have to delete their groups as well.
	 */
	public static final String DELETE_GROUP_AND_SUBGROUP = "DELETE_GROUP_AND_SUBGROUP";
	
	// Divide a group to n groups ,the hierarchy should be updated.
	// This is who is the father and child.
	public static final String DIVIDE_GROUP = "DIVIDE_GROUP";
	
	// TODO JOIN MY GROUP a leader can invite/suggest a node to join a certain group?
	
	
	
	
	
	
	
	
	/**
	 * 
	 */
	private MessageSubjects() {
		// Contant collection. This should not be instantiated.
	}

}
