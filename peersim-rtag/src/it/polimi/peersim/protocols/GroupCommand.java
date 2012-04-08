package it.polimi.peersim.protocols;

import java.io.Serializable;

public class GroupCommand implements Serializable {
	
	private static final long serialVersionUID = 2400309877359047074L;
	
	public static final String JOIN_REQUEST = "JoinRequest";
	public static final String JOIN_REQUEST_RESPONSE = "JoinRequestResponse";

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
