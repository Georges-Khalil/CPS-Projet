package fr.sorbonne_u.cps.pubsub.message;

import java.io.Serializable;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI.PropertyI;

public class Property implements PropertyI {
	private static final long serialVersionUID = 1L;
	
	private final String name;
	private final Serializable value;
	
	public Property(String name, Serializable value) {
		assert name != null && !name.isEmpty();
		this.name = name;
		this.value = value;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public Serializable getValue() {
		return this.value;
	}
}