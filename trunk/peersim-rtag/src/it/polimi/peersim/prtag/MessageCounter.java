/**
 * 
 */
package it.polimi.peersim.prtag;

import it.polimi.peersim.messages.UniverseMessage;

import java.util.ArrayList;
import java.util.HashMap;

import peersim.cdsim.CDState;

/**
 * @author pani
 *
 */
public class MessageCounter {
	
	private static MessageCounter singleton = null;
	
	private HashMap<Integer, HashMap<String, Integer>> messageByCycle = new  HashMap<Integer, HashMap<String, Integer>>();
	private HashMap<String, Integer> currentCycleCount = null; 
	private int currentCycle = -1;
	
	public static MessageCounter createInstance() {
		if (singleton == null) {
			singleton = new MessageCounter();
		}
		return singleton;
	}
	
	private MessageCounter() {}

	public void count(UniverseMessage message) {
		int cycle = CDState.getCycle();
		if (cycle > currentCycle) {
			nextCycle();
			currentCycle = cycle;
		}
		
		if (message == null) {
			return;
		}
		
		String head = message.getHead();
		if (currentCycleCount.containsKey(head)) {
			int count = currentCycleCount.get(head);
			count ++;
			currentCycleCount.put(head, count);
		} else {
			currentCycleCount.put(head, 1);
		}
	}
	
	private void nextCycle() {
		if (currentCycle > -1) {
			messageByCycle.put(currentCycle, currentCycleCount);
		}
		currentCycleCount = new HashMap<String, Integer>();
	}
	
	public void printAll() {
		System.out.println("Counting messages-------------------- ");
		System.out.println("Cmessagecycle size"+messageByCycle.size());
		for (int cycle: messageByCycle.keySet()) {
			System.out.println("Cycle " + cycle);
			HashMap<String, Integer> map = messageByCycle.get(cycle);
			for (String key: map.keySet()) {
				System.out.println(key + ": " + map.get(key));
			}
		}
		System.out.println("-------------------------------------------- ");
	}
}
