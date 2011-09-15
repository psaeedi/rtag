/**
 * 
 */
package it.polimi.rtag.messaging;

import it.polimi.rtag.GroupDescriptor;
import polimi.reds.Message;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class GroupFollowerCommand extends Message {

	/**
	 * A node notify its leader that it will leave a certain group. 
	 * TODO add a {@link GroupDescriptor} field to this class.
	 */
	public static final String LEAVING_NOTICE = "LEAVING_NOTICE";
	
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4481850960336362688L;

	/**
	 * 
	 */
	public GroupFollowerCommand() {
		// TODO Auto-generated constructor stub
	}

}
