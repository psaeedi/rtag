package it.polimi.peersim.controls;

import java.util.ArrayList;

import it.polimi.peersim.protocols.DiscoveryProtocol;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class DiscoveryController implements Control {
	
	private static final String DISCOVERY_PROTOCOL = "discovery_protocol";
	private static int discoveryProtocolId;
	
	
	//Neighbor Distance is initialized to 1

	public DiscoveryController(String prefix) {
		super();
		discoveryProtocolId = Configuration.getPid(
				prefix + "." + DISCOVERY_PROTOCOL);
       }

	@Override
	public boolean execute() {
		
		Node n;
		Node k;
		for (int i = 0; i < Network.size(); i++) {
            n = Network.get(i);
            DiscoveryProtocol discoveryProtocol = (DiscoveryProtocol) n.getProtocol(
            		discoveryProtocolId);
            ArrayList<Node> neighbours = new ArrayList<Node>();
            // TODO explore only the top matrix
            for (int j = 0; j < Network.size(); j++) {
                if (i == j) {
                	continue;
                }
            	k = Network.get(j);
            	if (discoveryProtocol.isCloseTo(n, k)) {
            		neighbours.add(k);
            	}
            }
            
            if (neighbours.isEmpty()){
    			System.out.println("oemptyo");
    		}
            discoveryProtocol.updateNeighbourhood(neighbours);
        }
		
		return false;
	}
		
	

}
