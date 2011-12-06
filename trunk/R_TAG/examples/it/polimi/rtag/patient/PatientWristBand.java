package it.polimi.rtag.patient;

import java.net.ConnectException;
import java.net.MalformedURLException;

import polimi.reds.broker.overlay.AlreadyNeighborException;
import polimi.reds.broker.overlay.NotRunningException;
import it.polimi.rtag.Node;

public class PatientWristBand {
	
	public enum PatientColor {
		RED,BLUE,GREEN,WHITE,BLACK
    }
	
	private Node node;
	// all patients seeing the GP have a white wrist band
	private PatientColor currentPatientColor = PatientColor.WHITE;
	
	public PatientWristBand(String host, int port, PatientColor currentPatientColor) {
	    node = new Node(host, port);
		node.start();
		setCurrentPatientColor(currentPatientColor);
	}

	private void setCurrentPatientColor(PatientColor currentPatientColor) {
		switch(this.currentPatientColor) {
		case RED:
		{
			switch(currentPatientColor) {
				case BLUE:
				{
					leaveGroup(PatientColor.RED);
					joinGroup(PatientColor.BLUE);
					break;
				}
				
				case GREEN:
				{
					leaveGroup(PatientColor.RED);
					joinGroup(PatientColor.GREEN);
					break;
				}
				
				case WHITE:
				{
					leaveGroup(PatientColor.RED);
					joinGroup(PatientColor.WHITE);
	                break;
				}
				
				case BLACK:
				{
					joinGroup(PatientColor.BLACK);
					leaveGroup(PatientColor.RED);
					break;
				}
			}
			break;
		}
		
		case BLUE:
		{
			switch(currentPatientColor) {
			case RED:
			{
				leaveGroup(PatientColor.BLUE);
				joinGroup(PatientColor.RED);
				break;
			}
			
			
			
			case GREEN:
			{
				leaveGroup(PatientColor.BLUE);
				joinGroup(PatientColor.GREEN);
				break;
			}
			
			case WHITE:
			{
				joinGroup(PatientColor.WHITE);
				leaveGroup(PatientColor.BLUE);
				break;
			}
			
			case BLACK:
			{
				leaveGroup(PatientColor.BLUE);
				joinGroup(PatientColor.BLACK);
				break;
			}
		}
			break;
		}
		
		
		case GREEN:
		{
			switch(currentPatientColor) {
			case RED:
			{
				leaveGroup(PatientColor.GREEN);
				joinGroup(PatientColor.RED);
				break;
			}
			
			case BLUE:
			{
				leaveGroup(PatientColor.GREEN);
				joinGroup(PatientColor.BLUE);
				break;
			}
			
			case WHITE:
			{
				leaveGroup(PatientColor.GREEN);
				joinGroup(PatientColor.WHITE);
				break;
			}

			case BLACK:
			{
				leaveGroup(PatientColor.GREEN);
				joinGroup(PatientColor.BLACK);
				break;
			}
		}
			break;
		}
		
		case WHITE:
		{
			switch(currentPatientColor) {
			case RED:
			{
				leaveGroup(PatientColor.WHITE);
				joinGroup(PatientColor.RED);
				break;
			}
			
			case BLUE:
			{
				leaveGroup(PatientColor.WHITE);
				joinGroup(PatientColor.BLUE);
				break;
			}
			
			case GREEN:
			{
				leaveGroup(PatientColor.WHITE);
				joinGroup(PatientColor.GREEN);
				break;
			}
			
			case BLACK:
			{
				leaveGroup(PatientColor.WHITE);
				joinGroup(PatientColor.BLACK);
				break;
			}
		}
			break;
		}
		
		case BLACK:
		{
			switch(currentPatientColor) {
			case RED:
			{
				leaveGroup(PatientColor.BLACK);
				joinGroup(PatientColor.RED);
				break;
			}
			
			case BLUE:
			{
				leaveGroup(PatientColor.BLACK);
				joinGroup(PatientColor.BLUE);
				break;
			}
			
			case GREEN:
			{
				joinGroup(PatientColor.GREEN);
				leaveGroup(PatientColor.BLACK);
				break;
			}
			
			case WHITE:
			{
				leaveGroup(PatientColor.BLACK);
				joinGroup(PatientColor.WHITE);
				break;
			}
			
		}
			break;
		}
		
	}
	this.currentPatientColor = currentPatientColor;
	
}

	public PatientColor getCurrentPatientColor() {
		return currentPatientColor;
	}
	
	private void joinGroup(PatientColor patientColor) {
		node.joinGroup(patientColor.toString());	
	}

	private void leaveGroup(PatientColor patientColor) {
		node.leaveGroup(patientColor.toString());
		
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


	}
	
	
	
	
