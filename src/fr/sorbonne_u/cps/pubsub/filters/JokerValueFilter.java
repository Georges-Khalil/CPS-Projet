package fr.sorbonne_u.cps.pubsub.filters;

import java.io.Serializable;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.ValueFilterI;

public class JokerValueFilter implements ValueFilterI {
	private static final long serialVersionUID = 1L;
	
	@Override
	public boolean match(Serializable value) {
		return true;
	}
}