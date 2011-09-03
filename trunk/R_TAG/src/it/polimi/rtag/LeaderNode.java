package it.polimi.rtag;

import java.io.Serializable;

import polimi.reds.Filter;
import polimi.reds.Message;
import polimi.reds.NodeDescriptor;
import polimi.reds.Reply;
import polimi.reds.broker.overlay.GenericOverlay;
import polimi.reds.broker.overlay.Overlay;
import polimi.reds.broker.routing.ReplyTable;
import polimi.reds.broker.routing.Router;
import polimi.reds.broker.routing.SubscriptionTable;

public class LeaderNode extends Node implements Router {

    private GroupingStrategy strategy;
    private LeaderDescriptor leaderDescriptor;

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

	public void setStrategy(GroupingStrategy strategy) {
		this.strategy = strategy;
	}

	public GroupingStrategy getStrategy() {
		return strategy;
	}

	public LeaderDescriptor getLeaderDescriptor() {
		return leaderDescriptor;
	}

	public void setLeaderDescriptor(LeaderDescriptor leaderDescriptor) {
		this.leaderDescriptor = leaderDescriptor;
		this.currentDescriptor = leaderDescriptor;
	}
	
	@Override
	public void setCurrentDescriptor(NodeDescriptor currentDescriptor) {
		if (!(currentDescriptor instanceof LeaderDescriptor)) {
			throw new IllegalArgumentException(
					"Leader descriptor must be an instance o LeaderDescriptor");
		}
		this.currentDescriptor = currentDescriptor;
		this.leaderDescriptor = (LeaderDescriptor) currentDescriptor;
	}

	@Override
	public NodeDescriptor getID() {
		// TODO Auto-generated method stub
		return null;
	}
   
}
