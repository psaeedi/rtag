package it.polimi.peersim.messages;

import java.util.logging.Logger;

import peersim.core.Control;

public class MessageCounting implements Control {
	
	Logger logger = Logger.getLogger("com.whatever");
 

	    public static long universeCurrent = 0;
	    

	    public static long universeCount = 0;
	    

	    public static long universeTotal = 0;
	    

	    public MessageCounting(String name) {
	        clearStats();
	    }

	    public static void universeMessages(int count) {
	    	universeCount += count;
	    	universeTotal += count;
	    }

	   

	   /* public static long mycoCount() {
	        return gossipCurrent + topoQueryCurrent + topoActionCurrent;
	    }

	   
	    public static long mycoTotal() {
	        return gossipTotal + topoQueryTotal + topoActionTotal;
	    }*/

	   
	    private static void clearStats() {
	        universeCurrent = universeCount;
	       

	        universeCount = 0;
	        
	    }

	    private static Logger log =
	        Logger.getLogger(MessageCounting.class.getName());
	    

	    public boolean execute() {
	        log.info("--------------Message counts (total) - Myco: " + universeCount() + ")");

	        clearStats();
	        return false;
	    }

		private long universeCount() {
			return universeCurrent;
		}

	}

