/**
 * 
 */
package it.polimi.rtag.app.pizzaDelivery;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import it.polimi.rtag.app.RemoteCallable;

/**
 * @author pani
 *
 */
public class OrderPizza extends RemoteCallable {

	public final static String MARGARITA = "MARGARITA";
	public final static double MARGARITA_PRICE = 5.30; 
	
	public final static String CAPRICOZA = "CAPRICOZA";
	public final static double CAPRICOZA_PRICE = 7.30; 
	
	/**
	 * @param name
	 * @param responseName
	 */
	public OrderPizza(String name, String responseName) {
		super(name, responseName);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see it.polimi.rtag.app.RemoteCallable#doCompute(java.util.Map)
	 */
	@Override
	public Serializable doCompute(Map<String, Serializable> args) {
		int totalQuantity = 0;
		double totalPrice = 0.0;
		for(String pizzaName: args.keySet()){
			int quantity = (Integer) args.get(pizzaName);
			totalQuantity += quantity; 
			if(MARGARITA.equals(pizzaName)){
				totalPrice += MARGARITA_PRICE * totalQuantity;	
			}
			if(CAPRICOZA.equals(pizzaName)){
				totalPrice += CAPRICOZA_PRICE * totalQuantity;	
			}
		}
		
		HashMap<String, Double> results = new HashMap<String, Double>();
		results.put("Preprationtime", (double) totalQuantity*5 );
		results.put("Totalprice", (double) totalPrice );
		
		return results;
	}

}
