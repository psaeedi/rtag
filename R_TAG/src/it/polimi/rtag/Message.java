/**
 * 
 */
package it.polimi.rtag;

import it.polimi.rtag.messaging.MessageType;

/**
 * @author panteha
 * 
 * 
 */
public class Message extends polimi.reds.Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7291797665468391009L;

	private MessageType type;
	// TODO fix tuple with lights
	private Tuple content;
}
