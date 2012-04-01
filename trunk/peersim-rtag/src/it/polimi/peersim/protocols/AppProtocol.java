package it.polimi.peersim.protocols;

import java.io.Serializable;

import it.polimi.peersim.messages.BaseMessage;
import it.polimi.peersim.messages.GroupingMessage;
import peersim.cdsim.CDProtocol;
import peersim.cdsim.CDState;
import peersim.cdsim.DaemonProtocol;
import peersim.config.Configuration;
import peersim.core.Node;

public class AppProtocol implements CDProtocol {
	
	private String friendlyName;
	
	private static String RED = "Red";
	private static String GREEN = "Green";
	
	public AppProtocol(String prefix) {
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
		}
		// TODO join group
	}

	

}
