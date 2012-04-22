/**
 * 
 */
package it.polimi.peersim.protocols;

import it.polimi.peersim.prtag.LocalUniverseDescriptor;

import java.io.Serializable;

/**
 * @author Panteha Saeedi
 *
 */
public class UniverseCommand implements Serializable {

	private static final long serialVersionUID = -8384815517803260341L;

	public static String UPDATE_DESCRIPTOR = "UpdateDescriptor";
	public static String ADDFOLOWER = "Addfollower";
	public static String ADDFOLOWER_ACK = "Addfollower_ack";

	private final String command;
	private final Serializable content;

	
	public static UniverseCommand createUpdateDescriptor(
			LocalUniverseDescriptor descriptor) {	
		return new UniverseCommand(UPDATE_DESCRIPTOR, descriptor);
	}
	
	public static UniverseCommand createAddfollower(
			LocalUniverseDescriptor descriptor) {
		return new UniverseCommand(ADDFOLOWER, descriptor);
	}
	
	public static UniverseCommand createAddfollowerAck() {
		return new UniverseCommand(ADDFOLOWER_ACK, null);
	}

	
	private UniverseCommand(String command, Serializable content) {
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
