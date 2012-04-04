package it.polimi.rtag.app.operator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Map;

import it.polimi.rtag.GroupDescriptor;
import it.polimi.rtag.Node;
import it.polimi.rtag.app.CallableApp;
import it.polimi.rtag.app.RemoteCallable;
import it.polimi.rtag.messaging.TupleMessage;




public class AppMessage extends RemoteCallable {
	
	AppNode node;

	public AppMessage(String name, String responseName) {
		super(name, responseName);
		
	}

	@Override
	public Serializable doCompute(Map<String, Serializable> args,
			Node currentNode) {
		return null;
	}
	
	public void createOnetoOneMessage(AppNode sender, AppNode receiver){
	
    AppMessage message = new AppMessage("sendMessage", "responseMessage");
    
    CallableApp appreceiver = new CallableApp();
	CallableApp appsender = new CallableApp();
    
    setSender(sender, appsender);
    setReceiver(receiver, appreceiver);
 
	appsender.putRemoteCallable(message.getName(), message);
	appsender.addCallListener(message.getName(), new MessageListener());
	appreceiver.putRemoteCallable(message.getName(), message);
	appreceiver.addResponseListener(message.getResponseName(), new ResponseListener());
	
	}
	
	public void createGroupcastMessage(AppNode sender, String groupFriendlyName){
		
	    AppMessage message = new AppMessage("sendMessage", "responseMessage");
	    
	    CallableApp appreceiver = new CallableApp();
		CallableApp appsender = new CallableApp();
		
		setSender(sender, appsender);
		appsender.putRemoteCallable(message.getName(), message);
		appsender.addCallListener(message.getName(), new MessageListener());  
		
			for (int j = 0; j < node.getNumberofNodes(groupFriendlyName); j++) {
				
					sendGroupcast(node.nodes.get(j), groupFriendlyName, "sendMessage");
					AppNode receiver = node.nodes.get(j);
					setReceiver(receiver, appreceiver);
					appreceiver.putRemoteCallable(message.getName(), message);
					appreceiver.addResponseListener(message.getResponseName(), new ResponseListener());
			}
		
		}
	
	
	private void setReceiver(AppNode receiver, CallableApp callableapp) {
		callableapp.setCurrentNode(receiver);
	}

	private void setSender(AppNode sender, CallableApp callableapp) {
		callableapp.setCurrentNode(sender);
	}
	
	private static void sendGroupcast(AppNode node, String groupFriendlyName, String content) {
		GroupDescriptor group = node.getGroup(groupFriendlyName);
    	if (group == null) {
    		return;
    	}
    	System.out.println(group);
    	
    	TupleMessage message = new GroupcastMessage(groupFriendlyName, content, "HELLO");
        node.getTupleSpaceManager().storeAndSend(message);
	}
	
	

	class MessageListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent event) {
		
		}
	}

	class ResponseListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent event) {
		
		}
	}


}
