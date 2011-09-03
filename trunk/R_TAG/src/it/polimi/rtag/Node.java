package it.polimi.rtag;

import it.polimi.rtag.messaging.MessageReport;
import polimi.reds.NodeDescriptor;

public class Node {

    public NodeDescriptor currentDescriptor;

	public NodeDescriptor getCurrentDescriptor() {
		return currentDescriptor;
	}

	public void setCurrentDescriptor(NodeDescriptor currentDescriptor) {
		this.currentDescriptor = currentDescriptor;
	}
	
	/**
	 * Attempts to join a group and if it does not exist creates a new one
	 */
	public boolean joinOrCreate(Tuple groupDescription) {
		// TODO fix tuple with lights
		throw new AssertionError("Not yet implemented error.");
	}

	public MessageReport sendMessage(Message msg, NodeDescriptor... recipients) {
		throw new AssertionError("Not yet implemented error.");
	}
	
	
	protected void checkLeaderStatus {
		throw new AssertionError("Not yet implemented error.");
	}
}
