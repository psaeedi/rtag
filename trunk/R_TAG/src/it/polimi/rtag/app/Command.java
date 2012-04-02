/**
 * 
 */
package it.polimi.rtag.app;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 * a synchronous command without response
 */
public class Command {

	private String name;
	
	public Command(String name) {
		super();
		//name of the command method
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
