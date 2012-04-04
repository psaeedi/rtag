package it.polimi.rtag.app.operator;

import java.io.Serializable;

import it.polimi.rtag.messaging.TupleMessage;


public class GroupcastMessage extends TupleMessage {

		private static final long serialVersionUID = -5146903837877861792L;
		
		public GroupcastMessage(String recipient,
				Serializable content, String command) {
			super(Scope.HIERARCHY, recipient, content, command);
		}
		
		@Override
		public String getSubject() {
			// TODO Auto-generated method stub
			return CUSTOM_MESSAGE;
		}

}
