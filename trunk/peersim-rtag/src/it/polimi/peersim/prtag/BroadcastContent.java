/**
 * 
 */
package it.polimi.peersim.prtag;

import java.io.Serializable;

/**
 * @author pani
 *
 */
public abstract class BroadcastContent implements Serializable{

	private int pid;
	private Serializable content;

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}
}
