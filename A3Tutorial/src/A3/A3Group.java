package A3;

import java.util.ArrayList;
import java.util.List;

public class A3Group {
	
	private String groupName;
	private A3Node supervisor;
	private List<A3Node> followers = new ArrayList<A3Node>();
	
	public A3Group(String groupName, A3Node supervisor) {
		this.groupName = groupName;
		this.supervisor = supervisor;
	}
	
	public void setSupervisor(A3Node supervisor) {
		this.supervisor = supervisor;
	}
	
	public A3Node getSupervisor() {
		return supervisor;
	}
	
	public List<A3Node> getFollowers(){
		return followers;
	}
	
	public void addFollower(A3Node node) {
		followers.add(node);
	}
	
	public void removeFollower(A3Node node) {
		followers.remove(node);
	}
	
	public void sendMsgToFollowers(A3Message msg) {
		for (A3Node node : followers) {
			node.getFollowerRole(groupName).receiveSupervisorMessage(msg);
		}
	}
	
	public void sendMsgToSupervisor(A3Message msg){
		supervisor.getSupervisorRole(groupName).receiveFollowerMessage(msg);
	}

}
