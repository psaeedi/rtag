/**
 * 
 */
package it.polimi.rtag;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.HashMultimap;

import polimi.reds.NodeDescriptor;
import polimi.reds.broker.overlay.AlreadyNeighborException;
import polimi.reds.broker.overlay.NotRunningException;
import polimi.reds.broker.overlay.SimpleTopologyManager;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class GroupAwareTopologyManager extends SimpleTopologyManager {

	private Set<NodeDescriptor> applicationNodes = new HashSet<NodeDescriptor>();
	
	private HashMultimap<NodeDescriptor, UUID> groupsByNode = HashMultimap.create();
	
	private Object lock = new Object();
	/**
	 * 
	 */
	public GroupAwareTopologyManager() {
		// TODO Auto-generated constructor stub
	}

	/** 
	 * When the application wants to connect a new node.
	 * 
	 * @see polimi.reds.broker.overlay.SimpleTopologyManager#addNeighbor(java.lang.String)
	 */
	@Override
	public NodeDescriptor addNeighbor(String url) throws NotRunningException,
			MalformedURLException, AlreadyNeighborException, ConnectException {
		NodeDescriptor descriptor = super.addNeighbor(url);
		if (descriptor != null) {
			synchronized (lock) {
				applicationNodes.add(descriptor);				
			}
		}
		return descriptor;
	}

	/**
	 * When the application wants to release a node.
	 * 
	 * @see polimi.reds.broker.overlay.SimpleTopologyManager#removeNeighbor(polimi.reds.NodeDescriptor)
	 */
	@Override
	public void removeNeighbor(NodeDescriptor descriptor) {
		boolean canBeRemoved = true;
		synchronized (lock) {
			applicationNodes.remove(descriptor);
			canBeRemoved = !groupsByNode.containsKey(descriptor);
		}
		if (canBeRemoved) {
			super.removeNeighbor(descriptor);
		}
	}

	
	public NodeDescriptor addNeighborForGroup(NodeDescriptor descriptor, 
			GroupDescriptor groupDescriptor) throws NotRunningException {
		
		if (descriptor.equals(getNodeDescriptor())) {
			throw new RuntimeException("Should NOT connect to itself.");
		}
		
		NodeDescriptor node = null;
		if (isNeighborOf(descriptor)) {
			node = descriptor;
		} else {
			for (String url: descriptor.getUrls()) {
				try {
					// TODO this may create a deadlock
					node = super.addNeighbor(url);
				} catch (AlreadyNeighborException ex) {
					node = descriptor;
				} catch (Exception ex) {
					continue;
				}
				
			}
		}
		if (node != null) {
			synchronized (lock) {
				groupsByNode.put(node, groupDescriptor.getUniqueId());
			}
		}
		return node;
		
	}
	
	public void removeNeighboorForGroup(NodeDescriptor descriptor,
			GroupDescriptor groupDescriptor) {
		boolean canBeRemoved = false;
		synchronized (lock) {
			groupsByNode.remove(descriptor, groupDescriptor.getUniqueId());
			if (groupsByNode.get(descriptor).size() == 0) {
				groupsByNode.removeAll(descriptor);
			}
			canBeRemoved = (!groupsByNode.containsKey(descriptor) ||
					groupsByNode.get(descriptor).size() == 0 || !applicationNodes.contains(descriptor));
		}
		if (canBeRemoved) {
			super.removeNeighbor(descriptor);
		}
	}
	
	public void removeNodesForGroup(GroupDescriptor groupDescriptor) {
		Set<NodeDescriptor> keys = null;
		synchronized (lock) {
			keys = new HashSet<NodeDescriptor>(groupsByNode.keySet());
		}
		for (NodeDescriptor descriptor: keys) {
			removeNeighboorForGroup(descriptor, groupDescriptor);
		}
	}
	
	public int getApplicationConnectionCount() {
		synchronized (lock) {
			return applicationNodes.size();
		}
	}
	
	public int getMiddlewareConnectionCount() {
		synchronized (lock) {
			return groupsByNode.keySet().size();
		}
	}

}
