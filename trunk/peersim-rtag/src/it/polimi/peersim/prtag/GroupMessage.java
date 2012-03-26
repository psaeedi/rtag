/**
 * 
 */
package it.polimi.peersim.prtag;

import java.io.Serializable;

/**
 * @author pani
 *
 */
public class GroupMessage {
	
	public static String UPDATE_DESCRIPTOR = "UpdateDescriptor";
	
	public static GroupMessage createUpdateDescriptor(LocalUniverseDescriptor descriptor) {
		return new GroupMessage(UPDATE_DESCRIPTOR, descriptor);
	}

	
	private String head;
	private Serializable body;
	
	private GroupMessage(String head, Serializable body) {
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
