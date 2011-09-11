package it.polimi.rtag;
import it.polimi.rtag.filters.*;
import it.polimi.rtag.messaging.*;

public class HelloWorld implements Runnable {

	
    private LeaderDescriptor node;

	public HelloWorld(LeaderDescriptor leader) {
    	this.node = leader;
	}

	public HelloWorld(FollowerDescriptor follower) {
		// TODO Auto-generated constructor stub
	}

	/**
     *
     */
    public static void main(String[] args) {
    	// TODO Auto-generated method stub
        
    	LeaderDescriptor leader = new LeaderDescriptor(null);
    	FollowerDescriptor follower = new FollowerDescriptor(null);
    	
        HelloWorld nodeA = new HelloWorld(leader);
        HelloWorld nodeB = new HelloWorld(follower);
       
        new Thread(nodeA).start(); 
        new Thread(nodeB).start();
        
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
           
       
    }

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
     