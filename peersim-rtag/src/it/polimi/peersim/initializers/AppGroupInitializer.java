package it.polimi.peersim.initializers;

import it.polimi.peersim.protocols.AppGroupProtocol;
import it.polimi.peersim.prtag.GroupDescriptor;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 */

public class AppGroupInitializer implements Control {
	
	
	private static final String APPGROUP_PROTOCOL = "appgroup_protocol";
	private static int appGroupProtocolId;
	
	private static String RED = "Red";
	private static String GREEN = "Green";
	
	
	
	public AppGroupInitializer(String prefix) {
    	appGroupProtocolId = Configuration.getPid(prefix + "." + APPGROUP_PROTOCOL);
    }

	@Override
	public boolean execute() {
		for (int i = 0; i < Network.size(); i++) {
			Node n = Network.get(i);
			
			//initialize each node with an app group
			if (i%2 == 0) {
				AppGroupProtocol appGroupProtocol = (AppGroupProtocol)
		            	n.getProtocol(appGroupProtocolId);
				appGroupProtocol.askLeaderToJoin(RED);
			} else {
				AppGroupProtocol appGroupProtocol = (AppGroupProtocol)
		            	n.getProtocol(appGroupProtocolId);
				appGroupProtocol.askLeaderToJoin(GREEN);
			}
        }
		return false;
	}

	

}
