/**
 * 
 */
package it.polimi.rtag.messaging;

import it.polimi.rtag.GroupDescriptor;

import java.io.Serializable;

import polimi.reds.NodeDescriptor;

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
	
	public static final String CREATE_CHILD_GROUP = "CREATE_CHILD_GROUP";
	
	public static final String DELETE_GROUP = "DELETE_GROUP";
		
	public static final String DELETE_HIERARCHY = "DELETE_HIERARCHY";
	
	public static final String UPDATE_DESCRIPTOR = "UPDATE_DESCRIPTOR";
		
	public static final String MIGRATE_TO_GROUP = "MIGRATE_TO_GROUP";
	
	public static final String SUBJECT = "TupleGroupCommand";
	

	
	public static TupleGroupCommand createDeleteGroupCommand(GroupDescriptor groupDescriptor) {
		return new TupleGroupCommand(Scope.GROUP, groupDescriptor, groupDescriptor, DELETE_GROUP);
	}

	public static TupleGroupCommand createUpdateGroupCommand(
			GroupDescriptor groupDescriptor) {
		return new TupleGroupCommand(Scope.GROUP, groupDescriptor, groupDescriptor, UPDATE_DESCRIPTOR);
	}

	public static TupleGroupCommand createMigrateToGroupCommand(
			GroupDescriptor remoteGroup, GroupDescriptor groupDescriptor) {
		return new TupleGroupCommand(Scope.GROUP, groupDescriptor, remoteGroup, MIGRATE_TO_GROUP);
	}

	
	public TupleGroupCommand(Scope scope, Serializable recipient,
			Serializable content, String command) {
		super(scope, recipient, content, command);
	}

	public String getSubject() {
		return SUBJECT;
	}

}
