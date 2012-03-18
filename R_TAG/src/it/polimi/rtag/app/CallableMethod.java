/**
 * 
 */
package it.polimi.rtag.app;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public abstract class CallableMethod {

	private String name;
	private String responseName;
	
	public CallableMethod(String name, String responseName) {
		super();
		this.name = name;
		this.responseName = responseName;
	}
	
	public abstract Serializable doCompute(Map<String, Serializable> args);

	public String getName() {
		return name;
	}

	public String getResponseName() {
		return responseName;
	}
}
