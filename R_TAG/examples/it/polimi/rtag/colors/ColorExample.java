/**
 * 
 */
package it.polimi.rtag.colors;

import it.polimi.rtag.colors.ColorApplication.Color;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;


/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class ColorExample {
	
	private static final int NUMBER_OF_NODES = 6;

	int localPort = 10001;
    
    String host = "localhost";
    
    ArrayList<ColorApplication> apps = new ArrayList<ColorApplication>();
    ArrayList<String> urls = new ArrayList<String>();
    
    
    PrintWriter pw = null;
    
    public ColorExample() {
    	try {
			pw = new PrintWriter(new File("ColorExample.csv"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
  
    private void setUpAndColor() {
    	
		for (int i = 0; i < NUMBER_OF_NODES; i++) {
			int port = localPort ++;
			ColorApplication node = new ColorApplication(host, port, Color.RED);
			apps.add(node);
			urls.add("reds-tcp:"+ host + ":" + port);
		}
	
		for (int i = 0; i < NUMBER_OF_NODES; i++) {
			int port = localPort ++;
			ColorApplication node = new ColorApplication(host, port, Color.BLUE);
			apps.add(node);
			urls.add("reds-tcp:"+ host + ":" + port);
		}
		
		for (int i = 0; i < NUMBER_OF_NODES; i++) {
			int port = localPort ++;
			ColorApplication node = new ColorApplication(host, port, Color.YELLOW);
			apps.add(node);
			urls.add("reds-tcp:"+ host + ":" + port);
		}
		
		createNetworkByAddingToNode0();
		changeColor(apps.get(2), Color.ORANGE);
		changeColor(apps.get(5), Color.PURPLE);
    }
    
    
    
    private void closeFile() {
    	pw.flush();
    	pw.close();
    	
    }
    
    private void Stop() {
    	for(ColorApplication application: apps) {
			application.tearDown();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
	}
 

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ColorExample exp = new ColorExample();
		exp.setUpAndColor();
		// 1- Send a groupcast to each group
		// Verify that all of them have received the right message
		// Count the number of sent messages for each groupcast
		
		// 2- Set the color to purple for all of them
		// Verify that they regroup
		
		// 3- Send a groupcast to purple
		// Verify that all have received the message
		// Count the number of messages sent
		
		// 4- Write to file
		
		exp.closeFile();
		exp.Stop();
	}
	
	private void createNetworkByAddingToNode0() {
    	for (int i = 1; i < 3 * NUMBER_OF_NODES; i++) {
    		System.out.println("************Adding neighbor " + i + " to node 0"+ apps.get(i));
    		apps.get(0).connectNeighbor(urls.get(i));
    		try {
				Thread.sleep(1000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
	
	private void changeColor(ColorApplication app, Color color) {
		app.setCurrentColor(color);
	}
	


}
