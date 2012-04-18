package it.polimi.peersim.protocols;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import it.polimi.peersim.protocols.GeoLocation;
import peersim.cdsim.CDState;
import peersim.cdsim.DaemonProtocol;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 */

import java.util.*; 


/**
 * @author Panteha Saeedi
 * 
 * Simulates the discovery device that we are using.
 * This could be Bluetooth or RFID for example.
 */
public class DiscoveryProtocol extends DaemonProtocol {
	
	private static final String GEOLOCATION_PROTOCOL = "geolocation_protocol";
	private final int geolocationProtocolId;
	
	private static final String DISCOVERY_RADIUS = "discovery_radius";
	protected final double discoveryRadius;
	
	// Discovery listeners
	private static final String UNIVERSE_PROTOCOL = "universe_protocol";
	private final int universeProtocolId;
	
	
	private ArrayList<Node> neighbors = new ArrayList<Node>();
	
	private Node currentNode;
	

	public DiscoveryProtocol(String prefix) {
		super(prefix);

		geolocationProtocolId = Configuration.getPid(
				prefix + "." + GEOLOCATION_PROTOCOL);
		discoveryRadius = Configuration.getDouble(
				prefix + "." + DISCOVERY_RADIUS, 0.1);
		universeProtocolId = Configuration.getPid(
				prefix + "." + UNIVERSE_PROTOCOL);
    }
	
	
	public void nextCycle(Node n, int protocolID ) {
		if( CDState.getCycle() % 10 != 0 ) return;
		 ArrayList<Node> neighbours = new ArrayList<Node>();
         // TODO explore only the top matrix
         for (int j = 0; j < Network.size(); j++) {
        	 Node k = Network.get(j);
             if (n.getID() == k.getID()) {
             	continue;
             }
            //if(neighbours.size()<20){
	         	if (isInCommunicationRange(n, k)) {
	         		//because of java heap we limit the number of neighbors to 100.
	         		neighbours.add(k);
	         	}
            //}
         }
         System.out.println("--------------------------CYCLE"+CDState.getCycle());
         updateNeighbourhood(neighbours);
	}
	
	
	
	/**
	 * Sets the current node and create a new universe for it.
	 * Also set the current instance as a change listener for the
	 * discovery protocol.
	 * 
	 * @param currentNode the current node.
	 */
	public void initialize(Node currentNode) {
		this.currentNode = currentNode;
	}

	public void updateNeighbourhood(ArrayList<Node> newNeighbors) {
		//at the first cycle if there are nodes isolated inform us
		//after crash isolation is ignored
		if (newNeighbors.isEmpty() &&  CDState.getCycle() == 1){
			throw new RuntimeException("no neighbor found-isolated node");
		}
	
		ArrayList<Node> added = new ArrayList<Node>(newNeighbors);
		//added has now  all the neighbors(new and old)
		//remove all the old neighbors
		added.removeAll(this.getNeighbors());

		ArrayList<Node> removed = new ArrayList<Node>(this.getNeighbors());
		//removed has now all the old neighbors
		removed.removeAll(newNeighbors);

		this.setNeighbors(newNeighbors);

		DiscoveryListener universeProtocol = (DiscoveryListener) 
				currentNode.getProtocol(universeProtocolId);
		
		System.out.println("+++++++++++++++++++++++++++++++++++++removed"+removed.size());
		//add to the list of ur neighbors
		if (!added.isEmpty()){
			// Notify the higher layers that certain nodes have been discovered.
			universeProtocol.notifyAddedNodes(currentNode, added);
		}
		
		if (!removed.isEmpty()){
			// Notify the higher layers that certain nodes have been removed.
			universeProtocol.notifyRemovedNodes(currentNode, removed);
		}
		System.out.println("Node "+currentNode.getID() + " neighbors: " + neighbors.size()); 
	}

	

	public boolean isInCommunicationRange(Node n, Node k) {
		return (distance(n, k) < discoveryRadius);
	}

	public double distance(Node n, Node k) {
		GeoLocation geoN = (GeoLocation) n.getProtocol(geolocationProtocolId);
		GeoLocation geoK = (GeoLocation) k.getProtocol(geolocationProtocolId);
		
        double x1 = geoN.getX();
        double x2 = geoK.getX();
        double y1 = geoN.getY();
        double y2 = geoK.getY();
        if (x1 == -1 || x2 == -1 || y1 == -1 || y2 == -1)
            throw new RuntimeException( "Found un-initialized coordinate." +
                   "InetInitializer class in the config file.");
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }
	
	
	public Object clone() {
		DiscoveryProtocol inp = null;
       
            inp = (DiscoveryProtocol) super.clone();
            inp.setNeighbors((ArrayList<Node>) this.getNeighbors().clone());
            inp.currentNode = this.currentNode;
        
        return inp;
    }


	public ArrayList<Node> getNeighbors() {
		return neighbors;
	}


	public void setNeighbors(ArrayList<Node> neighbors) {
		this.neighbors = neighbors;
	}
	
	
	
}
