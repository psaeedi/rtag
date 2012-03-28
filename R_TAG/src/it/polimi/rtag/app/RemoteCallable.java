/**
 * 
 */
package it.polimi.rtag.app;

import java.io.Serializable;
import java.util.Map;
import it.polimi.rtag.Node;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 * extend this for each method that you want to call
 */
public abstract class RemoteCallable {

	private String name;
	private String responseName;
	
	public RemoteCallable(String name, String responseName) {
		super();
		//name of the callable method
		//also used as a command in message
		this.name = name;
		this.responseName = responseName;
	}
	
	/**
	 * general way to call a calable method
	 * @param args a map name and value of parameter
	 *
	 * @return a serializable object
	 */
	public abstract Serializable doCompute(Map<String, Serializable> args, Node currentNode);

	public String getName() {
		return name;
	}

	public String getResponseName() {
		return responseName;
	}
}
