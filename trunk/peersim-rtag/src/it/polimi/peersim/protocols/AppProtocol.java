package it.polimi.peersim.protocols;

import it.polimi.peersim.protocols.grouping.GroupingProtocol;
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
	
	//private static final String NETWORK_SIZE = "netwrok_size";
	//protected final int networkSize;
	
	private static final String RED = "Red";
	private static final String GREEN = "Green";
	private static final String BLUE = "blue";
	private static final String YELLOW = "yellow";
	private static final String ORANGE = "orange";
	
	
	public AppProtocol(String prefix) {
		groupingProtocolId = Configuration.getPid(
				prefix + "." + GROUPING_PROTOCOL);
		startGroupCycle = Configuration.getInt(
				prefix + "." + START_GROUP_CYCLE, 1);
		//networkSize = Configuration.getInt(
			//	prefix + "." + NETWORK_SIZE, 5);
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
		
		if(CDState.getCycle() == startGroupCycle ){
			String friendlyName = null;
					if(currentNode.getID() % 7 == 0)
						friendlyName = RED ;	
				    else if (currentNode.getID() % 5 == 0)	
				    	friendlyName = GREEN ;
				    else if (currentNode.getID() % 3 == 0)	
				    	friendlyName = BLUE ;
				    else if (currentNode.getID() % 2 == 0)	
				    	friendlyName = YELLOW ;
				    else 
				    	friendlyName = ORANGE ;
			//String friendlyName = (currentNode.getID() % 2 == 0) ? RED : GREEN;	
			GroupingProtocol grouping = (GroupingProtocol)
					currentNode.getProtocol(groupingProtocolId);
			grouping.joinOrCreateGroup(currentNode, friendlyName);
		}
		/*if(CDState.getCycle() >= startGroupCycle && 
				CDState.getCycle() <= (startGroupCycle+(networkSize/5))){
					if(currentNode.getID()<= networkSize/4) {
						String friendlyName = (currentNode.getID() % 2 == 0) ? RED : GREEN;			
						GroupingProtocol grouping = (GroupingProtocol)
								currentNode.getProtocol(groupingProtocolId);
						grouping.joinOrCreateGroup(currentNode, friendlyName);
					    }
					
					else if(currentNode.getID()<= 2*networkSize/4 && currentNode.getID()>networkSize/4) {
						String friendlyName = (currentNode.getID() % 2 == 0) ? RED : GREEN;			
						GroupingProtocol grouping = (GroupingProtocol)
								currentNode.getProtocol(groupingProtocolId);
						grouping.joinOrCreateGroup(currentNode, friendlyName);
					    }
					
					else if(currentNode.getID()<= 3*networkSize/4 && currentNode.getID()>2*networkSize/4  ) {
						String friendlyName = (currentNode.getID() % 2 == 0) ? RED : GREEN;			
						GroupingProtocol grouping = (GroupingProtocol)
								currentNode.getProtocol(groupingProtocolId);
						grouping.joinOrCreateGroup(currentNode, friendlyName);
					    }
					else if(currentNode.getID()<= networkSize && currentNode.getID()>3*networkSize/4) {
						String friendlyName = (currentNode.getID() % 2 == 0) ? RED : GREEN;			
						GroupingProtocol grouping = (GroupingProtocol)
								currentNode.getProtocol(groupingProtocolId);
						grouping.joinOrCreateGroup(currentNode, friendlyName);
					    }
				
			
			}*/
		}

	

}
