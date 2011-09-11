package it.polimi.rtag;

import java.io.Serializable;
import java.net.ConnectException;
import java.net.MalformedURLException;

import lights.Tuple;

import it.polimi.rtag.filters.AnycastFilter;
import it.polimi.rtag.filters.BroadcastFilter;
import it.polimi.rtag.filters.GroupcastFilter;
import it.polimi.rtag.filters.UnicastFilter;
import it.polimi.rtag.messaging.MessageReport;
import it.polimi.rtag.messaging.TupleMessage;
import polimi.reds.Filter;
import polimi.reds.Message;
import polimi.reds.NodeDescriptor;
import polimi.reds.Reply;
import polimi.reds.broker.overlay.AlreadyNeighborException;
import polimi.reds.broker.overlay.GenericOverlay;
import polimi.reds.broker.overlay.NotRunningException;
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
	   * Note that reconfigurator assume that the implementation of this method is synchronized on the
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

  /**
   * Publish the given message coming from the specified neighbor. Depending on the routing policy
   * adopted, this requires to forward the given message to some or any of the neighbors of the
   * broker this router is part of.<br>
   * If <code>message</code> is instance of<code>Repliable</code>, the <code>Router</code>
   * needs to activate the <code>ReplyManager</code> to record the message in the
   * <code>ReplyTable</code>.
   * <p>
   * Note that reconfigurators assume that the implementation of this method is synchronized on the
   * router object, so that the reconfigurator can acquire the lock when it needs to make sure that
   * no router operations are executed concurrently with reconfiguration actions.
   * 
   * @param neighborID the identifier of the neighbor from which the message was received.
   * @param message the <code>Message</code> to be published.
   * 
   */
	@Override
	public void publish(NodeDescriptor sender, Message message) {
		//publish the message in the tuple space
		TupleMessage tmessage = (TupleMessage)message;
		Filter filter = tmessage.getFilter();
		if (filter instanceof UnicastFilter) {
			// TODO get recipient from filter
			// TODO overlay.send(title, tmessage, recipient);
		} else if (filter instanceof AnycastFilter){
			// TODO select a recipient matching the criteria
			// TODO overlay.send(title, tmessage, recipient);
		} else if (filter instanceof GroupcastFilter) {
			// TODO select the matching group
			// TODO send to all members
			// TODO add to tuple space?
		} else if (filter instanceof BroadcastFilter) {
			// TODO send to any one
			// TODO add to tuplespace?
		} else {
			throw new AssertionError("Unrecognizer filter type: " + filter);
		}
	}

	@Override
	public void setOverlay(Overlay overlay) {
		if (this.overlay != null) {
			throw new AssertionError("Overlay already configured");
		}
		this.overlay = overlay;
	}

	@Override
	public void subscribe(NodeDescriptor node, Filter filter) {
		// Check if the filter represents a group
		try {
			GroupcastFilter groupFilter = GroupcastFilter.createFromFilter(filter);
			// Check if this node is the right group leader
			GroupDescriptor groupDescriptor = groupFilter.getDescriptor();
			if (groupDescriptor.isLeader(currentDescriptor)) {
				try {
					overlay.addNeighbor(node.getUrls()[0]);
				} catch (AlreadyNeighborException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ConnectException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotRunningException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
			GroupcastFilter groupFilter = GroupcastFilter.createFromFilter(filter);
			// Check if this node is the right group leader
			GroupDescriptor groupDescriptor = groupFilter.getDescriptor();
			if (groupDescriptor.isLeader(currentDescriptor)) {
				// TODO Remove node to the group and notify all the other followers
				overlay.removeNeighbor(node);
			} else {
				// TODO decide what to do if the current node is not the 
				// group leader. We could reply with the leader address if known.
				// Otherwise we could simply send an error message.
			}
			
		} catch(IllegalArgumentException ex) {
			// Not a valid group filter: return
			return;
		}
	}

	@Override
	public void unsubscribeAll(NodeDescriptor node) {
		// Invoked before a node is quitting or if a node has collapsed
		overlay.removeNeighbor(node);
		// TODO the node has to be removed from all the groups
	}

  /**
   * This method is called by the <code>Overlay</code> whenever a new packet arrives from a
   * neighbor of the local node.
   * 
   * @param subject the subject the packet was addressed to.
   * @param source the <code>NodeDescriptor</code> of the neighbor the packet comes from.
   * @param data the received packet.
   */
	@Override
	public void notifyPacketArrived(String subject, NodeDescriptor sender,
			Serializable packet) {
		try {
			TupleMessage message = (TupleMessage)packet;
			if (message)
		} catch (ClassCastException ex) {
			throw new RuntimeException(
					"Invalid package type. Packages must be of type Tuple message. Found: " + 
					packet);
		}
	}
	
	@Override
	public NodeDescriptor getID() {
		return currentDescriptor;
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
