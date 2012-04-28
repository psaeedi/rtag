/**
 * 
 */
package it.polimi.peersim.prtag;

import it.polimi.peersim.messages.UniverseMessage;
import it.polimi.peersim.protocols.UniverseCommand;
import it.polimi.peersim.protocols.grouping.GroupingMessage;

import java.util.HashMap;

import peersim.cdsim.CDState;

/**
 * @author pani
 *
 */
public class UniverseMessageCounter {
	
	private static UniverseMessageCounter singleton = null;
	
	private HashMap<Integer, HashMap<String, Integer>> messageByCycle = 
			new  HashMap<Integer, HashMap<String, Integer>>();
	private HashMap<String, Integer> currentCycleCount = null; 
	private int currentCycle = -1;
	
	public static UniverseMessageCounter createInstance() {
		if (singleton == null) {
			singleton = new UniverseMessageCounter();
		}
		return singleton;
	}
	
	private UniverseMessageCounter() {}

	public void count(UniverseMessage message) {
		nextCycle();
		if (message == null) {
			return;
		}
		
		String head = message.getHead();
		if (UniverseMessage.UNIVERSE_COMMAND.equals(head)) {
			head = head + "-" +((UniverseCommand)message.getContent()).getCommand();
		} else if (UniverseMessage.BROADCAST.equals(head)) {			
			if (message.getContent() instanceof GroupingMessage) {
				head = head + "-" + ((GroupingMessage)message.getContent()).getHead();
			}
		} else if (UniverseMessage.SINGLECAST.equals(head)) {
			// TODO 
		}
		
		if (currentCycleCount.containsKey(head)) {
			int count = currentCycleCount.get(head);
			count ++;
			currentCycleCount.put(head, count);
		} else {
			currentCycleCount.put(head, 1);
		}
	}
	
	public void nextCycle() {
		int cycle = CDState.getCycle();
		if (cycle > currentCycle) {
			if (currentCycle > -1) {
				messageByCycle.put(currentCycle, currentCycleCount);
			}
			currentCycleCount = new HashMap<String, Integer>();
			currentCycle = cycle;
		}	
	}
	
	public void printAll() {
		System.out.println("Counting messages-------------------- ");
		//System.out.println("Cmessagecycle size"+messageByCycle.size());
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
