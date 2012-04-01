/**
 * 
 */
package it.polimi.peersim.messages;


import it.polimi.peersim.prtag.LocalUniverseDescriptor;

import java.io.Serializable;

import peersim.core.Node;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 */
public class UniverseMessage extends BaseMessage {
	
	private static final long serialVersionUID = -6818627867498834594L;
	
	public static String UPDATE_DESCRIPTOR = "UpdateDescriptor";
	public static String BROADCAST = "Broadcast";
	public static String ADDFOLOWER = "Addfollower";
	public static String ADDFOLOWER_ACK = "Addfollower_ack";
	public static String WRAP = "Wrap";
	
	private Node sender;
	
	public static UniverseMessage createUpdateDescriptor(
			int pid, Node sender, LocalUniverseDescriptor descriptor) {
		return new UniverseMessage(pid, sender, UPDATE_DESCRIPTOR, descriptor);
	}
	
	public static UniverseMessage createBroadcast(
			int pid, Node sender, BaseMessage body) {
		return new UniverseMessage(pid, sender, BROADCAST, body);
	}
	
	public static UniverseMessage wrapMessage(
			int pid, Node sender, BaseMessage message) {
		return new UniverseMessage(pid, sender, WRAP, message);
	}
	
	public static UniverseMessage createAddfollower(
			int pid, Node sender, LocalUniverseDescriptor descriptor) {
		return new UniverseMessage(pid, sender, ADDFOLOWER, descriptor);
	}
	
	public static UniverseMessage createAddfollowerAck(int pid, Node sender) {
		return new UniverseMessage(pid, sender, ADDFOLOWER_ACK, null);
	}
	
	private String head;
	
	private UniverseMessage(int pid, Node sender,
			String head, Serializable content) {
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
