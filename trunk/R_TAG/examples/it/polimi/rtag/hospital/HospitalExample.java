package it.polimi.rtag.hospital;

import it.polimi.rtag.hospital.HospitalJunction.IntelligentJunction;

public class HospitalExample {
	
	private static int port;
    private static int parent;
    
    private static String host;
    
    private static String urls;
    HospitalJunction apps;

	public static void main(String[] args) throws InterruptedException {
		   
		HospitalExample exp = new HospitalExample();
		
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
		    Thread.sleep(1000);
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
		
		//current node
		 apps = new HospitalJunction(host, port, IntelligentJunction.ONE);
		//the parent of the current node
		 urls = ("reds-tcp:"+ host + ":" + parent);
	
		 if(parent != 0)
		    {
		     System.out.println(" ");
		     System.out.println((new StringBuilder("***Adding neighbor ")).
		    		append(parent).append(" to node ").append(port).toString());
		     //connect the current node to its parent
		     apps.connectNeighbor(urls);
		     try {
				  Thread.sleep(1000);
			   }   catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			   }
		     }
	}
		
	}
