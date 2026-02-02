package fr.sorbonne_u.cps.pubsub.filters;

import fr.sorbonne_u.cps.pubsub.interfaces.MessageI.PropertyI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.PropertyFilterI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.ValueFilterI;

public class PropertyFilter implements PropertyFilterI {
	private static final long serialVersionUID = 1L;
	
	private final String name;
	private final ValueFilterI valueFilter;
	
	public PropertyFilter(String name, ValueFilterI valueFilter) {
		assert name != null && !name.isEmpty();
		assert valueFilter != null;
		this.name = name;
		this.valueFilter = valueFilter;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public ValueFilterI getValueFilter() {
		return this.valueFilter;
	}
	
	@Override
	public boolean match(PropertyI property) {
		if (property == null) {
			return false;
		}
		if (!name.equals(property.getName())) {
			return false;
		}
		return valueFilter.match(property.getValue());
	}
}