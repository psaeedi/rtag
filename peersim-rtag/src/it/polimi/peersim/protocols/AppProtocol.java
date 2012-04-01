package it.polimi.peersim.protocols;

import peersim.cdsim.CDState;
import peersim.cdsim.DaemonProtocol;
import peersim.config.Configuration;
import peersim.core.Node;

public class AppProtocol extends DaemonProtocol{
	
	private static final String GROUPING_PROTOCOL = "grouping_protocol";
	private static int groupProtocolId;
	private String friendlyName;
	
	private static String RED = "Red";
	private static String GREEN = "Green";
	
	public AppProtocol(String prefix) {
		super(prefix);

		groupProtocolId = Configuration.getPid
				(prefix + "." + GROUPING_PROTOCOL);
    }
	
	public void nextCycle(Node n, int protocolID ) {
		// TODO commented to check the rest
		/*
		if (CDState.getCycle() == n.getID()){
		 if (n.getID()%2 == 0) {
				friendlyName = RED;
			} else {
				friendlyName = GREEN;
			}
		 
		 GroupingProtocol appGroupProtocol = (GroupingProtocol)
	         		n.getProtocol(groupProtocolId);
	 		appGroupProtocol.joinOrCreateGroup(friendlyName);
		}
        */
	}

}
