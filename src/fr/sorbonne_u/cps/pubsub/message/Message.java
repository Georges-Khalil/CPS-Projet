package fr.sorbonne_u.cps.pubsub.message;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import fr.sorbonne_u.cps.pubsub.exceptions.UnknownPropertyException;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class Message implements MessageI {
    private static final long serialVersionUID = 1L;
    
    private final List<PropertyI> properties;
    private Serializable payload;
    private final Instant timestamp;
    
    public Message() {
        this("");
    }

    public Message(Serializable payload) {
        this(payload, Instant.now());
    }

    public Message(Serializable payload, Instant timestamp) {
        this(payload, timestamp, new ArrayList<>());
    }
    
    public Message(Instant timestamp, List<PropertyI> properties) {
        this("", timestamp, properties);
    }

    public Message(Serializable payload, Instant timestamp, List<PropertyI> properties) {
        if (timestamp == null || properties == null || payload == null)
            throw new IllegalArgumentException();
        this.timestamp = timestamp;
        this.properties = new ArrayList<>(properties);
        this.payload = payload;
    }
    
    @Override
    public boolean propertyExists(String name) {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException();
        for (PropertyI p : properties)
            if (p.getName().equals(name))
                return true;
        return false;
    }
    
    @Override
    public void putProperty(String name, Serializable value) {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException();
        if (this.propertyExists(name))
            throw new RuntimeException("Property already exists");
        properties.add(new Property(name, value));
    }
    
    @Override
    public void removeProperty(String name) throws UnknownPropertyException {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException();
        for (int i = 0; i < properties.size(); i++)
            if (properties.get(i).getName().equals(name)) {
                properties.remove(i);
                return;
            }
        throw new UnknownPropertyException("Property not found: " + name);
    }
    
    @Override
    public Serializable getPropertyValue(String name) throws UnknownPropertyException {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException();
        for (PropertyI p : properties)
            if (p.getName().equals(name))
                return p.getValue();
        throw new UnknownPropertyException("Property not found: " + name);
    }
    
    @Override
    public PropertyI[] getProperties() {
        return  properties.toArray(new PropertyI[0]);
    }

    @Override
    public MessageI copy() {
        Message copy = new Message(this.timestamp, this.properties);
        copy.payload = this.payload;
        return copy;
    }

    @Override
    public void setPayload(Serializable payload) {
        if (payload == null)
            throw new IllegalArgumentException();
        this.payload = payload;
    }

    @Override
    public Serializable getPayload() {
        return this.payload;
    }

    @Override
    public Instant getTimeStamp() {
        return this.timestamp;
    }
}