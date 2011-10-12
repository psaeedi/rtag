/**
 * 
 */
package it.polimi.rtag.colors;

import java.net.ConnectException;
import java.net.MalformedURLException;

import lights.Field;
import lights.Tuple;

import polimi.reds.broker.overlay.AlreadyNeighborException;
import polimi.reds.broker.overlay.NotRunningException;
import it.polimi.rtag.GroupCommunicationDispatcher;
import it.polimi.rtag.GroupDescriptor;
import it.polimi.rtag.Node;
import it.polimi.rtag.colors.ColorApplication.Color;
import it.polimi.rtag.messaging.*;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 *
 */
public class ColorApplication {
	
	
	private static TupleMessage GROUPCAST;
	
	public static String RED = "Red";
	public static String GREEN = "Green";
	public static String YELLOW = "Yellow";
	public static String BLUE = "Blue";
	public static String ORANGE = "Orange";
	public static String PURPLE = "Purple";
	public static String WHITE = "White";
	public static String BLACK = "Black";
	
	
	private Node node;
	private Color currentColor = Color.RED;
	
	public ColorApplication(String host, int port, Color currentColor) {
	    node = new Node(host, port);
		node.start();
		setCurrentColor(currentColor);
	}

	public Color getCurrentColor() {
		return currentColor;
	}

	public void setCurrentColor(Color currentColor) {
		switch(this.currentColor) {
			case RED:
			{
				switch(currentColor) {
					case RED:
					{
						break;
					}
					
					case BLUE:
					{
						leaveGroup(Color.RED);
						joinGroup(Color.BLUE);
						break;
					}
					
					case YELLOW:
					{
						leaveGroup(Color.RED);
						joinGroup(Color.YELLOW);
						break;
					}
					
					case GREEN:
					{
						leaveGroup(Color.RED);
						joinGroup(Color.BLUE);
						joinGroup(Color.YELLOW);
						break;
					}
					
					case PURPLE:
					{
						joinGroup(Color.BLUE);
						break;
					}
					
					case WHITE:
					{
						joinGroup(Color.BLUE);
						joinGroup(Color.YELLOW);
						break;
					}
					
					case ORANGE:
					{
						joinGroup(Color.YELLOW);
						break;
					}
					
					case BLACK:
					{
						leaveGroup(Color.RED);
						break;
					}
				}
				break;
			}
			
			case BLUE:
			{
				switch(currentColor) {
				case RED:
				{
					leaveGroup(Color.BLUE);
					joinGroup(Color.RED);
					break;
				}
				
				case BLUE:
				{
					break;
				}
				
				case YELLOW:
				{
					leaveGroup(Color.BLUE);
					joinGroup(Color.YELLOW);
					break;
				}
				
				case GREEN:
				{
					joinGroup(Color.YELLOW);
					break;
				}
				
				case PURPLE:
				{
					joinGroup(Color.RED);
					break;
				}
				
				case WHITE:
				{
					joinGroup(Color.RED);
					joinGroup(Color.YELLOW);
					break;
				}
				
				case ORANGE:
				{
					leaveGroup(Color.BLUE);
					joinGroup(Color.RED);
					joinGroup(Color.YELLOW);
					break;
				}
				
				case BLACK:
				{
					leaveGroup(Color.BLUE);
					break;
				}
			}
				break;
			}
			
			case YELLOW:
			{
				switch(currentColor) {
				case RED:
				{
					leaveGroup(Color.YELLOW);
					joinGroup(Color.RED);
					break;
				}
				
				case BLUE:
				{
					leaveGroup(Color.YELLOW);
					joinGroup(Color.BLUE);
					break;
				}
				
				case YELLOW:
				{
					break;
				}
				
				case GREEN:
				{
					joinGroup(Color.RED);
					break;
				}
				
				case PURPLE:
				{
					leaveGroup(Color.YELLOW);
					joinGroup(Color.BLUE);
					joinGroup(Color.RED);
					break;
				}
				
				case WHITE:
				{
					joinGroup(Color.BLUE);
					joinGroup(Color.RED);
					break;
				}
				
				case ORANGE:
				{
					joinGroup(Color.RED);
					break;
				}
				case BLACK:
				{
					leaveGroup(Color.YELLOW);
					break;
				}
			}
				break;
			}
			
			case GREEN:
			{
				switch(currentColor) {
				case RED:
				{
					leaveGroup(Color.BLUE);
					leaveGroup(Color.YELLOW);
					joinGroup(Color.RED);
					break;
				}
				
				case BLUE:
				{
					leaveGroup(Color.YELLOW);
					break;
				}
				
				case YELLOW:
				{
					leaveGroup(Color.BLUE);
					break;
				}
				
				case GREEN:
				{
					break;
				}
				
				case PURPLE:
				{
					leaveGroup(Color.YELLOW);
					joinGroup(Color.RED);
					break;
				}
				
				case WHITE:
				{
					leaveGroup(Color.BLUE);
					break;
				}
				
				case ORANGE:
				{
					leaveGroup(Color.BLUE);
					joinGroup(Color.RED);
					break;
				}
				
				case BLACK:
				{
					leaveGroup(Color.BLUE);
					leaveGroup(Color.YELLOW);
					break;
				}
			}
				break;
			}
			
			case PURPLE:
			{
				switch(currentColor) {
				case RED:
				{
					leaveGroup(Color.BLUE);
					break;
				}
				
				case BLUE:
				{
					leaveGroup(Color.RED);
					break;
				}
				
				case YELLOW:
				{
					leaveGroup(Color.BLUE);
					leaveGroup(Color.RED);
					joinGroup(Color.YELLOW);
					break;
				}
				
				case GREEN:
				{
					leaveGroup(Color.RED);
					joinGroup(Color.YELLOW);
					break;
				}
				
				case PURPLE:
				{
					break;
				}
				
				case WHITE:
				{
					joinGroup(Color.YELLOW);
					break;
				}
				
				case ORANGE:
				{
					leaveGroup(Color.BLUE);
					joinGroup(Color.YELLOW);
					break;
				}
				case BLACK:
				{
					leaveGroup(Color.RED);
					leaveGroup(Color.BLUE);
					break;
				}
			}
				break;
			}
			
			case WHITE:
			{
				switch(currentColor) {
				case RED:
				{
					leaveGroup(Color.BLUE);
					leaveGroup(Color.YELLOW);
					break;
				}
				
				case BLUE:
				{
					leaveGroup(Color.RED);
					leaveGroup(Color.YELLOW);
					break;
				}
				
				case YELLOW:
				{
					leaveGroup(Color.BLUE);
					leaveGroup(Color.RED);
					break;
				}
				
				case GREEN:
				{
					leaveGroup(Color.RED);
					break;
				}
				
				case PURPLE:
				{
					leaveGroup(Color.YELLOW);
					break;
				}
				
				case WHITE:
				{
					break;
				}
				
				case ORANGE:
				{
					leaveGroup(Color.BLUE);
					break;
				}
				
				case BLACK:
				{
					leaveGroup(Color.RED);
					leaveGroup(Color.BLUE);
					leaveGroup(Color.YELLOW);
					break;
				}
			}
				break;
			}
			
			case ORANGE:
			{
				switch(currentColor) {
				case RED:
				{
					leaveGroup(Color.YELLOW);
					break;
				}
				
				case BLUE:
				{
					leaveGroup(Color.YELLOW);
					leaveGroup(Color.RED);
					joinGroup(Color.BLUE);
					break;
				}
				
				case YELLOW:
				{
					leaveGroup(Color.RED);
					break;
				}
				
				case GREEN:
				{
					leaveGroup(Color.RED);
					joinGroup(Color.BLUE);
					break;
				}
				
				case PURPLE:
				{
					leaveGroup(Color.YELLOW);
					joinGroup(Color.BLUE);
					break;
				}
				
				case WHITE:
				{
					joinGroup(Color.BLUE);
					break;
				}
				
				case ORANGE:
				{
					break;
				}
				case BLACK:
				{
					leaveGroup(Color.RED);
					leaveGroup(Color.YELLOW);
					break;
				}
			}
				break;
			}
			
			case BLACK:
			{
				switch(currentColor) {
				case RED:
				{
					joinGroup(Color.RED);
					break;
				}
				
				case BLUE:
				{
					joinGroup(Color.BLUE);
					break;
				}
				
				case YELLOW:
				{
					joinGroup(Color.YELLOW);
					break;
				}
				
				case GREEN:
				{
					joinGroup(Color.BLUE);
					joinGroup(Color.YELLOW);
					break;
				}
				
				case PURPLE:
				{
					joinGroup(Color.BLUE);
					joinGroup(Color.RED);
					break;
				}
				
				case WHITE:
				{
					joinGroup(Color.BLUE);
					joinGroup(Color.YELLOW);
					joinGroup(Color.RED);
					break;
				}
				
				case ORANGE:
				{
					joinGroup(Color.YELLOW);
					joinGroup(Color.RED);
					break;
				}
				
				case BLACK:
				{
					break;
				}
			}
				break;
			}
			
		
		}
		this.currentColor = currentColor;
		
	}
	
	protected void joinGroup(Color color){
		node.joinGroup(color.toString());
				
	}
	
	protected void leaveGroup(Color color){
		node.leaveGroup(color.toString());
		
	}
	
	public void connectNeighbor(String url){
		try {
			node.addNeighbor(url);
		} catch (AlreadyNeighborException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConnectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotRunningException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	 public void tearDown() {
		node.stop();
	}
	 
	public enum Color {
		RED, BLUE, YELLOW, GREEN, PURPLE, WHITE, ORANGE, BLACK;
	}


	public void sendGroupcast(Color color, String command) {
    	GroupDescriptor colorGroup = node.getGroup(color.toString());
    	if (colorGroup == null) {
    		return;
    	}

    	GroupcastTupleMessage message = null;
    	// TODO instantiate the message
    	node.sendGroupcast(message);
	}




	
}
