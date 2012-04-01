package it.polimi.peersim.initializers;

import it.polimi.peersim.protocols.GeoLocation;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 */
public class GeoInitializer implements Control {
    
	/**
     * The protocol to operate on.
     * 
     * @config
     */
    private static final String GEOLOCATION_PROTOCOL = "geolocation_protocol";

    /** Protocol identifier, obtained from config property {@link #GEOLOCATION_PROTOCOL}. */
    private final int geolocationId;

    /**
     * Standard constructor that reads the configuration parameters. Invoked by
     * the simulation engine.
     * 
     * @param prefix
     *            the configuration prefix for this class.
     */
    public GeoInitializer(String prefix) {
    	geolocationId = Configuration.getPid(prefix + "." + GEOLOCATION_PROTOCOL);
    }

    /**
     * Initialize the node coordinates. The first node in the {@link Network} is
     * the root node by default and it is located in the middle (the center of
     * the square) of the surface area.
     */
    public boolean execute() {
    	System.err.println("=======Executing GeoInitializer");
    	
        // Set coordinates x,y
    	GeoLocation location;
    	Node n;
        for (int i = 0; i < Network.size(); i++) {
            n = Network.get(i);
            location = (GeoLocation) n.getProtocol(geolocationId);
            location.setX(5*CommonState.r.nextDouble());
            location.setY(5*CommonState.r.nextDouble());
        }
        return false;
    }

	public void initialize(Node n) {
		// TODO Auto-generated method stub
		
	}

}
