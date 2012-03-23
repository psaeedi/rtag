package example1;

import A3.A3Middlware;

public class Launch1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		A3Middlware middleware = new A3Middlware();
		
		RedNode node1 = new RedNode(middleware, "red1");
		node1.addSupervisorRole("red", new RedSupervisor(), node1.getName());
		node1.setup("red");
		
		RedNode node2 = new RedNode(middleware, "red2");
		node2.addFollowerRole("red", new RedFollower(), node2.getName());
		node2.setup("red");
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		RedNode node3 = new RedNode(middleware, "red3");
		node3.addSupervisorRole("red", new RedSupervisor(), node3.getName());
		node3.addFollowerRole("red", new RedFollower(), node3.getName());
		node3.setup("red");
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		node1.kill("red");
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		node3.kill("red");
		
		
	}

}
