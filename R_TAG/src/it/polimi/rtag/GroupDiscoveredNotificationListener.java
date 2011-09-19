/**
 * 
 */
package it.polimi.rtag;

import polimi.reds.NodeDescriptor;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public interface GroupDiscoveredNotificationListener {

	public void handleGroupDiscovered(NodeDescriptor sender, GroupDescriptor groupDescriptor);
	
}
