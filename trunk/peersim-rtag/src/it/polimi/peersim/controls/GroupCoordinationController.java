package it.polimi.peersim.controls;


import it.polimi.peersim.protocols.UniverseProtocol;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;


// TODO this protocol should be built on top of 
// DiscoveryProtocol and of UniverseProtocol by coordinating them.
//
// At each execution this should ask the discovery protocol
// for new and removed nodes then ask the UniverseProtocol to handle them
public class GroupCoordinationController implements Control {
	
	
	private static final String UNIVERSE_PROTOCOL = 
			"universe_protocol";
	private static int universeProtocolId;
	
	
	
	//Neighbor Distance is initialized to 1

	public GroupCoordinationController(String prefix) {
		super();
		universeProtocolId = Configuration.getPid(
				prefix + "." + UNIVERSE_PROTOCOL);
		
       }
	

	@Override
	public boolean execute() {
		Node n;
        for (int i = 0; i < Network.size(); i++) {
            n = Network.get(i);
            UniverseProtocol universeProtocol = (UniverseProtocol)
            		n.getProtocol(universeProtocolId);
            
            
            // TODO invoke universe protocol according to what is happening
            /*
            universeProtocol.shouldRequestToJoin();
            universeProtocol.shouldAcceptJoinRequest();
            universeProtocol.shouldSplitTo();
            universeProtocol.shouldAcceptToCreateAChild();
            universeProtocol.shouldAcceptToCreateAChild();
            universeProtocol.followerToSplit();
            universeProtocol.electNewLeader();
            */
        }
        return false;
    }

}
