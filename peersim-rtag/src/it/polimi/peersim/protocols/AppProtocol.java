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
	
	private static final String START_GROUP_CYCLE = "start_group_cycle";
	protected final int startGroupCycle;
	
	private static final String RED = "Red";
	private static final String GREEN = "Green";
	
	public AppProtocol(String prefix) {
		groupingProtocolId = Configuration.getPid(
				prefix + "." + GROUPING_PROTOCOL);
		startGroupCycle = Configuration.getInt(
				prefix + "." + START_GROUP_CYCLE, 1);
    }
	
	@Override
	public Object clone() {
		AppProtocol clone = null;
        try {
        	clone = (AppProtocol) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens
        return clone;
	}
	
	public void nextCycle(Node currentNode, int protocolID ) {
		if(CDState.getCycle() == startGroupCycle) {
			String friendlyName = (currentNode.getID() % 2 == 0) ? RED : GREEN;			
			GroupingProtocol grouping = (GroupingProtocol)
					currentNode.getProtocol(groupingProtocolId);
			grouping.joinOrCreateGroup(currentNode, friendlyName);
		}
		
		
	}

	

}
