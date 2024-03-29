package it.polimi.rtag;

import it.polimi.rtag.app.AbstractApp;
import it.polimi.rtag.messaging.TupleMessage;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.List;

import polimi.reds.NodeDescriptor;
import polimi.reds.broker.overlay.AlreadyNeighborException;
import polimi.reds.broker.overlay.NotRunningException;
import polimi.reds.broker.overlay.Overlay;
import polimi.reds.broker.overlay.TCPWorkingTransport;
import polimi.reds.broker.overlay.Transport;


/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)</p>.
 * 
 * TODO this should handle a collection of {@link GroupDiscoveredNotificationListener}
 */
public class Node {
    
    private GroupAwareTopologyManager topologyManager;
    private Overlay overlay;
    private TupleSpaceManager tupleSpaceManager;
    
    private GroupCommunicationDispatcher groupCommunicationDispatcher;

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
		
		topologyManager = new GroupAwareTopologyManager();
		Transport transport = new TCPWorkingTransport(port);
			
		setOverlay(new MessageCountingGenericOverlay(topologyManager, transport));
		
		groupCommunicationDispatcher = new GroupCommunicationDispatcher(this);
		tupleSpaceManager = new TupleSpaceManager(overlay, this);
		groupCommunicationDispatcher.setTupleSpaceManager(tupleSpaceManager);
		
		//groupCommunicationDispatcher.join(GroupCommunicationManager.createUniverseCommunicationManager(this));
		groupCommunicationDispatcher.joinGroupAndNotifyNetwork(GroupDescriptor.UNIVERSE);
	}

	public TupleSpaceManager getTupleSpaceManager() {
		return tupleSpaceManager;
	}
	
	public Overlay getOverlay() {
		return overlay;
	}
	
	void setOverlay(Overlay overlay) {
		if (this.overlay != null) {
			throw new AssertionError("Overlay already configured");
		}
		this.overlay = overlay;
	}	
	
	public NodeDescriptor getNodeDescriptor() {
		return overlay.getNodeDescriptor();
	}

	public void start() {
		overlay.start();
	}

	public void stop() {
		groupCommunicationDispatcher.removeAllGroupsAndDisconnect();
		overlay.stop();
	}

	/**
	 * @return the groupCommunicationDispatcher
	 */
	public GroupCommunicationDispatcher getGroupCommunicationDispatcher() {
		return groupCommunicationDispatcher;
	}

	public void setApplication(AbstractApp app) {
		this.getTupleSpaceManager().setApplication(app);
	}
	
	public void storeAndSend(TupleMessage message) {
		this.getTupleSpaceManager().storeAndSend(message);
	}
	
	/**
	 * @return the topologyManager
	 */
	public GroupAwareTopologyManager getTopologyManager() {
		return topologyManager;
	}

	public GroupDescriptor joinGroup(String friendlyName) {
		// if string is equal to the constant universe, rise an exception
		if (GroupDescriptor.UNIVERSE.equals(friendlyName)) {
			throw new RuntimeException("The application cannot join the universe.");
		}
		// otherwise tell dispatcher to create and join a group of what we need
		return groupCommunicationDispatcher.joinGroupAndNotifyNetwork(friendlyName);
	}

	public void leaveGroup(String friendlyName) {
		if (GroupDescriptor.UNIVERSE.equals(friendlyName)) {
			throw new RuntimeException("The application cannot leave the universe.");
		}
		groupCommunicationDispatcher.leaveGroupsWithName(friendlyName);
		
	}
	
	public void deleteGroup(String friendlyName) {
		groupCommunicationDispatcher.deleteGroup(friendlyName);
	}

	public GroupDescriptor getGroup(String friendlyName) {
		return groupCommunicationDispatcher.getGroupForHierarchy(friendlyName);
	}

	public List<GroupDescriptor> getAllGroups() {
		return groupCommunicationDispatcher.getAllGroups();
	}
	
	public NodeDescriptor addNeighbor(String url) throws AlreadyNeighborException, 
			ConnectException, MalformedURLException, NotRunningException {
		return overlay.addNeighbor(url);
	}
	
}
