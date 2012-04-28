package it.polimi.peersim.protocols.grouping;

import java.io.Serializable;

import peersim.core.Node;

public class GroupCommand implements Serializable {
	
	private static final long serialVersionUID = 2400309877359047074L;
	
	public static final String ADD_OR_UPDATE_BEACON = "AddOrUpdateBeacon";
	public static final String PREMATURALY_DISCARD_BEACON = "PrematuralyDiscardBeacon";
	
	public static final String JOIN_REQUEST = "JoinRequest";
	public static final String JOIN_RESPONSE_YES = "JoinResponseYes";
	public static final String JOIN_RESPONSE_NO = "JoinResponseNo";
    
    public static final String UPDATE_DESCRIPTOR = "updateDescriptor";
    public static final String DELETE_DESCRIPTOR = "deleteDescriptor";
    
    public static final String NOTIFY_LEAVE = "notifyLeave";

	
	public static GroupCommand createAddOrUpdateBeacon(GroupBeacon beacon) {
		return new GroupCommand(ADD_OR_UPDATE_BEACON, beacon);
	}
	
	public static GroupCommand createPrematurelyBeacon(GroupBeacon beacon) {
		return new GroupCommand(PREMATURALY_DISCARD_BEACON, beacon);
	}
	
	public static GroupCommand createJoinRequest(String groupName) {
		return new GroupCommand(JOIN_REQUEST, groupName);
	}
	
	public static GroupCommand createJoinResponseYes(GroupDescriptor descriptor) {
		return new GroupCommand(JOIN_RESPONSE_YES, descriptor);
	}
	
	public static GroupCommand createJoinResponseNo(String groupName) {
		return new GroupCommand(JOIN_RESPONSE_NO, groupName);
	}
	
	public static GroupCommand createUpdateDescriptor(GroupDescriptor descriptor) {
		return new GroupCommand(UPDATE_DESCRIPTOR, descriptor);
	}
	
	public static GroupCommand createDeleteDescriptor(GroupDescriptor descriptor) {
		return new GroupCommand(DELETE_DESCRIPTOR, descriptor);
	}
	
	public static GroupCommand createLeaveNotify(String groupName) {
		return new GroupCommand(NOTIFY_LEAVE, groupName);
	}
	
	private final String command;
	private final Serializable content;
	
	public GroupCommand(String command, Serializable content) {
		super();
		this.command = command;
		this.content = content;
	}

	public String getCommand() {
		return command;
	}

	public Serializable getContent() {
		return content;
	}
	
}
