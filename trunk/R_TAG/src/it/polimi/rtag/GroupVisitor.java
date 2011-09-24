/**
 * 
 */
package it.polimi.rtag;

/**
 * @author Panteha Saeedi (saeedi@elet.polimi.it)
 * 
 * Visits a group and performs acertain action on it.
 */
public interface GroupVisitor {

	public void visit(GroupDescriptor groupDescriptor);
	public void visitParent();
	public void visitLeader();
	public void visitFollowers();
	
}
