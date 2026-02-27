package fr.sorbonne_u.cps.pubsub.filters;

import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI.PropertyI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class MessageFilter implements MessageFilterI {
    private static final long serialVersionUID = 1L;
    
    private final PropertyFilterI[] propertyFilters;
    private final PropertiesFilterI[] propertiesFilters;
    private final TimeFilterI timeFilter;

    public MessageFilter(PropertyFilterI... propertyFilters) {
        this(null, propertyFilters);
    }

    public MessageFilter(TimeFilterI timeFilter, PropertyFilterI... propertyFilters) {
        this(propertyFilters, null, timeFilter);
    }

    public MessageFilter(PropertyFilterI[] propertyFilters, 
                         PropertiesFilterI[] propertiesFilters, 
                         TimeFilterI timeFilter) {
        this.propertyFilters = propertyFilters != null ? propertyFilters : new PropertyFilterI[0];
        this.propertiesFilters = propertiesFilters != null ? propertiesFilters : new PropertiesFilterI[0];
        this.timeFilter = timeFilter != null ? timeFilter : TimeFilter.acceptAny();
    }
    
    @Override
    public PropertyFilterI[] getPropertyFilters() {
        return this.propertyFilters;
    }
    
    @Override
    public PropertiesFilterI[] getPropertiesFilters() {
        return this.propertiesFilters;
    }
    
    @Override
    public TimeFilterI getTimeFilter() {
        return this.timeFilter;
    }
    
    @Override
    public boolean match(MessageI message) {
        if (message == null)
            throw new IllegalArgumentException();
        
        if (!this.timeFilter.match(message.getTimeStamp()))
            return false;
        
        PropertyI[] properties = message.getProperties();
        
        for (PropertyFilterI propertyFilter : this.propertyFilters) {
            boolean not_found = true;
            for (PropertyI property : properties)
                if (propertyFilter.match(property)) {
                    not_found = false;
                    break;
                }
            if (not_found)
                return false;
        }
        
        for (PropertiesFilterI propertiesFilter : this.propertiesFilters)
            if (!propertiesFilter.match(properties))
                return false;
        
        return true;
    }
}