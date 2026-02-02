package fr.sorbonne_u.cps.pubsub.filters.valuefilters;

import java.io.Serializable;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.ValueFilterI;

public class JokerValueFilter implements ValueFilterI {
	private static final long serialVersionUID = 1L;
	
	private static final JokerValueFilter INSTANCE = new JokerValueFilter();
	
	public static JokerValueFilter getInstance() {
		return INSTANCE;
	}
	
	private JokerValueFilter() {
	}
	
	@Override
	public boolean match(Serializable value) {
		return true;
	}
}