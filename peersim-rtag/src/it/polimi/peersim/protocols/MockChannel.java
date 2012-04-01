/**
 * 
 */
package it.polimi.peersim.protocols;

import java.io.Serializable;

import it.polimi.peersim.messages.BaseMessage;
import it.polimi.peersim.messages.MockMessage;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.transport.Transport;

/**
 * @author Panteha Saeedi
 * 
 * Simulate a 1 to 1 transport protocol such as udp, tcp, zigbee or Bluetooth.
 * This can be replaced with any transport protocol.
 * 
 * This should be the only protocol communicating to a remote node.
 * This represents the lower layer in the protocol stack.
 */
public class MockChannel extends ForwardingProtocol<MockMessage> implements Transport {

	private static final String LATENCY = "latency";
	private static int latency;
	
	public MockChannel(String prefix) {
		super(prefix);
		latency = Configuration.getInt(
				prefix + "." + LATENCY, 0);
	}
	
	/* (non-Javadoc)
	 * @see peersim.transport.Transport#getLatency(peersim.core.Node, peersim.core.Node)
	 */
	@Override
	public long getLatency(Node source, Node dest) {
		return latency;
	}

	/* (non-Javadoc)
	 * @see peersim.transport.Transport#send(peersim.core.Node, peersim.core.Node, java.lang.Object, int)
	 */
	@Override
	public void send(Node currentNode, Node recipient, Object message, int pid) {
		if (protocolId != pid) {
			throw new AssertionError(
					"A MockChannel can only send messages to " +
					"another MockChannel on a remote node.");
		}
		MockChannel remoteProtocol = (MockChannel) recipient.getProtocol(protocolId);
		remoteProtocol.receiveAndPushUpMessage(recipient, currentNode, (MockMessage)message);
	}

	@Override
	public MockMessage handlePushDownMessage(Node currentNode, Node recipient,
			Serializable content) {
		MockMessage message = new MockMessage(protocolId, currentNode, recipient, content);
		send(currentNode, recipient, message, protocolId);
		// This never pushes down
		return null;
	}

	@Override
	public BaseMessage handlePushUpMessage(Node currentNode, Node sender,
			MockMessage message) {
		return (BaseMessage) message.getContent();
	}

}
