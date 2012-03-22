package it.polimi.rtag.app.pizzaDelivery;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.HashMap;

import polimi.reds.broker.overlay.AlreadyNeighborException;
import polimi.reds.broker.overlay.NotRunningException;
import it.polimi.rtag.Node;
import it.polimi.rtag.app.CallableApp;
import it.polimi.rtag.app.CallableInvocationMessage;
import it.polimi.rtag.messaging.TupleMessage;

public class PizzaDeliveryExample {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Node nodePizzeria = new Node("localhost", 40000);
		CallableApp appPizzeria = new CallableApp();
		appPizzeria.setCurrentNode(nodePizzeria);
		nodePizzeria.start();
		Node nodeCaller = new Node("localhost", 20000);
		CallableApp appCaller = new CallableApp();
		appCaller.setCurrentNode(nodeCaller);
		nodeCaller.start();
		try {
			nodePizzeria.getOverlay().addNeighbor("reds-tcp:localhost:" + 20000);
		} catch (AlreadyNeighborException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConnectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotRunningException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		OrderPizza command = new OrderPizza("OrderPizza", "OrderPizzaResponse");
		appPizzeria.put(command.getName(), command);
		appPizzeria.addCallListener(command.getName(), new PizzeriaListener());
		appCaller.put(command.getName(), command);
		appCaller.addResponseListener(command.getResponseName(), new CustomerListener());
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		HashMap<String, Serializable> order = new HashMap<String, Serializable>();
		order.put(OrderPizza.MARGARITA, 4);
		
		TupleMessage message = new CallableInvocationMessage(nodePizzeria.getNodeDescriptor(),
				order, command);
		nodeCaller.getTupleSpaceManager().storeAndSend(message);
	}

}


class PizzeriaListener implements PropertyChangeListener {

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		System.out.println("******* Pizza dice? Subito da lei.");
	}
}

class CustomerListener implements PropertyChangeListener {

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		System.out.println("******* Tra un po' si mangia");
	}
}