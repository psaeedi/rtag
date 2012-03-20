package it.polimi.peersim.controls;

import it.polimi.peersim.protocols.GroupProtocol;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class GroupController implements  Control {
	
	private static final String GROUP_PROTOCOL = "group_protocol";
	private static int groupProtocolId;
	
	public GroupController(String prefix) {
		super();
		groupProtocolId = Configuration.getPid(
				prefix + "." + GROUP_PROTOCOL);
    }
	
	@Override
	public boolean execute() {
		Node n;
        for (int i = 0; i < Network.size(); i++) {
            n = Network.get(i);
            GroupProtocol groupProtocol = (GroupProtocol)
            		n.getProtocol(groupProtocolId);
            groupProtocol.clear();
            String group = (n.getID() % 2 == 0) ? "RED" : "BLUE";
            groupProtocol.add(group);
           // System.out.println("Node " + n.getID() + " added to " + group );
        }
        return false;
    }

}
