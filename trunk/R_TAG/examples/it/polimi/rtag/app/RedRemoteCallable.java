/**
 * 
 */
package it.polimi.rtag.app;

import java.io.Serializable;
import java.util.Map;

import it.polimi.rtag.Node;
import it.polimi.rtag.app.RemoteCallable;

/**
 * @author pani
 *
 */
public class RedRemoteCallable extends RemoteCallable {

	
	/**
	 * @param name
	 * @param responseName
	 */
	public RedRemoteCallable(String name, String responseName) {
		super(name, responseName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Serializable doCompute(Map<String, Serializable> args,
			Node currentNode) {
		// TODO Auto-generated method stub
		return null;
	}


}
