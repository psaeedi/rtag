package it.polimi.rtag;
import java.util.HashSet;

import polimi.reds.NodeDescriptor;


/**
 * @author panteha
 * 
 * Extends the {@link NodeDescriptor} by storing group information.
 */
public class ExtendedNodeDescriptor extends NodeDescriptor {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9013479057481042624L;

	/**
	 * The local universe to which this node belongs.
	 * Each node can only belong to a single universe 
	 * which would be its local universe!
	 */
	private GroupDescriptor localUniverse;
	
	/**
	 * All the groups of which this node is leader. 
	 */
	private HashSet<GroupDescriptor> leadedGroups;
	
	/**
	 * All the groups of which this node is follower. 
	 */
	private HashSet<GroupDescriptor> followedGroups;
	
	//TODO implement this class

	public ExtendedNodeDescriptor(String mainURL) {
		super(mainURL, true);
	}
	
	
}