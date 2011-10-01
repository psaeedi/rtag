/**
 * 
 */
package it.polimi.rtag;

import java.beans.PropertyChangeListener;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it).
 * 
 * Every time a {@link GroupDescriptor} changes the
 * {@link GroupCommunicationManager} notifies its listeners
 * that the group topology has changed.
 */
public interface GroupChangeListener extends PropertyChangeListener {

}
