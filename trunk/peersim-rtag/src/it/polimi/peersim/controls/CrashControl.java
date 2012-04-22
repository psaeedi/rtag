package it.polimi.peersim.controls;


import it.polimi.peersim.protocols.DiscoveryListener;
import it.polimi.peersim.protocols.DiscoveryProtocol;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.DynamicNetwork;

public class CrashControl extends DynamicNetwork  {
	
	private static final String CRASH_CYCLE = "crash_cycle";
	protected final int crashCycle;
	
	private static final String SECOND_CRASH_CYCLE = "second_crash_cycle";
	protected final int secondCrashCycle;
	
	private static final String DISCOVERY_PROTOCOL = "discovery_protocol";
	private final int discoveryProtocolId;
	
	private Node currentNode;
	

	@Override
	protected void remove(int n) {
		if(CDState.getCycle() == crashCycle || CDState.getCycle() == 30+crashCycle
				|| CDState.getCycle() == 60+crashCycle){
			System.out.println("--------------------------------------------------" +
					"-------------------------------------" +
					"-crashhhhhhhhhhhhhh" + CDState.getCycle()); 
			for (int i = 0; i < n; ++i) {
	            Network.remove(CommonState.r.nextInt(Network.size()));
			}
			
		}
		
	}
	
	
	public void initialize(Node currentNode) {
		this.currentNode = currentNode;
	}

	public CrashControl(String prefix) {
		
		super(prefix);
		crashCycle = Configuration.getInt(
				prefix + "." + CRASH_CYCLE, 40);
		secondCrashCycle = Configuration.getInt(
				prefix + "." + SECOND_CRASH_CYCLE, 80);
		discoveryProtocolId = Configuration.getPid(
				prefix + "." + DISCOVERY_PROTOCOL);
	}


	
}
