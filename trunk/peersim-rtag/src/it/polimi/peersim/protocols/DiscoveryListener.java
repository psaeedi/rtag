package it.polimi.peersim.protocols;

import java.util.ArrayList;

import peersim.core.Node;

public interface DiscoveryListener {

	public abstract void notifyAddedNodes(Node currentNode,
			ArrayList<Node> added);

	public abstract void notifyRemovedNodes(Node currentNode,
			ArrayList<Node> removed);

}