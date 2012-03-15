package it.polimi.peersim.protocols;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.core.Protocol;
import it.polimi.peersim.protocols.GeoLocation;
import it.polimi.peersim.prtag.GroupDescriptor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*; 

public class DiscoveryProtocol implements Protocol{
	
	private static final String GEOLOCATION_PROTOCOL = "geolocation_protocol";
	private static int geolocationProtocolId;
	
	private static final String DISCOVERY_RADIUS = "discovery_radius";
	protected final double discoveryRadius;
	
	private static final String UNIVERSE_PROTOCOL = "universe_protocol";
	private static int universeProtocolId;
	
	private ArrayList<Node> neighbors = new ArrayList<Node>();
	
	public final static String NODES_DISCOVERED = "NODES_DISCOVERED";
	public final static String NODES_LOST = "NODES_LOST";
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
	public void initialize(Node currentNode){//, DiscoveryProtocol discoveryProtocol) {
		this.currentNode = currentNode;
	}

	public void updateNeighbourhood(ArrayList<Node> newNeighbors) {
		//added has now  all the neighbors(new and old)
	
		if (newNeighbors.isEmpty()){
			//System.out.println("updatenieighborhood-empty1-no neighbor find-isolated");
			throw new RuntimeException("no neighbor find-isolated node");
		}
	
		
		ArrayList<Node> added = new ArrayList<Node>(newNeighbors);
		/*if (added.isEmpty()){
			System.out.println("empty2");
		}*/
		//remove all the old neighbors
		added.removeAll(this.getNeighbors());
		//added.removeAll(newNeighbors);
		
		//removed has now all the old neighbors
		ArrayList<Node> removed = new ArrayList<Node>(this.getNeighbors());
		removed.removeAll(newNeighbors);

		this.setNeighbors(neighbors);

		UniverseProtocol universeProtocol = (UniverseProtocol) 
				currentNode.getProtocol(universeProtocolId);
		//add to the list of ur neighbors
		if (!added.isEmpty()){
			universeProtocol.notifyAddedNode(added);
		}
		
		if (!removed.isEmpty()){
			universeProtocol.notifyRemovedNode(removed);
			System.out.println("Node " + " is removed from the neighbor list" + added.indexOf(added));
		}
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
        } catch (CloneNotSupportedException e) {
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
