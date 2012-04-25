/**
 * 
 */
package it.polimi.peersim.protocols.grouping;

import it.polimi.peersim.messages.BaseMessage;

import java.io.Serializable;

import peersim.core.Node;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 */
public class GroupingMessage extends BaseMessage {
	
	private static final long serialVersionUID = -1554400393005842078L;
	
	public static final String WRAP = "Wrap";
	public static final String GROUP_COMMAND = "groupCommand";
	
	public static GroupingMessage createGroupCommand(
			int pid, Node sender, GroupCommand command) {
		return new GroupingMessage(pid, sender, GROUP_COMMAND, command);
	}
		
	public static GroupingMessage wrapMessage(
			int pid, Node sender, BaseMessage message) {
		return new GroupingMessage(pid, sender, WRAP, message);
	}
	
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
