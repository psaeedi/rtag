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
public class BroadcastContent implements Serializable{

	private static final long serialVersionUID = 4238344333725721404L;
	private int pid;
	private Serializable content;
	private Node sender;
	
	public BroadcastContent(Node sender, int pid, Serializable content) {
		super();
		this.sender = sender;
		this.pid = pid;
		this.content = content;
	}

	public int getPid() {
		return pid;
	}

	public Serializable getContent() {
		return content;
	}

	public Node getSender() {
		return sender;
	}

}
