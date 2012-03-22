/**
 * 
 */
package it.polimi.rtag.app;

import it.polimi.rtag.messaging.TupleMessage;
import polimi.reds.NodeDescriptor;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 * user define application should extend this class
 * just define what application should do
 */
public abstract class AbstractApp {

	public abstract void handleMessageReceived(NodeDescriptor sender, TupleMessage message);
}
