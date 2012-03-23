package A3;

public abstract class SupervisorRole implements Runnable{
	
	private GroupDescriptor descriptor;
	protected boolean active;
	private int resourceCost;
	protected A3Middlware middleware;
	private String nodeID;
	
	public A3Middlware getMiddleware() {
		return middleware;
	}

	public void setMiddleware(A3Middlware middleware) {
		this.middleware = middleware;
	}

	// This method will be called when their is an update in the nodes in the group
	public abstract void receiveGroupUpdate(A3UpdateMessage msg);
	
	//This method will be called when a follower sends a message
	public abstract void receiveFollowerMessage(A3Message msg);

	public void setDescriptor(GroupDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	public GroupDescriptor getDescriptor() {
		return descriptor;
	}
	
	public void activate() {
		active = true;
	}
	
	public void deactivate() {
		active=false;
	}

	public int getResourceCost() {
		return resourceCost;
	}

	public void setResourceCost(int resourceCost) {
		this.resourceCost = resourceCost;
	}

	public String getNodeID() {
		return nodeID;
	}

	public void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}
 
	
	
	
}
