/**
 * 
 */
package it.polimi.peersim.protocols;

import java.io.Serializable;

/**
 * @author pani
 *
 */
public class UniverseCommand implements Serializable {

	private static final long serialVersionUID = -8384815517803260341L;

	public static final String COUNT_LEADERS = "count_leaders";
	public static final String COUNT_LEADERS_RESPONSE = "count_leaders_response";

	private final String command;
	private final int content;
	
	public UniverseCommand(String command, int content) {
		super();
		this.command = command;
		this.content = content;
	}

	public String getCommand() {
		return command;
	}

	public int getContent() {
		return content;
	}
	
}
