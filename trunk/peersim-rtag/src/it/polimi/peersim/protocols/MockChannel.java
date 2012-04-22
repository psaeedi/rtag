/**
 * 
 */
package it.polimi.peersim.protocols;

import java.util.ArrayList;

import it.polimi.peersim.messages.BaseMessage;
import it.polimi.peersim.messages.MockMessage;
import it.polimi.peersim.prtag.UndeliverableMessageException;
import peersim.cdsim.CDProtocol;
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
public class MockChannel extends ForwardingProtocol<MockMessage> implements Transport, CDProtocol {

	private static final String DISCOVERY_PROTOCOL = "discovery_protocol";
	private final int discoveryProtocolId;
	
	private static final String LATENCY = "latency";
	private static int latency;
	
	// TODO fix the name
	private static final String THROUGHPUT = "throughput";
	private static int throughput;
	
	private ArrayList<MockMessage> channelMessageQueue;
	
	public MockChannel(String prefix) {
		super(prefix);
		channelMessageQueue = new ArrayList<MockMessage>();
		latency = Configuration.getInt(
				prefix + "." + LATENCY, 5);
		throughput = Configuration.getInt(
				prefix + "." + THROUGHPUT, 1);
		discoveryProtocolId = Configuration.getPid(
				prefix + "." + DISCOVERY_PROTOCOL);
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

	private boolean areInCommunicationRange(Node currentNode, Node recipient) {
		DiscoveryProtocol discoveryProtocol = (DiscoveryProtocol) 
				currentNode.getProtocol(discoveryProtocolId);
		return discoveryProtocol.isInCommunicationRange(currentNode, recipient);
	}

	@Override
	public MockMessage handlePushDownMessage(Node currentNode, Node recipient,
			BaseMessage content) throws UndeliverableMessageException {
		// If the recipient is not in communication range
		// raise an exception
		// TODO
		//if (!areInCommunicationRange(currentNode, recipient)) {
		//	throw new UndeliverableMessageException(recipient, content);
		//}
		MockMessage message = new MockMessage(protocolId, currentNode, recipient, content);
		//send(currentNode, recipient, message, protocolId);
		channelMessageQueue.add(message);
		// This never pushes down
		return null;
	}

	private void sendAllMessageInQueue(Node currentNode) {
		ArrayList<MockMessage> messagesToSend = new ArrayList<MockMessage>();
		for (int i = 0; i < throughput; i++) {
			if (channelMessageQueue.size() == 0) {
				break;
			}
			MockMessage message = channelMessageQueue.remove(0);
			messagesToSend.add(message);
		}

		for (MockMessage message: messagesToSend) {
			//System.out.println("+++++++++++++++++++++++++++++++++++++latency"+latency+
				//	"num-que"+NUM_MESSAGE_QUEUE+"_________node:"+currentNode.getID());
			//if(latency> NUM_MESSAGE_QUEUE){
			send(currentNode, message.getReceiver(), message, protocolId);
			//NUM_MESSAGE_QUEUE++;
			//}
		}	
	}
	
	@Override
	public BaseMessage handlePushUpMessage(Node currentNode, Node sender,
			MockMessage message) {
		return (BaseMessage) message.getContent();
	}

	@Override
	public void handleUnreliableRecipientException(
			Node currentNode, 
			UndeliverableMessageException ex) 
					throws UndeliverableMessageException {
		throw new UndeliverableMessageException(
				ex.getRecipient(), ex.getBaseMessage());
	}

	@Override
	protected void handleForwardedUnreliableRecipientException(
			Node currentNode, 
			UndeliverableMessageException ex) {
		throw new AssertionError("This protocol should be at the bottom of the stack.");
	}

	@Override
	public void nextCycle(Node currentNode, int pid) {
		sendAllMessageInQueue(currentNode);
	}
	
	@Override
	public Object clone() {
		MockChannel clone = null;
		clone = (MockChannel) super.clone();
		clone.channelMessageQueue = new ArrayList<MockMessage>(channelMessageQueue);
        return clone;
	}

}
