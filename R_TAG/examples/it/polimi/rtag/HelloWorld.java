package it.polimi.rtag;


public class HelloWorld implements Runnable {

	private Node node;
	
    public HelloWorld(Node node) {
    	this.node = node;
	}

	/**
     *
     */
    public static void main(String[] args) {
    	// TODO Auto-generated method stub
        
    	LeaderNode leader = new LeaderNode();
    	FollowerNode follower = new FollowerNode();
    	
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
     