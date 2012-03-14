/**
 * 
 */
package it.polimi.peersim.protocols;

import peersim.core.Protocol;

/**
 * @author pani
 *
 */
public class GeoLocation implements Protocol {

	private double x;
	private double y;
	
	 public GeoLocation(String prefix) {
	        /* Un-initialized coordinates defaults to -1. */
	        x = y = -1;
	    }
	
	public Object clone() {
		GeoLocation inp = null;
        try {
            inp = (GeoLocation) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens
        return inp;
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
}
