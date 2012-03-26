package it.polimi.peersim.initializers;

import it.polimi.peersim.protocols.DiscoveryProtocol;
import it.polimi.peersim.protocols.UniverseProtocol;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

/**
 * Initializes the {@link UniverseProtocol} by assigning 
 * the current node and by setting it as a change listener
 * for discovery events.
 * 
 * @author Panteha Saeedi@ elet.polimi.it
 *
 */
public class UniverseInitializer implements Control {
	
	private static final String UNIVERSE_PROTOCOL = "universe_protocol";
	private static final String DICSCOVERY_PROTOCOL = "discovery_protocol";

    /** Protocol identifier, obtained from config property {@link #UNIVERSE_PROTOCOL}. */
	private static int universeProtocolId;
	private static int discoveryProtocolId;
    
    /**
     * @param prefix
     */
    public UniverseInitializer(String prefix) {
    	universeProtocolId = Configuration.getPid(prefix + "." + UNIVERSE_PROTOCOL);
    	discoveryProtocolId = Configuration.getPid(prefix + "." + DICSCOVERY_PROTOCOL);
    }

	@Override
	public boolean execute() {
		for (int i = 0; i < Network.size(); i++) {
			Node n = Network.get(i);
			DiscoveryProtocol discoveryProtocol = (DiscoveryProtocol)
            		n.getProtocol(discoveryProtocolId);
			discoveryProtocol.initialize(n); 
            UniverseProtocol universeProtocol = (UniverseProtocol)
            		n.getProtocol(universeProtocolId);
            // create an empty universe and then send it to setUniverse(node, universe)
            // Each node should be initialized with its own universe
            universeProtocol.initialize(n); 
        }
		return false;
	}


	
}
