package fr.sorbonne_u.cps.pubsub.filters;

import fr.sorbonne_u.cps.pubsub.interfaces.MessageI.PropertyI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.PropertyFilterI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.ValueFilterI;

public class PropertyFilter implements PropertyFilterI {
    private static final long serialVersionUID = 1L;
    
    private final String name;
    private final ValueFilterI valueFilter;
    
    public PropertyFilter(String name, ValueFilterI valueFilter) {
    if (name == null || valueFilter == null || name.isEmpty())
        throw new IllegalArgumentException();
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
        if (property == null)
            throw new IllegalArgumentException();
        return name.equals(property.getName()) && valueFilter.match(property.getValue());
    }
}