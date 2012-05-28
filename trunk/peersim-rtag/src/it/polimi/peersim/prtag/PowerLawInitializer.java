package it.polimi.peersim.prtag;

import peersim.core.Node;
import java.util.*;
import peersim.config.*;
import peersim.core.*;


public class PowerLawInitializer implements Control {
    private static final String PAR_ALPHA = "alpha";
    private static final String PAR_MAX = "max";

   // private static LogManager manager = LogManager.getLogManager();
    //private static Logger log = Logger.getLogger("fungus");

    private final String name;
    private static double alpha;
    private static double max;
    private static int intmax;
    //private final int pid;

    private static cern.jet.random.engine.RandomEngine generator;

    public PowerLawInitializer(String name) {
        this.name = name;
        alpha = Configuration.getDouble(name + "." + PAR_ALPHA);
        max = Configuration.getDouble(name + "." + PAR_MAX);
        intmax = (new Double(max).intValue());
        this.generator =
            new cern.jet.random.engine.MersenneTwister(CommonState.r.nextInt(Integer.MAX_VALUE));
    }

    public static double nextPowLaw(double alpha, double max) {
        return cern.jet.random.Distributions.nextPowLaw(alpha,max,generator);
    }

    public static int nextPowInt() {
        return intmax - (new Double(nextPowLaw(alpha,max))).intValue();
    }

	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		return false;
	}


   /* public static void initialize(MycoNode n) {
        n.getHyphaData().setMax(nextPowInt());
    }

    public boolean execute() {
        MycoNode n;
        HyphaData d;
        
        for (int i = 1; i < Network.size(); i++) {
            n = (MycoNode) Network.get(i);
            initialize(n);
        }
        
        return false;
    }*/

}