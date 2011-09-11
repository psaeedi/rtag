/**
 * 
 */
package it.polimi.rtag.messaging;

/**
 * @author panteha
 * Dfeine a set of possible message subjects.
 * Message subjects are used to identify which kind of message
 * was sent or received.
 */
public class MessageSubjects {

	public static final String COMMUNICATION = "COMM";
	public static final String ACK = "ACK";
	
	// TODO check if reds is already doing this
	public static final String PING = "PING";
	public static final String PONG = "PONG";
	
	public static final String JOIN_GROUP = "JOIN_GROUP";
	// TODO define all the possible group requests and answers
	
	//Node Migrate from its group to a new group.
	public static final String Migrate_NewGroup = "Migrate_NewGroup";
	
	//Node leaves its group. It is no longer follows its 
	//group description,e.g. its status is changed
	public static final String Leave_Group = "Leave_Group";
	
	//A leader node ask another leader to merge their group.
	//It also proposes as the new group leader
	public static final String Merge_Groups = "Merge_Groups";
	
	
	public static final String Delete_GroupOnly = "Delete_GroupOnly";
	
	public static final String Delete_GroupAndSubgroups = "Delete_GroupAndSubgroups";
	
	// Divide a group to n groups ,the hierarchy should be updated.
	// This is who is the father and child.
	public static final String Divide_Group = "Divide_Group";
	
	
	
	
	
	
	
	
	
	
	/**
	 * 
	 */
	private MessageSubjects() {
		// Contant collection. This should not be instantiated.
	}

}
