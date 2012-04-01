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
public class UniverseMessage {
	
	public static String UPDATE_DESCRIPTOR = "UpdateDescriptor";
	public static String BROADCAST = "Broadcast";
	public static String ADDFOLOWER = "Addfollower";
	public static String ADDFOLOWER_ACK = "Addfollower_ack";
	
	private Node sender;
	
	public static UniverseMessage createUpdateDescriptor(Node sender, LocalUniverseDescriptor descriptor) {
		return new UniverseMessage(sender, UPDATE_DESCRIPTOR, descriptor);
	}
	
	public static UniverseMessage createBroadcast(Node sender, BroadcastContent body) {
		return new UniverseMessage(sender, BROADCAST, body);
	}
	
	public static UniverseMessage createAddfollower(Node sender, LocalUniverseDescriptor descriptor) {
		return new UniverseMessage(sender, ADDFOLOWER, descriptor);
	}
	
	public static UniverseMessage createAddfollowerAck(Node sender) {
		return new UniverseMessage(sender, ADDFOLOWER_ACK, null);
	}
	
	private String head;
	private Serializable body;
	
	private UniverseMessage(Node sender, String head, Serializable body) {
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
