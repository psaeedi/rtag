package it.polimi.rtag.patient;

import it.polimi.rtag.patient.PatientWristBand.PatientColor;

public class PatientExample {
	
	private static int port;
    private static int parent;
    private static String host;
    
    private static String urls;
    PatientWristBand apps;
 
 
	public static void main(String[] args) throws InterruptedException {
		
		PatientExample exp = new PatientExample();
		
		 String args1[];
		    int k = (args1 = args).length;
		    
		    for(int i = 0; i < k; i++)
		    {
		    String arg = args1[i];
		    System.out.print(" ");
		    System.out.print(arg);
		    System.out.print(" ");
		    }
		    
		    for(int j = 0; j < k; j++)
		    {
		    String arg = args1[j];
		    String splited1[] = arg.split("=", 2);
		    String splited2[] = arg.split(":", 2);
		    String splited3[] = arg.split("-", 2);
		    if(splited1[0].equalsIgnoreCase("port"))
		    port = Integer.parseInt(splited1[1]);
		    if(splited2[0].equalsIgnoreCase("parent"))
		    parent = Integer.parseInt(splited2[1]);
		    if(splited3[0].equalsIgnoreCase("host"))
		    host = splited3[1];
		    
		    }
		    
		    exp.colorUpAndConnect(); 
		    System.out.print(" ");
		    System.out.println(" ");
		    Thread.sleep(2000);
		    exp.stop();
	}

	private void stop() { 
			apps.tearDown();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }

	private void colorUpAndConnect() {
		 apps = new PatientWristBand(host, port, PatientColor.WHITE);
		 urls = ("reds-tcp:"+ host + ":" + parent);
	
		 if(parent != 0)
		    {
		     System.out.println(" ");
		     System.out.println("***Adding neighbor " + parent + " to node " + port);
		     apps.connectNeighbor(urls);
 		     try {
				  Thread.sleep(500);
			   }   catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			   }
		     }

	 }

	
}
