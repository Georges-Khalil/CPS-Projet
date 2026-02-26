package fr.sorbonne_u.cps.pubsub.message;

import java.io.Serializable;

import fr.sorbonne_u.cps.pubsub.interfaces.MessageI.PropertyI;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class Property implements PropertyI {
    private static final long serialVersionUID = 1L;
    
    private final String name;
    private final Serializable value;
    
    public Property(String name, Serializable value) {
        if (name == null ||  value == null || name.isEmpty())
            throw new IllegalArgumentException();
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Property property = (Property) o;
        return this.name.equals(property.name) && this.value.equals(property.value);
    }
}