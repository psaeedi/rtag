/**
 * 
 */
package it.polimi.peersim.protocols;

import it.polimi.peersim.prtag.LocalUniverseDescriptor;

import java.util.HashMap;

import peersim.cdsim.DaemonProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;

/**
 * @author pani
 *
 * Defines the geoloaction of each node.
 */
public class CapacityGenerator extends DaemonProtocol {
	
	 private static final String PAR_ALPHA = "alpha";
     private static final String PAR_MAX = "max";

	 private static double alpha;
	 private static double max;
	 private static int intmax;
	 private static cern.jet.random.engine.RandomEngine generator;
	 
	 private HashMap<Node, Integer> capacityList = 
				new HashMap<Node, Integer>();
	
	 public CapacityGenerator(String prefix) {
		 super(prefix);
		 //this.name = name;
		 alpha = Configuration.getDouble(
					prefix + "." + PAR_ALPHA, 2);
		 max = Configuration.getDouble(
					prefix + "." + PAR_MAX, 60);
	    }
	
	public Object clone() {
		CapacityGenerator clone = (CapacityGenerator) super.clone();
       
        return clone;
    }
	
	 public static double nextPowLaw(double alpha, double max) {
		 generator =
			        new cern.jet.random.engine.MersenneTwister(CommonState.r.nextInt(Integer.MAX_VALUE));
	        return cern.jet.random.Distributions.nextPowLaw(alpha,max,generator);
	 }

	 public int nextPowInt(Node currentNode) {
		    //intmax = (new Double(max).intValue());
	        //return intmax - (new Double(nextPowLaw(alpha,max))).intValue();
		    double pop = nextPowLaw(alpha,max);
		   capacityList.put(currentNode, (int) pop);
			return (int) pop;
	  }
	 
	 public int getCapacity(Node currentNode){
		 int capacity = capacityList.get(currentNode);
		 return capacity;
		 
	 }


	@Override
	public void nextCycle(Node node, int protocolID) {
		// TODO Move the nodes around
		super.nextCycle(node, protocolID);
	}

}
