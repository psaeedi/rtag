package it.polimi.peersim.protocols.grouping;

import java.io.Serializable;

import peersim.core.Node;

public class GroupCommand implements Serializable {
	
	private static final long serialVersionUID = 2400309877359047074L;
	
	public static final String ADD_OR_UPDATE_BEACON = "AddOrUpdateBeacon";
	public static final String PREMATURALY_DISCARD_BEACON = "PrematuralyDiscardBeacon";
	
	public static final String JOIN_REQUEST = "JoinRequest";
	public static final String JOIN_RESPONSE_YES = "JoinResponse";
	public static final String JOIN_RESPONSE_NO = "JoinResponse";
    public static final String JOIN_REQUEST_RESPONSE = "join_request_response";
    
    public static final String UPDATE_DESCRIPTOR = "updateDescriptor";
    public static final String DELETE_DESCRIPTOR = "deleteDescriptor";
    
    public static final String ADOPT_FOLLOWER = "adoptfollower";

	public static final String ADOPT_REQUEST = "adoptrequest";
	
	public static final String CHANGE_LEADER_RQUEST = "changeleaderrequest";
	
	public static final String FOLLOWER_LEFT = "followerleft";

	
	public static GroupCommand createAddOrUpdateBeacon(GroupBeacon beacon) {
		return new GroupCommand(ADD_OR_UPDATE_BEACON, beacon);
	}
	
	public static GroupCommand createPrematurelyBeacon(GroupBeacon beacon) {
		return new GroupCommand(PREMATURALY_DISCARD_BEACON, beacon);
	}
	
	public static GroupCommand createJoinRequest(String groupName) {
		return new GroupCommand(JOIN_REQUEST, groupName);
	}
	
	public static GroupCommand createJoinRequestResponse (String groupName) {
		return new GroupCommand(JOIN_REQUEST_RESPONSE, groupName);
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
	
	public static GroupCommand createAdoptRequest(GroupDescriptor descriptor) {
		return new GroupCommand(ADOPT_REQUEST, descriptor);
	}
	
	public static GroupCommand createYouAreAdopted(GroupDescriptor descriptor) {
		return new GroupCommand(CHANGE_LEADER_RQUEST, descriptor);
	}
	
	public static GroupCommand createLeftLeader(String groupName) {
		return new GroupCommand(FOLLOWER_LEFT, groupName);
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
