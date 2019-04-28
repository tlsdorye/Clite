package program;

import java.util.*;

public class TypeMap extends HashMap<Variable, Type> {

	// TypeMap is implemented as a Java HashMap.
	// Plus a 'display' method to facilitate experimentation.
	public void display() {
		System.out.println(this.entrySet());
	}

}
