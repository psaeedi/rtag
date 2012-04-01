package it.polimi.peersim.initializers;

import it.polimi.peersim.protocols.GroupingProtocol;
import it.polimi.peersim.protocols.DiscoveryProtocol;
import it.polimi.peersim.protocols.GeoLocation;
import it.polimi.peersim.protocols.UniverseProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
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
public class ProtocolStackInitializer implements Control {
	
    private static final String GEOLOCATION_PROTOCOL = "geolocation_protocol";
    private static final String DICSCOVERY_PROTOCOL = "discovery_protocol";
	private static final String UNIVERSE_PROTOCOL = "universe_protocol";
	
    /** Protocol identifier, obtained from config property {@link #UNIVERSE_PROTOCOL}. */
    private final int geolocationId;
    private final int discoveryProtocolId;
	private final int universeProtocolId;

	
    /**
     * @param prefix
     */
    public ProtocolStackInitializer(String prefix) {
    	geolocationId = Configuration.getPid(prefix + "." + GEOLOCATION_PROTOCOL);
    	discoveryProtocolId = Configuration.getPid(prefix + "." + DICSCOVERY_PROTOCOL);
    	universeProtocolId = Configuration.getPid(prefix + "." + UNIVERSE_PROTOCOL);   	
    }

	@Override
	public boolean execute() {
		System.err.println("=======Executing ProtocolStackInitializer");
		
		GeoLocation location;
		
		for (int i = 0; i < Network.size(); i++) {
			
			Node n = Network.get(i);
	        location = (GeoLocation) n.getProtocol(geolocationId);
	        location.setX(5*CommonState.r.nextDouble());
	        location.setY(5*CommonState.r.nextDouble());
			
			initializeDiscovery(n);
			initializeUniverse(n);
        }
		return false;
	}

	public void initializeDiscovery(Node n) {
		DiscoveryProtocol discoveryProtocol = (DiscoveryProtocol)
        		n.getProtocol(discoveryProtocolId);
		discoveryProtocol.initialize(n); 
	}
	
	public void initializeUniverse(Node n) {
		UniverseProtocol universeProtocol = (UniverseProtocol)
        		n.getProtocol(universeProtocolId);
        // create an empty universe and then send it to setUniverse(node, universe)
        // Each node should be initialized with its own universe
        universeProtocol.initialize(n);
	}
}
