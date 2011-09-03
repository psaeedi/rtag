package it.polimi.rtag;

import java.util.*;
import polimi.reds.NodeDescriptor;


/**
 * @author panteha
 * 
 */

public class FollowerDescriptor extends NodeDescriptor {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8499411642653798031L;
	/**
	 * 
	 */
	
	
	private ArrayList<GroupDescriptor> groupDescriptors;

	public FollowerDescriptor(String mainURL) {
		super(mainURL, false);
	}

	/**
	 * @return the groupDescriptors
	 */
	public ArrayList<GroupDescriptor> getGroupDescriptors() {
		return groupDescriptors;
	}

}
