package it.polimi.peersim.controls;

import java.util.ArrayList;

import it.polimi.peersim.protocols.DiscoveryProtocol;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;

public class WirePrtag implements Control{
	
	
	private static final String WIREPRTAG_PROTOCOL = "wireprtag_protocol";
	private static int wireprtagProtocolId;
	
	private static final String DISCOVERY_PROTOCOL = "discovery_protocol";
	private static int discoveryProtocolId;

	/**
	 * 
	 */
	public WirePrtag(String prefix)
	{
		super();
		wireprtagProtocolId = Configuration.getPid(
				prefix + "." + WIREPRTAG_PROTOCOL);
		discoveryProtocolId = Configuration.getPid(
				prefix + "." + DISCOVERY_PROTOCOL);
	}

	public boolean execute()
	{
		Node node;
		//current node check its neighbors!
		int size = Network.size();
		for (int i = 0; i < size; i++) {
			//check all the nodes if they are his neighbor
			node = Network.get(i);
			DiscoveryProtocol discoveryProtocol = (DiscoveryProtocol) node.getProtocol(
					discoveryProtocolId);
			ArrayList<Node> neighbors = discoveryProtocol.getNeighbors();
			for	(Node k: neighbors) {
				Linkable link =
						(Linkable) Network.get(i).getProtocol(wireprtagProtocolId);
					link.pack();
			}
		}
		return false;
	}
}

