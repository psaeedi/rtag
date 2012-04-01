/**
 * 
 */
package it.polimi.peersim.messages;

import java.io.Serializable;

/**
 * @author Panteha Saeedi
 * 
 * A base message
 *
 */
public class BaseMessage implements Serializable {

	private static final long serialVersionUID = -4434574380875379068L;

	private final int pid;
	private final Serializable content;
	
	/**
	 * @param pid
	 */
	public BaseMessage(int pid, Serializable content) {
		super();
		this.pid = pid;
		this.content = content;
	}

	public int getPid() {
		return pid;
	}
	
	public Serializable getContent() {
		return content;
	}
}
