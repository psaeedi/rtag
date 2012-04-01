/**
 * 
 */
package it.polimi.peersim.messages;

import java.io.Serializable;

import peersim.core.Node;

/**
 * @author Panteha Saeedi
 * 
 *
 */
public class RoutingMessage extends BaseMessage {

	private static final long serialVersionUID = -1847995677493043575L;
	private final Node recipient;
	private final Node proxy;

	/**
	 * @param pid
	 * @param content
	 * @param recipient
	 * @param proxy
	 */
	public RoutingMessage(int pid, Node recipient,
			Node proxy, Serializable content) {
		super(pid, content);
		this.recipient = recipient;
		this.proxy = proxy;
	}

	public Node getRecipient() {
		return recipient;
	}

	public Node getProxy() {
		return proxy;
	}
	
}
