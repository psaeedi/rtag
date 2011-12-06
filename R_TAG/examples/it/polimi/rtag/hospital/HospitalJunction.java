package it.polimi.rtag.hospital;

import java.net.ConnectException;
import java.net.MalformedURLException;

import polimi.reds.broker.overlay.AlreadyNeighborException;
import polimi.reds.broker.overlay.NotRunningException;
import it.polimi.rtag.Node;


public class HospitalJunction {
	
	public enum IntelligentJunction {
		ONE,TWO,THREE,FOUR,FIVE 
    }
	
	private Node node;
	
	/* Junction one is the main entrance, however, there are
	 * other auxiliary junctions
	 */
	private IntelligentJunction currentJunction = IntelligentJunction.ONE;
	
	
	
	public HospitalJunction(String host, int port, IntelligentJunction currentJunction) {
	    node = new Node(host, port);
		node.start();
		setCurrentJunction(currentJunction);
	}

	public IntelligentJunction getCurrentJunction() {
		return currentJunction;
	}

	public void setCurrentJunction(IntelligentJunction currentJunction) {
		
		this.currentJunction = currentJunction;
	}
	
	protected void joinGroup(IntelligentJunction currentJunction){
		node.joinGroup(currentJunction.toString());			
	}
	
	protected void leaveGroup(IntelligentJunction currentJunction){
		node.leaveGroup(currentJunction.toString());	
	}

	public void connectNeighbor(String urls) {
		try {
			node.addNeighbor(urls);
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
		
	}

	public void tearDown() {
		// TODO Auto-generated method stub
		
	}
	
	
}

		