/**
 * 
 */
package it.polimi.peersim.controls;

import it.polimi.peersim.prtag.UniverseMessageCounter;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.Control;

/**
 * @author Panteha Saeedi
 *
 */
public class MessageCountingControl implements Control {
	
	private static final String LAST_CYCLE = "last_cycle";
	protected final int lastCycle;

	UniverseMessageCounter messageCounter = UniverseMessageCounter.createInstance();
	
	/**
	 * 
	 */
	public MessageCountingControl(String prefix) {
		lastCycle = Configuration.getInt(
				prefix + "." + LAST_CYCLE, 10);
	}

	/* (non-Javadoc)
	 * @see peersim.core.Control#execute()
	 */
	@Override
	public boolean execute() {
		messageCounter.nextCycle();
		if(CDState.getCycle()==lastCycle){
    	messageCounter.printAll();}
    	return false;
	}

}
