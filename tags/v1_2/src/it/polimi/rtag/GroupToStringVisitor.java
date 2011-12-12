/**
 * 
 */
package it.polimi.rtag;

import polimi.reds.NodeDescriptor;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class GroupToStringVisitor implements GroupVisitor {

	private GroupDescriptor groupDescriptor;
	private StringBuilder builder;
	
	@Override
	public void visit(GroupDescriptor groupDescriptor) {
		this.groupDescriptor = groupDescriptor;
		builder = new StringBuilder();
		builder.append("Id: " + groupDescriptor.getUniqueId() + "\n");
		builder.append("Name: " + groupDescriptor.getFriendlyName() + "\n");
		visitParent();
		visitLeader();
		visitFollowers();
	}
	
	/* (non-Javadoc)
	 * @see it.polimi.rtag.GroupVisitor#visitParent()
	 */
	@Override
	public void visitParent() {
		builder.append("Parent: " + groupDescriptor.getParentLeader() + "\n");
	}

	/* (non-Javadoc)
	 * @see it.polimi.rtag.GroupVisitor#visitLeader()
	 */
	@Override
	public void visitLeader() {
		builder.append("Leader: " + groupDescriptor.getLeader() + "\n");
	}

	/* (non-Javadoc)
	 * @see it.polimi.rtag.GroupVisitor#visitFollowers()
	 */
	@Override
	public void visitFollowers() {
		for (NodeDescriptor node: groupDescriptor.getFollowers()) {
			builder.append("Follower: " + node + "\n");
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return builder.toString();
	}

	
}
