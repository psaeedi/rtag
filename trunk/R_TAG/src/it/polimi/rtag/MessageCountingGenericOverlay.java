/**
 * 
 */
package it.polimi.rtag;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

import polimi.reds.NodeDescriptor;
import polimi.reds.broker.overlay.GenericOverlay;
import polimi.reds.broker.overlay.Link;
import polimi.reds.broker.overlay.NotConnectedException;
import polimi.reds.broker.overlay.NotRunningException;
import polimi.reds.broker.overlay.TopologyManager;
import polimi.reds.broker.overlay.Transport;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class MessageCountingGenericOverlay extends GenericOverlay {

	private HashMap<String, Integer> sentMessages = new HashMap<String, Integer>();
	private HashMap<String, Integer> receivedMessages = new HashMap<String, Integer>();
	
	/**
	 * @param tm
	 * @param tr
	 */
	public MessageCountingGenericOverlay(TopologyManager tm, Transport tr) {
		super(tm, tr);
	}

	private void incrementMessage(HashMap<String, Integer> map, String subject) {
		synchronized (map) {
			int value = 0;
			if (map.containsKey(subject)) {
				value = map.get(subject);
			}
			value ++;
			map.put(subject, value);
		}
	}

	/**
	 * @return the sentMessages
	 */
	public HashMap<String, Integer> getSentMessages() {
		return sentMessages;
	}

	/**
	 * @return the receivedMessages
	 */
	public HashMap<String, Integer> getReceivedMessages() {
		return receivedMessages;
	}

	/* (non-Javadoc)
	 * @see polimi.reds.broker.overlay.GenericOverlay#notifyDataArrived(java.lang.String, polimi.reds.broker.overlay.Link, java.io.Serializable)
	 */
	@Override
	public void notifyDataArrived(String subject, Link source, Serializable data) {
		incrementMessage(receivedMessages, subject);
		super.notifyDataArrived(subject, source, data);
	}

	/* (non-Javadoc)
	 * @see polimi.reds.broker.overlay.GenericOverlay#send(java.lang.String, java.io.Serializable, polimi.reds.NodeDescriptor)
	 */
	@Override
	public void send(String subject, Serializable packet,
			NodeDescriptor recipient) throws IOException,
			NotConnectedException, NotRunningException {
		incrementMessage(sentMessages, subject);
		super.send(subject, packet, recipient);
	}
	
	public void printToStream(PrintStream pw) {
		// printing messages
		System.out.println(getNodeDescriptor().getID());
		System.out.println("----------sent messages---------");
		for (Entry<String, Integer> entry: sentMessages.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
		System.out.println("----------received messages---------");
		for (Entry<String, Integer> entry: receivedMessages.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
		System.out.println("-------------------");
		System.out.println("");
	}
	
}
