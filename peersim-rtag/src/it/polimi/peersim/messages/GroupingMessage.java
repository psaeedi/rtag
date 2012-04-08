/**
 * 
 */
package it.polimi.peersim.messages;

import it.polimi.peersim.protocols.GroupCommand;
import it.polimi.peersim.prtag.GroupDescriptor;

import java.io.Serializable;

import peersim.core.Node;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 */
public class GroupingMessage extends BaseMessage {
	
	private static final long serialVersionUID = -1554400393005842078L;
	
	public static final String UPDATE_DESCRIPTOR = "UpdateDescriptor";
	public static final String DELETE_DESCRIPTOR = "DeleteDescriptor";
	public static final String WRAP = "Wrap";
	//public static String REPLACELEADER_REQUEST = "ReplaceLeaderRequest";
	public static final String GROUP_COMMAND = "groupCommand";
	
	public static GroupingMessage createUpdateDescriptor(
			int pid, Node sender, GroupDescriptor descriptor) {
		return new GroupingMessage(pid, sender, UPDATE_DESCRIPTOR, descriptor);
	}
	
	public static GroupingMessage createDeleteDescriptor(
			int pid, Node sender, GroupDescriptor descriptor) {
		return new GroupingMessage(pid, sender, DELETE_DESCRIPTOR, descriptor);
	}

	public static GroupingMessage createGroupCommand(
			int pid, Node sender, GroupCommand command) {
		return new GroupingMessage(pid, sender, GROUP_COMMAND, command);
	}
		
	public static GroupingMessage wrapMessage(
			int pid, Node sender, BaseMessage message) {
		return new GroupingMessage(pid, sender, WRAP, message);
	}
	
	/*
	public static GroupingMessage createLeaderReplacement(int pid,
			Node sender, String groupName) {
		return new GroupingMessage(pid, sender, REPLACELEADER_REQUEST, groupName);
	}*/
	
	private Node sender;
	private String head;
	
	private GroupingMessage(int pid, Node sender, String head, Serializable content) {
		super(pid, content);
		this.sender = sender;
		this.head = head;
	}
	
	public String getHead() {
		return head;
	}

	public Node getSender() {
		return sender;
	}

}
