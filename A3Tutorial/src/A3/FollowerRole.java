package A3;

public abstract class FollowerRole implements Runnable{
	
	private GroupDescriptor descriptor;
	protected boolean active;
	private int resourceCost;
	private A3Middlware middleware;
	private String nodeID;
	

	//This method will be called when a follower sends a message
	public abstract void receiveSupervisorMessage(A3Message msg);

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

	public void setMiddleware(A3Middlware middleware) {
		this.middleware = middleware;
	}

	public A3Middlware getMiddleware() {
		return middleware;
	}

	public String getNodeID() {
		return nodeID;
	}

	public void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}
	
	

}
