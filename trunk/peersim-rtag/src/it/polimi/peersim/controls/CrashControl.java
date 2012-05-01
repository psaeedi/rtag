package it.polimi.peersim.controls;

import it.polimi.peersim.protocols.DiscoveryProtocol;
import it.polimi.peersim.protocols.GeoLocation;
import it.polimi.peersim.protocols.UniverseProtocol;
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
	
	private static final String GEOLOCATION_PROTOCOL = "geolocation_protocol";
	private final int geolocationId;
	
	private static final String UNIVERSE_PROTOCOL = "universe_protocol";
	private final int universeProtocolId;

	@Override
	protected void remove(int n) {

			for (int i = 0; i < n; ++i) {
	            Network.remove(CommonState.r.nextInt(Network.size()));
	            Network.remove(n);
			}
		
	}
	
	
	protected void add(int n) {
		
		if(CDState.getCycle() == crashCycle){
			System.out.println("--------------------------------------------------" + n +
					"-------------------------------------" +"-crashhhhhhhhhhhhhh" + CDState.getCycle()); 
					for (int i = 0; i < n; ++i) {
			            Network.remove(CommonState.r.nextInt(Network.size()));
			            //Network.remove(n);
					}
					
					System.out.println("****size of the netwrok" + Network.size());
					
				}
		
		
		//bring them back after 65 cycles or 12
		else if(CDState.getCycle() == crashCycle + 65){
			System.out.println("****size of the netwrok" + Network.size());
			System.out.println("back in---------------------------back in------------------" + n+
					"-------------------------------------" +
					"-crashhhhhhhhhhhhhh" + CDState.getCycle()); 
			for (int i = 0; i < n; ++i) {
                Node newnode = (Node) Network.prototype.clone();
                for (int j = 0; j < inits.length; ++j) {
                     inits[j].initialize(newnode);
                }
	            Network.add(newnode);
				GeoLocation location;
		        location = (GeoLocation) newnode.getProtocol(geolocationId);
		        location.setX(5*CommonState.r.nextDouble());
		        location.setY(5*CommonState.r.nextDouble());
				
				initializeDiscovery(newnode);
				initializeUniverse(newnode);
				
			}
			
			System.out.println("****size of the netwrok" + Network.size());
	      }
		
		
	
	}
	
	public void initializeDiscovery(Node n) {
		DiscoveryProtocol discoveryProtocol = (DiscoveryProtocol)
        		n.getProtocol(discoveryProtocolId);
		discoveryProtocol.initialize(n); 
	}
	
	public void initializeUniverse(Node n) {
		UniverseProtocol universeProtocol = (UniverseProtocol)
        		n.getProtocol(universeProtocolId);
        // create an empty universe and then send it to setUniverse(node, universe)
        // Each node should be initialized with its own universe
        universeProtocol.initialize(n);
	}
	
	
	public void initialize(Node currentNode) {
	}

	public CrashControl(String prefix) {
		
		super(prefix);
		crashCycle = Configuration.getInt(
				prefix + "." + CRASH_CYCLE, 40);
		secondCrashCycle = Configuration.getInt(
				prefix + "." + SECOND_CRASH_CYCLE, 80);
		discoveryProtocolId = Configuration.getPid(
				prefix + "." + DISCOVERY_PROTOCOL);
		geolocationId = Configuration.getPid(
				prefix + "." + GEOLOCATION_PROTOCOL);
		universeProtocolId = Configuration.getPid(
				prefix + "." + UNIVERSE_PROTOCOL); 
	}


	
}
