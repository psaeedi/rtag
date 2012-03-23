package it.polimi.peersim.protocols;

import peersim.cdsim.CDProtocol;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.Node;

public class CheckCycleProtocol implements CDProtocol {
	
	private static final String UNIVERSE_PROTOCOL = "universe_protocol";
	private static int universeProtocolId;

	private static final String START_CYCLE = "start_cycle";
	private static int startCycle;
	
	

	
	
	
	 public CheckCycleProtocol(String prefix) {
		 
		 universeProtocolId = Configuration.getPid(
					prefix + "." + UNIVERSE_PROTOCOL);
		 
		 startCycle = Configuration.getInt(
				 prefix + "." + START_CYCLE);
		
	 }
	 
	 public Object clone() {
		 CheckCycleProtocol inp = null;
	        try {
	        	inp = (CheckCycleProtocol) super.clone();
	        	
	        } catch (CloneNotSupportedException e) {
	        } // never happens
	        return inp;
		}
	 
	 @Override
		public void nextCycle(Node node, int pid) {
		 
		// if (CDState.getCycle() < startCycle ){
			return;
	 }
		

}
