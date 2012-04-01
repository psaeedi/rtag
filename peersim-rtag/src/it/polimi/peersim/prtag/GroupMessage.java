/**
 * 
 */
package it.polimi.peersim.prtag;

import java.io.Serializable;

import peersim.core.Node;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 */
public class GroupMessage implements Serializable {
	
	public static String UPDATE_DESCRIPTOR = "UpdateDescriptor";
	public static String DELETE_DESCRIPTOR = "DeleteDescriptor";
	public static String JOIN_REQUEST = "JoinRequest";
	public static String JOIN_REQUEST_ACK = "JoinRequestAck";
	
	public static GroupMessage createUpdateDescriptor(Node sender, GroupDescriptor descriptor) {
		return new GroupMessage(sender, UPDATE_DESCRIPTOR, descriptor);
	}
	
	public static GroupMessage createDeleteDescriptor(Node sender, GroupDescriptor descriptor) {
		return new GroupMessage(sender, DELETE_DESCRIPTOR, descriptor);
	}

	public static GroupMessage createJoinRequest(Node sender, String groupName) {
		return new GroupMessage(sender, JOIN_REQUEST, groupName);
	}

	public static GroupMessage createJoinRequestAck(
			Node sender, GroupDescriptor descriptor) {
		return new GroupMessage(sender, JOIN_REQUEST_ACK, descriptor);
	}

	private Node sender;
	private String head;
	private Serializable body;
	
	private GroupMessage(Node sender, String head, Serializable body) {
		super();
		this.sender = sender;
		this.head = head;
		this.body = body;
	}
	
	public String getHead() {
		return head;
	}
	
	public Serializable getBody() {
		return body;
	}

	public Node getSender() {
		return sender;
	}

}
