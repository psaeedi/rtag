package example2;

import A3.A3Middlware;

public class Launch2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		A3Middlware middleware = new A3Middlware();
		
		MixedNode node1 = new MixedNode(middleware, "red1");
		node1.addSupervisorRole("red", new RedSupervisor(), node1.getName());
		node1.setup("red");
		
		MixedNode node2 = new MixedNode(middleware, "red2");
		node2.addFollowerRole("red", new RedFollower(), node2.getName());
		node2.setup("red");
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		MixedNode node3 = new MixedNode(middleware, "red3");
		node3.addSupervisorRole("red", new RedSupervisor(), node3.getName());
		node3.addFollowerRole("red", new RedFollower(), node3.getName());
		node3.setup("red");
		
		MixedNode node4 = new MixedNode(middleware, "blue1");
		node4.addSupervisorRole("blue", new BlueSupervisor(), node4.getName());
		node4.setup("blue");
		
		MixedNode node5 = new MixedNode(middleware, "blue2");
		node5.addFollowerRole("blue", new BlueFollower(), node5.getName());
		node5.setup("blue");
		
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		node1.kill("red");
		
		MixedNode node6 = new MixedNode(middleware, "blue&red");
		node6.addFollowerRole("blue", new BlueFollower(), node6.getName());
		node6.addSupervisorRole("red", new RedSupervisor(), node6.getName());
		node6.addFollowerRole("red", new RedFollower(), node6.getName());
		node6.setup("blue");
		node6.setup("red");
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		node3.kill("red");
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		node6.kill("blue");
	}

}
