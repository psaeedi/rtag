package it.polimi.rtag.app.example1;

import it.polimi.rtag.Node;

import java.net.ConnectException;
import java.net.MalformedURLException;

import polimi.reds.broker.overlay.AlreadyNeighborException;
import polimi.reds.broker.overlay.NotRunningException;

/**
 * @author Panteha saeedi@ elet.polimi.it
 *
 */

public class Launch1 {

	/**
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		Node node1 = new Node("localhost", 40000);
		RedMaster master = new RedMaster();
		master.setCurrentNode(node1);
		node1.start();
		Node node2 = new Node("localhost", 20000);
		RedSlave slave = new RedSlave();
		slave.setCurrentNode(node2);
		node2.start();
		try {
			node1.getOverlay().addNeighbor("reds-tcp:localhost:" + 20000);
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
		
		node1.joinGroup("purple");
		//invoke command (CallableApp)
		master.invokeCommand(node2.getNodeDescriptor(), master.joinApp.getName(), "purple");
		master.start();
	}

}
