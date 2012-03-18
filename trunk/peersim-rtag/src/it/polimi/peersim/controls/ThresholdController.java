package it.polimi.peersim.controls;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import it.polimi.peersim.protocols.*;

public class ThresholdController implements Control {
	private static final String UNIVERSE_PROTOCOL = "universe_protocol";
	private static int universeProtocolId;
	
	
	//Neighbor Distance is initialized to 1

	public ThresholdController(String prefix) {
		super();
		universeProtocolId = Configuration.getPid(
				prefix + "." + UNIVERSE_PROTOCOL);
       }

	@Override
	public boolean execute() {
		for (int i = 0; i < Network.size(); i++) {
            Node n = Network.get(i);
            UniverseProtocol universeProtocol = (UniverseProtocol) n.getProtocol(
            		universeProtocolId);
            if (universeProtocol.isCongested()) {
            	universeProtocol.handleCongestion();
            }
        }
		// TODO check what this return false does
		return false;
	}
		
	

}
