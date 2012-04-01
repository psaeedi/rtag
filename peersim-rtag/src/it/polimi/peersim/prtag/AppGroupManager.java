/**
 * 
 */
package it.polimi.peersim.prtag;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 *
 */
public class AppGroupManager {

	private String name;
	private GroupDescriptor followedGroup;
	private GroupDescriptor leadedGroup;
	
	public AppGroupManager(String name) {
		super();
		this.name = name;
	}

	public AppGroupManager(AppGroupManager oldgroupmanager) {
		this.name = oldgroupmanager.name;
	}

	public GroupDescriptor getFollowedGroup() {
		return followedGroup;
	}
	
	public void setFollowedGroup(GroupDescriptor followedGroup) {
		this.followedGroup = followedGroup;
	}
	
	public GroupDescriptor getLeadedGroup() {
		return leadedGroup;
	}
	
	public void setLeadedGroup(GroupDescriptor leadedGroup) {
		this.leadedGroup = leadedGroup;
	}
	
	
}
