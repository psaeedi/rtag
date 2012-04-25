/**
 * 
 */
package it.polimi.peersim.protocols.grouping;

import java.io.Serializable;

import peersim.core.Node;

/**
 * @author Panteha Saeedi
 *
 * Notifies the network that a certain node is the leader of
 * a group belonging to a hierachy with a certain name.
 * 
 * Beacons are broadcasted through the network and are
 * only valid for a certain number of cycles/time.
 */
public class GroupBeacon implements Serializable {

	private static final long serialVersionUID = -5286735135287312981L;

	private final String groupName;
	private final Node leader;
	private final int expireCycle;
	
	public GroupBeacon(String name, Node leader, int expireCycle) {
		super();
		this.groupName = name;
		this.leader = leader;
		this.expireCycle = expireCycle;
	}

	public String getGroupName() {
		return groupName;
	}

	public Node getLeader() {
		return leader;
	}

	public int getExpireCycle() {
		return expireCycle;
	}
}
