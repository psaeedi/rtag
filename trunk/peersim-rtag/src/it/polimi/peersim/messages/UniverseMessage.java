/**
 * 
 */
package it.polimi.peersim.messages;


import it.polimi.peersim.protocols.UniverseCommand;

import java.io.Serializable;

import peersim.core.Node;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 */
public class UniverseMessage extends BaseMessage {
	
	private static final long serialVersionUID = -6818627867498834594L;

	public static String UNIVERSE_COMMAND = "UniverseCommand";
	public static String BROADCAST = "Broadcast";
	public static String SINGLECAST = "Singlecast";
	
	private Node sender;
	
	
	public static UniverseMessage createBroadcast(
			int pid, Node sender, BaseMessage body) {
		return new UniverseMessage(pid, sender, BROADCAST, body);
	}
	
	public static UniverseMessage createSinglecast(
			int pid, Node sender, BaseMessage message) {
		return new UniverseMessage(pid, sender, SINGLECAST, message);
	}
		
	public static UniverseMessage createUniverseCommand(
			int pid, Node sender, UniverseCommand command) {
		return new UniverseMessage(pid, sender, UNIVERSE_COMMAND, command);
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
