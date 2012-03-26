/**
 * 
 */
package it.polimi.peersim.prtag;

import peersim.core.Node;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 */
public class RoutingPath {
	
	Node destination;
    Node source;
    private long expiretime;
	

	public RoutingPath(Node destination, Node source) {
		super();
		this.destination = destination;
		this.source = source;
		
		//keeping the routing table for 5 min
		//TODO use simulation time
		expiretime = System.currentTimeMillis()+ 60 * 1000 * 5;
	}


	public Node getDestination() {
		return destination;
	}


	public void setDestination(Node destination) {
		this.destination = destination;
	}


	public Node getSource() {
		return source;
	}


	public void setSource(Node source) {
		this.source = source;
	}
	
	public boolean isExpired(){
		//TODO we need to use simulation time instead
		return System.currentTimeMillis()> expiretime;
		
	}


	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RoutingPath) {
			RoutingPath path = (RoutingPath)obj;
			return destination.equals(path.destination) &&
					source.equals(path.source);
		}
		return super.equals(obj);
	}



}
