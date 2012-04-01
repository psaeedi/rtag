/**
 * 
 */
package it.polimi.peersim.protocols;

import peersim.cdsim.DaemonProtocol;
import peersim.core.Node;

/**
 * @author pani
 *
 * Defines the geoloaction of each node.
 */
public class GeoLocation extends DaemonProtocol {

	private double x;
	private double y;
	
	 public GeoLocation(String prefix) {
		 super(prefix);
	        /* Un-initialized coordinates defaults to -1. */
	        x = y = -1;
	    }
	
	public Object clone() {
		GeoLocation clone = (GeoLocation) super.clone();
        clone.x = x;
        clone.y = y;
        return clone;
    }

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	@Override
	public void nextCycle(Node node, int protocolID) {
		// TODO Move the nodes around
		super.nextCycle(node, protocolID);
	}

}
