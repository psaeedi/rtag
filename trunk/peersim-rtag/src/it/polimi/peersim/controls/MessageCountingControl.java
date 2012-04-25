/**
 * 
 */
package it.polimi.peersim.controls;

import it.polimi.peersim.prtag.UniverseMessageCounter;
import peersim.core.Control;

/**
 * @author Panteha Saeedi
 *
 */
public class MessageCountingControl implements Control {

	UniverseMessageCounter messageCounter = UniverseMessageCounter.createInstance();
	
	/**
	 * 
	 */
	public MessageCountingControl(String prefix) {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see peersim.core.Control#execute()
	 */
	@Override
	public boolean execute() {
		messageCounter.nextCycle();
    	messageCounter.printAll();
    	return false;
	}

}
