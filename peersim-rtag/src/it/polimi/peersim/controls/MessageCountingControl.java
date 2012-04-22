/**
 * 
 */
package it.polimi.peersim.controls;

import it.polimi.peersim.prtag.MessageCounter;
import peersim.core.Control;

/**
 * @author rax
 *
 */
public class MessageCountingControl implements Control {

	MessageCounter messageCounter = MessageCounter.createInstance();
	
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
		messageCounter.count(null);
    	messageCounter.printAll();
    	return false;
	}

}
