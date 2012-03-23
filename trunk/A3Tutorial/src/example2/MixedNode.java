package example2;

import A3.A3Middlware;
import A3.A3Node;

public class MixedNode extends A3Node {

	public MixedNode(A3Middlware middleware, String name) {
		super(middleware, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setup(String colour) {
		// TODO Auto-generated method stub
		middleware.addNodeToGroup(colour, this);
	}

	@Override
	public void kill(String colour) {
		// TODO Auto-generated method stub
		middleware.removeNodeFromGroup(colour, this);
	}

	

}
