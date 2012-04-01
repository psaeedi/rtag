package it.polimi.peersim.protocols;

import peersim.cdsim.CDProtocol;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.Node;

/**
 * 
 * The protocol stack is:
 * 6 - App
 * 5 - Grouping
 * 4 - Routing
 * 3 - UniverseProtocol
 * 2 - TupleSpaceProtocol
 * 1 - MockChannel
 */
public class AppProtocol implements CDProtocol {
	
	private static final String GROUPING_PROTOCOL = "grouping_protocol";
	private static int groupingProtocolId;
	
	
	private String friendlyName;
	
	private static String RED = "Red";
	private static String GREEN = "Green";
	
	public AppProtocol(String prefix) {
		groupingProtocolId = Configuration.getPid(
				prefix + "." + GROUPING_PROTOCOL);
    }
	
	@Override
	public Object clone() {
		AppProtocol clone = null;
        try {
        	clone = (AppProtocol) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens
        clone.friendlyName = friendlyName;
        return clone;
	}
	
	public void nextCycle(Node currentNode, int protocolID ) {
		if (CDState.getCycle() == currentNode.getID()) {
			if (currentNode.getID()%2 == 0) {
				friendlyName = RED;
			} else {
				friendlyName = GREEN;
			}
			GroupingProtocol grouping = (GroupingProtocol)
					currentNode.getProtocol(groupingProtocolId);
			grouping.joinOrCreateGroup(currentNode, friendlyName);
		}
	}

	

}
