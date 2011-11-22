/**
 * 
 */
package it.polimi.rtag.messaging;

import it.polimi.rtag.GroupDescriptor;

import java.io.Serializable;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class TupleGroupCommand extends TupleMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5203617233752524013L;
	
	public static final String LEAVING_NOTICE = "LEAVING_NOTICE";
	
	public static final String MERGE_GROUPS = "MERGE_GROUPS";
	
	public static final String CREATE_CHILD_GROUP = "CREATE_CHILD_GROUP";
	
	public static final String DELETE_GROUP = "DELETE_GROUP";
		
	public static final String DELETE_HIERARCHY = "DELETE_HIERARCHY";
	
	public static final String UPDATE_DESCRIPTOR = "UPDATE_DESCRIPTOR";
	
	public static final String JOIN_MY_GROUP = "JOIN_MY_GROUP";	
	
	public static final String MIGRATE_TO_GROUP = "MIGRATE_TO_GROUP";
	
	public static final String ADOPT_GROUP = "ADOPT_GROUP";
	
	public static final String SUBJECT = "TupleGroupCommand";
	
	private GroupDescriptor groupDescriptor;
	private String command;
	
	/**
	 * @param scope
	 * @param recipient
	 */
	public TupleGroupCommand(Scope scope, Serializable recipient) {
		super(scope, recipient);
		// TODO Auto-generated constructor stub
	}

	public String getSubject() {
		return SUBJECT;
	}
}
