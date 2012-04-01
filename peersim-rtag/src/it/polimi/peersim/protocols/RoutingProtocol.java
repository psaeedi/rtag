/**
 * 
 */
package it.polimi.peersim.protocols;

import java.io.Serializable;
import java.util.ArrayList;

import it.polimi.peersim.messages.BaseMessage;
import it.polimi.peersim.messages.RoutingMessage;
import it.polimi.peersim.prtag.LocalUniverseDescriptor;
import it.polimi.peersim.prtag.RoutingPath;

import com.google.common.collect.HashMultimap;
import peersim.core.Node;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 */
public class RoutingProtocol extends ForwardingProtocol<RoutingMessage> {
	
	HashMultimap<Node, RoutingPath> routingTable = HashMultimap.create();

	public RoutingProtocol(String prefix) {
		super(prefix);
	}

	@Override
	public Object clone() {
		RoutingProtocol clone = null;
		clone = (RoutingProtocol) super.clone();
		clone.routingTable = HashMultimap.create(routingTable);
        return clone;
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

	@Override
	public RoutingMessage handlePushDownMessage(Node currentNode,
			Node recipient, Serializable content) {
		// Mock implementation using the recipient as a proxy
		return new RoutingMessage(protocolId, recipient, recipient, content);
	}

	@Override
	public BaseMessage handlePushUpMessage(Node currentNode, Node sender,
			RoutingMessage message) {
		if (currentNode.equals(message.getRecipient())) {
			return (BaseMessage) message.getContent();
		} else {
			// Forward the message
			pushDownMessage(
					currentNode, message.getRecipient(), message.getContent());
			return null;
		}
	}

}
