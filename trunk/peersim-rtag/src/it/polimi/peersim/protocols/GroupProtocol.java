package it.polimi.peersim.protocols;

import java.util.ArrayList;


import peersim.core.Protocol;

// TODO this was only an experiment. at some point we will have to remove it
public class GroupProtocol implements Protocol {
	
	public GroupProtocol(String prefix) {
	}

	private ArrayList<String> groups = new ArrayList<String>();

	@Override
	public Object clone() {
		GroupProtocol inp = null;
        try {
            inp = (GroupProtocol) super.clone();
            inp.groups = (ArrayList<String>) this.groups.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens
        return inp;
    }

	public boolean add(String e) {
		return groups.add(e);
	}

	public void clear() {
		groups.clear();
	}

	public boolean remove(Object o) {
		return groups.remove(o);
	}

	public int size() {
		return groups.size();
	}
	
	

	
}
