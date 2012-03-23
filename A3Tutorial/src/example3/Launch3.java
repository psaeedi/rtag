package example3;

import A3.A3Middlware;

public class Launch3 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		A3Middlware middleware = new A3Middlware();
		
		MixedNode node1 = new MixedNode(middleware, "rbg");
		node1.addSupervisorRole("red", new RedSupervisor(), node1.getName());
		node1.addSupervisorRole("blue", new BlueSupervisor(), node1.getName());
		node1.addSupervisorRole("green", new GreenSupervisor(), node1.getName());
		node1.setup("red");
		node1.setup("blue");
		node1.setup("green");
		
		MixedNode node2 = new MixedNode(middleware, "red1");
		node2.addFollowerRole("red", new RedFollower(), node2.getName());
		node2.setup("red");
		
		MixedNode node3 = new MixedNode(middleware, "blue1");
		node3.addFollowerRole("blue", new BlueFollower(), node3.getName());
		node3.setup("blue");
		
		MixedNode node4 = new MixedNode(middleware, "green1");
		node4.addFollowerRole("green", new GreenFollower(), node4.getName());
		node4.setup("green");
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		MixedNode node5 = new MixedNode(middleware, "red2");
		node5.addSupervisorRole("red", new RedSupervisor(), node5.getName());
		node5.addFollowerRole("red", new RedFollower(), node5.getName());
		node5.setup("red");
		
		MixedNode node6 = new MixedNode(middleware, "blue2");
		node6.addSupervisorRole("blue", new BlueSupervisor(), node6.getName());
		node6.setup("blue");
		
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		node1.kill("red");
		
		MixedNode node7 = new MixedNode(middleware, "blue&red");
		node7.addSupervisorRole("blue", new BlueSupervisor(), node7.getName());
		node7.addFollowerRole("blue", new BlueFollower(), node7.getName());
		node7.addSupervisorRole("red", new RedSupervisor(), node7.getName());
		node7.addFollowerRole("red", new RedFollower(), node7.getName());
		node7.setup("blue");
		node7.setup("red");
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		node1.kill("blue");
		node5.kill("red");
		
		MixedNode node8 = new MixedNode(middleware, "green");
		node8.addFollowerRole("green", new GreenFollower(), node8.getName());
		node8.setup("green");
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("\n*************killing green");
		node1.kill("green");
	}

}
