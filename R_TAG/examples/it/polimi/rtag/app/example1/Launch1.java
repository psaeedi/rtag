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
	 * @throws InterruptedException 
	 * 
	 */
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub

		
		Node nodemaster = new Node("localhost", 40000);
		RedMaster master = new RedMaster();
		master.setCurrentNode(nodemaster);
		nodemaster.start();
		Node nodeslave = new Node("localhost", 20000);
		RedSlave slave = new RedSlave();
		slave.setCurrentNode(nodeslave);
		nodeslave.start();
		try {
			nodemaster.getOverlay().addNeighbor("reds-tcp:localhost:" + 20000);
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
		
		nodemaster.joinGroup("purple");
		//invoke command (CallableApp)
		master.invokeCommand(nodeslave.getNodeDescriptor(), master.joinApp.getName(), "purple");
		master.start();
		Thread.sleep(10000);
		master.stop();
	}

}
