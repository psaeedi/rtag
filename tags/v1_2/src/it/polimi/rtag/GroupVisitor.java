/**
 * 
 */
package it.polimi.rtag;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 * 
 * Visits a group and performs ascertain action on it.
 * Used for printout
 */
public interface GroupVisitor {

	public void visit(GroupDescriptor groupDescriptor);
	public void visitParent();
	public void visitLeader();
	public void visitFollowers();
	
}
