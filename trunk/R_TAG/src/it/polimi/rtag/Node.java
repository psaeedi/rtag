package it.polimi.rtag;

import java.io.Serializable;
import java.util.TreeSet;
import java.util.logging.Logger;

import lights.Tuple;

import it.polimi.rtag.messaging.MessageReport;
import polimi.reds.Filter;
import polimi.reds.Message;
import polimi.reds.MessageID;
import polimi.reds.NodeDescriptor;
import polimi.reds.Reply;
import polimi.reds.broker.overlay.GenericOverlay;
import polimi.reds.broker.overlay.Overlay;
import polimi.reds.broker.overlay.SimpleTopologyManager;
import polimi.reds.broker.overlay.TCPTransport;
import polimi.reds.broker.overlay.TopologyManager;
import polimi.reds.broker.overlay.Transport;
import polimi.reds.broker.routing.GenericTable;
import polimi.reds.broker.routing.HashReplyTable;
import polimi.reds.broker.routing.ImmediateForwardReplyManager;
import polimi.reds.broker.routing.ReplyManager;
import polimi.reds.broker.routing.ReplyTable;
import polimi.reds.broker.routing.Router;
import polimi.reds.broker.routing.RoutingStrategy;
import polimi.reds.broker.routing.SubscriptionForwardingRoutingStrategy;
import polimi.reds.broker.routing.SubscriptionTable;




public class Node implements Router{

    public NodeDescriptor currentDescriptor;
    private GroupingStrategy strategy;
    private Overlay overlay;
    private RoutingStrategy routingStrategy;
	private ReplyManager replyManager;
	SubscriptionTable subscriptionTable;
	ReplyTable replyTable;
	private Object nodeReply;
    
	public Node(String address, int port) {
		/**
		 * This interface is the "core" of a REDS broker. Classes that implement this interface constitutes
		 * the main component of a REDS broker. It is registered to the <code>Overlay</code> to receive
		 * messages coming from the other nodes of the network. In most cases it delegates its main
		 * functionalities to the other components that compose a REDS broker: namely, the
		 * <code>RoutingStrategy</code> and the <code>ReplyManager</code>. It holds the common data
		 * structure managed by those components: the <code>SubscriptionTable</code> and the
		 * <code>ReplyTable</code>.
		 */
		
		// TODO implement a new routing topology manager
		TopologyManager topologyManager = new SimpleTopologyManager();
		Transport transport = new TCPTransport(port);
		
		overlay = new GenericOverlay(topologyManager, transport);
		
		// TODO implement a new routing strategy
		routingStrategy = new SubscriptionForwardingRoutingStrategy();
		
		// TODO implement a new reply strategy
		replyManager = new ImmediateForwardReplyManager();
		
		subscriptionTable = new GenericTable();
		
		replyTable = new HashReplyTable();
	}
    
    
	  /**
	   * Forwards the given reply to to its sender. According to the informations contained into the
	   * local reply table the reply will be sent to a specific neighbor or dropped if no information
	   * about its sender is contained in the reply table.
	   * <p>
	   * Note that reconfigurators assume that the implementation of this method is synchronized on the
	   * router object, so that the reconfigurator can acquire the lock when it needs to make sure that
	   * no router operations are executed concurrently with reconfiguration actions.
	   * 
	   * @param reply the reply to be sent.
	   */
	@Override
	public void forwardReply(Reply reply) {
		replyManager.forwardReply(reply);
	  }

	@Override
	public Overlay getOverlay() {
		return overlay;
	}

	@Override
	public ReplyTable getReplyTable() {
		return replyTable;
	}

	@Override
	public SubscriptionTable getSubscriptionTable() {
		return subscriptionTable;
	}

	@Override
	public void publish(NodeDescriptor arg0, Message arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOverlay(Overlay overlay) {
		this.overlay = overlay;
	}

	@Override
	public void subscribe(NodeDescriptor node, Filter filter) {
		// Check if the filter represents a group
		try {
			GroupFilter groupFilter = GroupFilter.createFromFilter(filter);
			// Check if this node is the right group leader
			GroupDescriptor groupDescriptor = groupFilter.getDescriptor();
			if (groupDescriptor.isLeader(currentDescriptor)) {
				// TODO Add node to the group and notify all the other followers
			} else {
				// TODO decide what to do if the current node is not the 
				// group leader. We could reply with the leader address if known.
				// Otherwhise we could simply send an error message.
			}
			
		} catch(IllegalArgumentException ex) {
			// Not a valid group filter: return
			return;
		}
	}

	@Override
	public void unsubscribe(NodeDescriptor node, Filter filter) {
		// Check if the filter represents a group
		try {
			GroupFilter groupFilter = GroupFilter.createFromFilter(filter);
			// Check if this node is the right group leader
			GroupDescriptor groupDescriptor = groupFilter.getDescriptor();
			if (groupDescriptor.isLeader(currentDescriptor)) {
				// TODO Remove node to the group and notify all the other followers
			} else {
				// TODO decide what to do if the current node is not the 
				// group leader. We could reply with the leader address if known.
				// Otherwhise we could simply send an error message.
			}
			
		} catch(IllegalArgumentException ex) {
			// Not a valid group filter: return
			return;
		}
	}

	@Override
	public void unsubscribeAll(NodeDescriptor node) {
		// Invoked before a node is quitting or if a node has collapsed
		// TODO the node has to be removed from all the groups
	}

	@Override
	public void notifyPacketArrived(String arg0, NodeDescriptor arg1,
			Serializable arg2) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public NodeDescriptor getID() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setStrategy(GroupingStrategy strategy) {
		this.strategy = strategy;
	}

	public GroupingStrategy getStrategy() {
		return strategy;
	}

    

	public NodeDescriptor getCurrentDescriptor() {
		return currentDescriptor;
	}

	public void setCurrentDescriptor(NodeDescriptor currentDescriptor) {
		this.currentDescriptor = currentDescriptor;
	}
	
	/**
	 * Attempts to join a group and if it does not exist creates a new one
	 */
	public boolean joinOrCreate(Tuple groupDescription) {
		// TODO fix tuple with lights
		throw new AssertionError("Not yet implemented error.");
	}

	public MessageReport sendMessage(Message msg, NodeDescriptor... recipients) {
		throw new AssertionError("Not yet implemented error.");
	}
	
	
	protected void checkLeaderStatus() {
		throw new AssertionError("Not yet implemented error.");
	}
}
