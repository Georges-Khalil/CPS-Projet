package fr.sorbonne_u.cps.pubsub.filters;

import java.io.Serializable;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.ValueFilterI;

public class ValueFilter implements ValueFilterI {
	private static final long serialVersionUID = 1L;
	
	private final Serializable referenceValue;
	
	public ValueFilter(Serializable referenceValue) {
		this.referenceValue = referenceValue;
	}
	
	@Override
	public boolean match(Serializable value) {
		if (referenceValue == null) {
			return value == null;
		}
		return referenceValue.equals(value);
	}
}