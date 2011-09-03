package it.polimi.rtag;

import polimi.reds.NodeDescriptor;

public class Node {

    public NodeDescriptor currentDescriptor;

	public NodeDescriptor getCurrentDescriptor() {
		return currentDescriptor;
	}

	public void setCurrentDescriptor(NodeDescriptor currentDescriptor) {
		this.currentDescriptor = currentDescriptor;
	}

}
