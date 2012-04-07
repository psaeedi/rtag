/*
 * 
 *
 */

package it.polimi.peersim.observers;

import it.polimi.peersim.protocols.GeoLocation;
import it.polimi.peersim.protocols.UniverseProtocol;
import it.polimi.peersim.prtag.LocalUniverseDescriptor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.graph.Graph;
import peersim.reports.GraphObserver;
import peersim.util.FileNameGenerator;

/**
 * @author Panteha Saeedi@ elet.polimi.it
 * 
 * This class prints to files the topology wiring using a Gnuplot friendly
 * syntax. Uses the {@link peersim.graph.Graph} interface to visit the topology.
 * 
 * @author
 */
public class GeoObserver extends GraphObserver {
    // ------------------------------------------------------------------------
    // Parameters
    // ------------------------------------------------------------------------

    /**
     * The filename base to print out the topology relations.
     * 
     * @config
     */
    private static final String PAR_FILENAME_BASE = "file_base";

    /**
     * The coordinate protocol to look at.
     * 
     * @config
     */
    private static final String PAR_COORDINATES_PROT = "plot_protocol";

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /**
     * Topology filename. Obtained from config property
     * {@link #PAR_FILENAME_BASE}.
     */
    private final String graph_filename;

    /**
     * Utility class to generate incremental indexed filenames from a common
     * base given by {@link #graph_filename}.
     */
    private final FileNameGenerator fng;

    /**
     * Coordinate protocol identifier. Obtained from config property
     * {@link #PAR_COORDINATES_PROT}.
     */
    private final int geoPid;
    
    private static final String DISCOVERY_PROTOCOL = "discovery_protocol";
	private static int discoveryProtocolId;
	
	private static final String UNIVERSE_PROTOCOL = "universe_protocol";
	private static int universeProtocolId;
	
	private ArrayList<UniverseProtocol> leadeduniverse =
			new ArrayList<UniverseProtocol>();

	private int thisCycle;
	
	public static ArrayList<Integer> cycles = new ArrayList<Integer>();

	private static int networkSize;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Standard constructor that reads the configuration parameters. Invoked by
     * the simulation engine.
     * 
     * @param prefix
     *            the configuration prefix for this class.
     */
    public GeoObserver(String prefix) {
        super(prefix);
     
        discoveryProtocolId = Configuration.getPid(
				prefix + "." + DISCOVERY_PROTOCOL);
        
        universeProtocolId = Configuration.getPid(
				prefix + "." + UNIVERSE_PROTOCOL);
        
        
        geoPid = Configuration.getPid(prefix + "." + PAR_COORDINATES_PROT);
        graph_filename = Configuration.getString(prefix + "."
                + PAR_FILENAME_BASE, "graph_dump");
        fng = new FileNameGenerator(graph_filename, ".dat");
        
        
    }

    // Control interface method.
    public boolean execute() {
        try {
            updateGraph();

            System.out.print(name + ": ");

            // initialize output streams
            String fname = fng.nextCounterName();
            FileOutputStream fos = new FileOutputStream(fname);
            System.out.println("Writing to file " + fname);
            PrintStream pstr = new PrintStream(fos);
  
            // dump topology:
            graphToFile(g, pstr, geoPid);
           //graphToFile(g, pstr, geoPid, universeProtocolId);
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    /**
     * Utility method: prints out data to plot the topology using gnuplot a
     * gnuplot style.
     * 
     * @param g
     *            current graph.
     * @param ps
     *            a {@link java.io.PrintStream} object to write to.
     * @param thisCycle 
     * @param coordPid
     *            coordinate protocol identifier.
     */
   /* private static void graphToFile(Graph g, PrintStream ps, int geoPid) {
    	
        for (int i = 1; i < g.size(); i++) {
            Node current = (Node) g.getNode(i);
            double x_to = ((GeoLocation) current
                    .getProtocol(geoPid)).getX();
            double y_to = ((GeoLocation) current
                    .getProtocol(geoPid)).getY();
            Node n = (Node) g.getNode(i);
			DiscoveryProtocol discoveryProtocol = (DiscoveryProtocol) n.getProtocol(
					discoveryProtocolId);
			ArrayList<Node> neighbors = discoveryProtocol.getNeighbors();
			for	(Node k: neighbors) {
				 double x_from = ((GeoLocation) k
	                        .getProtocol(geoPid)).getX();
	             double y_from = ((GeoLocation) k
	                        .getProtocol(geoPid)).getY();
            ps.println(x_from + " " + y_from);
            ps.println(x_to + " " + y_to);
            ps.println(); 
            }
			
        }
    }*/
    
    

    private static void graphToFile(Graph g, PrintStream ps, int geoPid) {
    	    	
	    for (int i = 0; i < g.size(); i++) {
	        Node n = (Node) g.getNode(i);

	        
	        double x_to = ((GeoLocation) n
	                .getProtocol(geoPid)).getX();
	        double y_to = ((GeoLocation) n
	                .getProtocol(geoPid)).getY();
	        UniverseProtocol universeProtocol = (UniverseProtocol) n.getProtocol(
					universeProtocolId);
			
	        LocalUniverseDescriptor groupDescriptor = universeProtocol.getLocaluniverse();
	        if (groupDescriptor == null) {
	        	continue;
	        }
	       
			System.out.print("[Node " + n.getID() +" ] leaders {");
			for	(Node k: universeProtocol.leaders) {
				System.out.print(k.getID() +", ");
			}
			 System.out.print("} followers {");
			for	(Node k:  universeProtocol.followers) {
				 System.out.print(k.getID() +", ");
			}
			 System.out.println("}");
			
			for	(Node k: groupDescriptor.getFollowers()) {
				 double x_from = ((GeoLocation) k
	                        .getProtocol(geoPid)).getX();
	             double y_from = ((GeoLocation) k
	                        .getProtocol(geoPid)).getY();
	        ps.println(x_from + " " + y_from);
	        ps.println(x_to + " " + y_to);
	        ps.println(); 
	        }
			
	    }
	}
    
    
   
   
}
