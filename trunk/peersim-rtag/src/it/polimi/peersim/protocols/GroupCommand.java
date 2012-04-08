package it.polimi.peersim.protocols;

import java.io.Serializable;

public class GroupCommand implements Serializable {

	private static final long serialVersionUID = -8384815517803260341L;

	public static final String JOIN_REQUEST = "join_request";
	public static final String JOIN_REQUEST_RESPONSE = "join_request_response";

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
