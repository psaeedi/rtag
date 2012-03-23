package it.polimi.peersim.protocols;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.core.Protocol;
import it.polimi.peersim.protocols.GeoLocation;

import java.util.*; 


public class DiscoveryProtocol implements Protocol{
	
	private static final String GEOLOCATION_PROTOCOL = "geolocation_protocol";
	private static int geolocationProtocolId;
	
	private static final String DISCOVERY_RADIUS = "discovery_radius";
	protected final double discoveryRadius;
	
	private static final String UNIVERSE_PROTOCOL = "universe_protocol";
	private static int universeProtocolId;
	
	private ArrayList<Node> neighbors = new ArrayList<Node>();
	
	private Node currentNode;

	public DiscoveryProtocol(String prefix) {
		geolocationProtocolId = Configuration.getPid(
				prefix + "." + GEOLOCATION_PROTOCOL);
		discoveryRadius = Configuration.getDouble(
				prefix + "." + DISCOVERY_RADIUS, 0.01);
		universeProtocolId = Configuration.getPid(
				prefix + "." + UNIVERSE_PROTOCOL);
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
		
		if (newNeighbors.isEmpty()){
			throw new RuntimeException("no neighbor find-isolated node");
		}
	
		ArrayList<Node> added = new ArrayList<Node>(newNeighbors);
		//added has now  all the neighbors(new and old)
		//remove all the old neighbors
		added.removeAll(this.getNeighbors());

		ArrayList<Node> removed = new ArrayList<Node>(this.getNeighbors());
		//removed has now all the old neighbors
		removed.removeAll(newNeighbors);

		this.setNeighbors(newNeighbors);

		UniverseProtocol universeProtocol = (UniverseProtocol) 
				currentNode.getProtocol(universeProtocolId);
		//add to the list of ur neighbors
		if (!added.isEmpty()){
			//we inform the current node of its neighbor
			universeProtocol.notifyAddedNodes(added);
			//System.out.println("A Node " + " is added to the neighbor list of Node:" +
					//currentNode.getID());
		}
		
		if (!removed.isEmpty()){
			universeProtocol.notifyRemovedNodes(removed);
			//System.out.println("A Node " + " is removed from the neighbor list of Node:" +
					//currentNode.getID());
		}
		
		System.out.println("Node "+currentNode.getID()+"neighbors:" + neighbors.size()); 
	}

	

	public boolean isCloseTo(Node n, Node k) {
		
		/*if(distance(n,k)> discoveryRadius){
			return false;
		}*/
		//System.out.println("DiscoveryProtocol-isclose!");
		return (distance(n, k) < discoveryRadius);
		//return true;
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
        try {
            inp = (DiscoveryProtocol) super.clone();
            inp.setNeighbors((ArrayList<Node>) this.getNeighbors().clone());
            inp.currentNode = this.currentNode;
        } catch (CloneNotSupportedException e) {
        	e.printStackTrace();
        } // never happens
        return inp;
    }


	public ArrayList<Node> getNeighbors() {
		return neighbors;
	}


	public void setNeighbors(ArrayList<Node> neighbors) {
		this.neighbors = neighbors;
	}
	
}
