/**
 * 
 */
package it.polimi.rtag.app.example1;

import it.polimi.rtag.app.Command;
import it.polimi.rtag.app.CallableApp;

/**
 * @author pani
 *
 */
public class RedApp extends CallableApp {

	public Command joinApp = new Command("joinApp");
	public Command leaveApp = new Command("leaveApp");
	public Command becomeMaster = new Command("becomeMaster");
	public Command becomeSlave = new Command("becomeSlave");
	
	/**
	 * 
	 */
	public RedApp() {
		putCommand(joinApp);
		putCommand(leaveApp);
		putCommand(becomeMaster);
		putCommand(becomeSlave);
	}

}
