/**
 * 
 */
package it.polimi.peersim.prtag;


import java.io.Serializable;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 */
public class UniverseMessage {
	
	public static String UPDATE_DESCRIPTOR = "UpdateDescriptor";
	public static String BROADCAST = "Broadcast";
	public static String ADDFOLOWER = "Addfollower";
	public static String ADDFOLOWER_ACK = "Addfollower_ack";
	
	public static UniverseMessage createUpdateDescriptor(LocalUniverseDescriptor descriptor) {
		return new UniverseMessage(UPDATE_DESCRIPTOR, descriptor);
	}
	
	public static UniverseMessage createBroadcast(BroadcastContent body) {
		return new UniverseMessage(BROADCAST, body);
	}
	
	public static UniverseMessage createAddfollower(LocalUniverseDescriptor descriptor) {
		return new UniverseMessage(ADDFOLOWER, descriptor);
	}
	
	public static UniverseMessage createAddfollowerAck() {
		return new UniverseMessage(ADDFOLOWER_ACK, null);
	}
	
	private String head;
	private Serializable body;
	
	private UniverseMessage(String head, Serializable body) {
		super();
		this.head = head;
		this.body = body;
	}
	
	public String getHead() {
		return head;
	}
	public Serializable getBody() {
		return body;
	}

	
}
