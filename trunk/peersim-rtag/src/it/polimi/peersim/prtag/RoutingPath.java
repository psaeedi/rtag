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
    Node proxy;
    private long expiretime;
	

	public RoutingPath(Node destination, Node proxy) {
		super();
		this.destination = destination;
		this.proxy = proxy;
		
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


	public Node getProxy() {
		return proxy;
	}


	public void setProxy(Node source) {
		this.proxy = source;
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
					proxy.equals(path.proxy);
		}
		return super.equals(obj);
	}



}
