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

	private Set<NodeDescriptor> ungroupedNodes = new HashSet<NodeDescriptor>();
	
	private HashMultimap<NodeDescriptor, UUID> groupsByNode = HashMultimap.create();
	
	/**
	 * 
	 */
	public GroupAwareTopologyManager() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see polimi.reds.broker.overlay.SimpleTopologyManager#addNeighbor(java.lang.String)
	 */
	@Override
	public NodeDescriptor addNeighbor(String url) throws NotRunningException,
			MalformedURLException, AlreadyNeighborException, ConnectException {
		NodeDescriptor descriptor = super.addNeighbor(url);
		if (descriptor != null) {
			ungroupedNodes.add(descriptor);
		}
		return descriptor;
	}

	public NodeDescriptor addNeighborForGroup(NodeDescriptor descriptor, 
			GroupDescriptor groupDescriptor) throws NotRunningException {
		
		if (descriptor.equals(getNodeDescriptor())) {
			throw new RuntimeException("Should NOT connect to itself.");
		}
		
		NodeDescriptor node = null;
		
		if (ungroupedNodes.contains(descriptor)) {
			ungroupedNodes.remove(descriptor);
			node = descriptor;
		} else if (isNeighborOf(descriptor)) {
			node = descriptor;
		} else {
			for (String url: descriptor.getUrls()) {
				try {
					node = addNeighbor(url);
				} catch (AlreadyNeighborException ex) {
					node = descriptor;
				} catch (Exception ex) {
					continue;
				}
				
			}
			if (node != null) {
				ungroupedNodes.remove(descriptor);
			}
		}
		if (node != null) {
			groupsByNode.put(node, groupDescriptor.getUniqueId());
		}
		return node;
	}
	
	public void removeNeighboorForGroup(NodeDescriptor descriptor,
			GroupDescriptor groupDescriptor) {
		groupsByNode.remove(descriptor, groupDescriptor);
		if (!groupsByNode.containsKey(descriptor) ||
				groupsByNode.get(descriptor).size() == 0)
		{
			if (ungroupedNodes.contains(descriptor)) {
				throw new RuntimeException("Node " + descriptor +
						" which was in a group should not be in " +
						" the ungrouped list.");
			}
			super.removeNeighbor(descriptor);
		}
	}
	
	public void removeNodesForGroup(GroupDescriptor groupDescriptor) {
		Set<NodeDescriptor> keys = groupsByNode.keySet();
		for (NodeDescriptor descriptor: keys) {
			removeNeighboorForGroup(descriptor, groupDescriptor);
		}
	}

	/* (non-Javadoc)
	 * @see polimi.reds.broker.overlay.SimpleTopologyManager#removeNeighbor(polimi.reds.NodeDescriptor)
	 */
	@Override
	public void removeNeighbor(NodeDescriptor descriptor) {
		if (ungroupedNodes.contains(descriptor)) {
			ungroupedNodes.remove(descriptor);
		}
		groupsByNode.removeAll(descriptor);
		super.removeNeighbor(descriptor);
	}
}
