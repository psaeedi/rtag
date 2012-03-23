package A3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class A3Middlware {
	
	
	Map<String, A3Group> existingGroups = new HashMap<String, A3Group>();
	

	public Set<String> getGroups() {
		// TODO Auto-generated method stub
		return existingGroups.keySet();
	}


	private void createGroup(String groupName, A3Node node) {	
		existingGroups.put(groupName, new A3Group(groupName, node));
	}
	
	
	public void removeGroup(String groupName){
		A3Group group = existingGroups.get(groupName);
		existingGroups.remove(group);
		group.getSupervisor().getSupervisorRole(groupName).deactivate();
		for(A3Node node : group.getFollowers()){
			node.getFollowerRole(groupName).deactivate();
		}
		System.out.println("\n\nThe gruop "+ groupName +" has been eliminated\n\n");
	}


	private void joinGroup(String groupName, A3Node node) {
		A3Group group = existingGroups.get(groupName);
		if (group!=null) {
			group.addFollower(node);
			A3UpdateMessage update = new A3UpdateMessage();
			update.setMessage(node.getName()+" join the group: "+groupName);
			group.getSupervisor().getSupervisorRole(groupName).receiveGroupUpdate(update);
		}
		
	}


	public void sendToFollowers(A3Message msg, String groupName) {
		// TODO Auto-generated method stub
		A3Group group = existingGroups.get(groupName);
		if (group!= null) {
			group.sendMsgToFollowers(msg);
		}
	}

	public void sendToSupervisor(A3Message msg, String groupName){
		A3Group group = existingGroups.get(groupName);
		if (group!= null) {
			group.sendMsgToSupervisor(msg);
		}
	}
	
	
	public void addNodeToGroup(String groupName, A3Node node){
		A3Group group = existingGroups.get(groupName);
		if (group==null) {
			//create group only if node is Supervisor
			if(node.getSupervisorRole(groupName)!=null){
				createGroup(groupName, node);
				node.getSupervisorRole(groupName).activate();
				new Thread(node.getSupervisorRole(groupName)).start();
				System.out.println("\n\n["+node.getName()+"] is the new supervisor of "+groupName);
			}
		}else
			//join group only if node is Follower
			if(node.getFollowerRole(groupName)!=null){
				joinGroup(groupName, node);
				node.getFollowerRole(groupName).activate();
				new Thread(node.getFollowerRole(groupName)).start();
			}
	}
	
	
	public void removeNodeFromGroup(String groupName, A3Node node){
		A3Group group = existingGroups.get(groupName);
		if(group!=null){
			//remove a Supervisor node
			if(group.getSupervisor()==node){
				List<A3Node> follower = group.getFollowers();
				boolean flag=true;
				for(int i=0; i<follower.size()&&flag; i++ ){
					A3Node newSup = follower.get(i);
					if(newSup.getSupervisorRole(groupName)!=null){
						
						node.getSupervisorRole(groupName).deactivate();
						newSup.getFollowerRole(groupName).deactivate();
						group.removeFollower(newSup);
						newSup.getSupervisorRole(groupName).activate();
						new Thread(newSup.getSupervisorRole(groupName)).start();
						group.setSupervisor(newSup);
						System.out.println("\n\n["+newSup.getName()+"] is the new supervisor of "+groupName);
						flag=false;
					}
				}
				if(flag){
					this.removeGroup(groupName);
				}
			}else{
				//remove a Follower node
				group.removeFollower(node);
				node.getFollowerRole(groupName).deactivate();
				A3UpdateMessage update = new A3UpdateMessage();
				update.setMessage("\n\n"+node.getName()+" leaves the group: "+groupName+"\n\n");
				group.getSupervisor().getSupervisorRole(groupName).receiveGroupUpdate(update);
			}
			
		}
	}

}
