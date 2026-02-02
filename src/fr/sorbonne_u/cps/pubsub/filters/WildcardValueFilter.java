package fr.sorbonne_u.cps.pubsub.filters;

import java.io.Serializable;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.ValueFilterI;

public class WildcardValueFilter implements ValueFilterI {
	private static final long serialVersionUID = 1L;
	
	private static final WildcardValueFilter INSTANCE = new WildcardValueFilter();
	
	public static WildcardValueFilter getInstance() {
		return INSTANCE;
	}
	
	private WildcardValueFilter() {
	}
	
	@Override
	public boolean match(Serializable value) {
		return true;
	}
}