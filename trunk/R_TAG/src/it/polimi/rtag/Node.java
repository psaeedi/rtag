package it.polimi.rtag;

import java.io.Serializable;

import lights.Tuple;

import it.polimi.rtag.messaging.MessageReport;
import polimi.reds.Filter;
import polimi.reds.Message;
import polimi.reds.NodeDescriptor;
import polimi.reds.Reply;
import polimi.reds.broker.overlay.Overlay;
import polimi.reds.broker.routing.ReplyTable;
import polimi.reds.broker.routing.Router;
import polimi.reds.broker.routing.SubscriptionTable;

public class Node implements Router{

    public NodeDescriptor currentDescriptor;
    private GroupingStrategy strategy;
    private Overlay overlay;
    
	@Override
	public void forwardReply(Reply arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Overlay getOverlay() {
		return overlay;
	}

	@Override
	public ReplyTable getReplyTable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SubscriptionTable getSubscriptionTable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void publish(NodeDescriptor arg0, Message arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOverlay(Overlay overlay) {
		this.overlay = overlay;
	}

	@Override
	public void subscribe(NodeDescriptor arg0, Filter arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unsubscribe(NodeDescriptor arg0, Filter arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unsubscribeAll(NodeDescriptor arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyPacketArrived(String arg0, NodeDescriptor arg1,
			Serializable arg2) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public NodeDescriptor getID() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setStrategy(GroupingStrategy strategy) {
		this.strategy = strategy;
	}

	public GroupingStrategy getStrategy() {
		return strategy;
	}

    

	public NodeDescriptor getCurrentDescriptor() {
		return currentDescriptor;
	}

	public void setCurrentDescriptor(NodeDescriptor currentDescriptor) {
		this.currentDescriptor = currentDescriptor;
	}
	
	/**
	 * Attempts to join a group and if it does not exist creates a new one
	 */
	public boolean joinOrCreate(Tuple groupDescription) {
		// TODO fix tuple with lights
		throw new AssertionError("Not yet implemented error.");
	}

	public MessageReport sendMessage(Message msg, NodeDescriptor... recipients) {
		throw new AssertionError("Not yet implemented error.");
	}
	
	
	protected void checkLeaderStatus() {
		throw new AssertionError("Not yet implemented error.");
	}
}
