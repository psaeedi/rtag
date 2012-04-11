package it.polimi.peersim.controls;


import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.DynamicNetwork;

public class CrashControl extends DynamicNetwork  {
	
	private static final String CRASH_CYCLE = "crash_cycle";
	protected final int crashCycle;

	@Override
	protected void add(int n) {
	    if(CDState.getCycle() == crashCycle){
			for (int i = 0; i < n; ++i) {
	            Node newnode = (Node) Network.prototype.clone();
	            for (int j = 0; j < inits.length; ++j) {
	                    inits[j].initialize(newnode);
	            }
	            Network.add(newnode);
	         }
			return;
		}
	    return;
	
	}

	@Override
	protected void remove(int n) {
		if(CDState.getCycle() == crashCycle){
			System.out.println("---------------------------------------------------------------------------------------" +
					"-crashhhhhhhhhhhhhh"+CDState.getCycleT()); 
			for (int i = 0; i < n; ++i) {
	            Network.remove(CommonState.r.nextInt(Network.size()));
			}
			
			}
		return;
		
	}

	public CrashControl(String prefix) {
		
		super(prefix);
		crashCycle = Configuration.getInt(
				prefix + "." + CRASH_CYCLE,5);
	}


	
}
