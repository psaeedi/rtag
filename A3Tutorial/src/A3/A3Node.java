package A3;

import java.util.HashMap;
import java.util.Map;

public abstract class A3Node {
	
	private int resourceThreshold;
	private String name;
	protected A3Middlware middleware;
	
	private Map<String,SupervisorRole> supervisorRoles = new HashMap<String, SupervisorRole>(); 
	private Map<String,FollowerRole> followerRoles = new HashMap<String, FollowerRole>();
	
	public void setResourceThreshold(int resourceThreshold) {
		this.resourceThreshold = resourceThreshold;
	}

	public int getResourceThreshold() {
		return resourceThreshold;
	}
	
	public void addSupervisorRole(String groupName, SupervisorRole role, String nodeID) {
		role.setMiddleware(middleware);
		supervisorRoles.put(groupName, role);
		role.setNodeID(nodeID);
	}
	
	public SupervisorRole getSupervisorRole(String groupName) {
		return supervisorRoles.get(groupName);
	}
	
	public FollowerRole getFollowerRole(String groupName) {
		return followerRoles.get(groupName);
	}
	
	public void addFollowerRole(String groupName, FollowerRole role, String nodeID) {
		role.setMiddleware(middleware);
		followerRoles.put(groupName, role);
		role.setNodeID(nodeID);
	}
	
	public A3Node(A3Middlware middleware, String name) {
		super();
		this.middleware = middleware;
		this.name = name;
	}

	public abstract void setup(String colour);
	
	public abstract void kill(String colour);
	
	public String getName() {
		return name;
	}




}
