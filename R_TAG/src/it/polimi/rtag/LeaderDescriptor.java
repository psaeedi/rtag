package it.polimi.rtag;
import java.util.ArrayList;

import polimi.reds.NodeDescriptor;


/**
 * @author panteha
 *
 */
public class LeaderDescriptor extends NodeDescriptor {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9013479057481042624L;

	private GroupDescriptor leadedGroup;
	private ArrayList<GroupDescriptor> groupDescriptors;
	
	
	public LeaderDescriptor(String mainURL) {
		super(mainURL, true);
	}


	/**
	 * @return the leadedGroup
	 */
	public GroupDescriptor getLeadedGroup() {
		return leadedGroup;
	}


	/**
	 * @param leadedGroup the leadedGroup to set
	 */
	public void setLeadedGroup(GroupDescriptor leadedGroup) {
		this.leadedGroup = leadedGroup;
	}


	/**
	 * @return the groupDescriptors
	 */
	public ArrayList<GroupDescriptor> getGroupDescriptors() {
		return groupDescriptors;
	}

	
	
}