/**
 * 
 */
package it.polimi.peersim.protocols;

import it.polimi.peersim.prtag.LocalUniverseDescriptor;
import it.polimi.peersim.prtag.RoutingPath;

import com.google.common.collect.HashMultimap;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 */
public class RoutingProtocol implements Transport, EDProtocol {
	
	HashMultimap<Node, RoutingPath> routingTable = HashMultimap.create();

	public RoutingProtocol(String prefix) {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object clone() {
		RoutingProtocol inp = null;
        try {
        	inp = (RoutingProtocol) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens
        return inp;
	}

	@Override
	public void processEvent(Node arg0, int arg1, Object arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getLatency(Node arg0, Node arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void send(Node src, Node dest, java.lang.Object msg, int pid) {
		// TODO this is the key method of this protocol
		// Fake implementation
		// Instead of passing from node to node we just send it to the destination.
		EDProtocol protocol = (EDProtocol)dest.getProtocol(pid);
		protocol.processEvent(dest, pid, msg);
	}
	
	public void removeExpiredPath(){
		//TODO remove all the expired entry from the table
	}
	
	public void removeLostPath(Node lostNode){
		// remove all the lost entry from the table
		this.routingTable.removeAll(lostNode);
		// TODO remove all the entries with that node as a source
	}
	
	public void addPath(LocalUniverseDescriptor localUniverse){
		// TODO it creates the path for all the leaders and 
		// the followers of that descriptor
		for (Node follower: localUniverse.getFollowers()){
			 RoutingPath routingpath = new RoutingPath(follower, 
					 localUniverse.getLeader());
		}
		
	}

}
