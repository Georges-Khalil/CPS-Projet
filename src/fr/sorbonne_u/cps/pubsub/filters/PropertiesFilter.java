package fr.sorbonne_u.cps.pubsub.filters;

import java.io.Serializable;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI.PropertyI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.MultiValuesFilterI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.PropertiesFilterI;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class PropertiesFilter implements PropertiesFilterI {
    private static final long serialVersionUID = 1L;
    
    private final MultiValuesFilterI multiValuesFilter;
    
    public PropertiesFilter(MultiValuesFilterI multiValuesFilter) {
    if (multiValuesFilter == null || multiValuesFilter.getNames() == null || multiValuesFilter.getNames().length == 0)
        throw new IllegalArgumentException();
    this.multiValuesFilter = multiValuesFilter;
    }
    
    @Override
    public MultiValuesFilterI getMultiValuesFilter() {
        return this.multiValuesFilter;
    }

    @Override
    public boolean match(PropertyI... properties) {
        if (properties == null)
            throw new IllegalArgumentException();

        String[] names = multiValuesFilter.getNames();
        Serializable[] values = new Serializable[names.length];

        for (int i = 0; i < names.length; i++) {
            boolean found = false;
            for (PropertyI property : properties)
                if (names[i].equals(property.getName())) {
                    values[i] = property.getValue();
                    found = true;
                    break;
                }
            if (!found)
                return false;
        }

        return multiValuesFilter.match(values);
    }
}