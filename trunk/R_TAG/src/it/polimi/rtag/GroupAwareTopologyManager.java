/**
 * 
 */
package it.polimi.rtag;

import java.beans.PropertyChangeEvent;

import polimi.reds.broker.overlay.SimpleTopologyManager;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class GroupAwareTopologyManager extends SimpleTopologyManager 
		implements GroupChangeListener {

	/**
	 * 
	 */
	public GroupAwareTopologyManager() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * NOTE: this event will be only received if 
	 * oldGroupDescript.equals(NewGroupDescript) == false
	 * therefore the GroupDescriptequals method implies
	 * that {@link GroupDescriptor#equals(Object)} will return
	 * <code>true</code> if the descriptor has been updated.
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		GroupDescriptor oldGroupDescriptor = (GroupDescriptor) evt.getOldValue();
		GroupDescriptor newGroupDescriptor = (GroupDescriptor) evt.getNewValue();
		// TODO do something
		if (oldGroupDescriptor == null) {
			// A new group has been created/added
			// TODO establish a connection with leader/parent/followers
			return;
		} else if (newGroupDescriptor == null) {
			// A group has been removed/dismantled
			// TODO remove all the unnecessary connections
		} else {
			// A group has been updated
			// TODO compare the two descriptors and update the connections
		}
		
	}

}
