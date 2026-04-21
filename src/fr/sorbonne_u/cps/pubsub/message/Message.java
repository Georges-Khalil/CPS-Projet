package fr.sorbonne_u.cps.pubsub.message;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

import fr.sorbonne_u.cps.pubsub.exceptions.UnknownPropertyException;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class Message implements MessageI {
    private static final long serialVersionUID = 1L;
    
    private final Map<String, PropertyI> properties;
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
    
    public Message(Instant timestamp, Collection<PropertyI> properties) {
        this("", timestamp, properties);
    }

    public Message(Serializable payload, Instant timestamp, Collection<PropertyI> properties) {
        if (timestamp == null || properties == null || payload == null)
            throw new IllegalArgumentException();
        this.timestamp = timestamp;
        this.payload = payload;
        this.properties = new HashMap<>();
        for (PropertyI property : properties)
            putProperty(property.getName(), property.getValue());
    }
    
    @Override
    public boolean propertyExists(String name) {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException();
        return this.properties.containsKey(name);
    }
    
    @Override
    public void putProperty(String name, Serializable value) {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException();
        if (this.propertyExists(name))
            throw new RuntimeException("Property already exists");
        this.properties.put(name, new Property(name, value));
    }
    
    @Override
    public void removeProperty(String name) throws UnknownPropertyException {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException();
        if (this.properties.remove(name) == null)
            throw new UnknownPropertyException("Property not found: " + name);
    }
    
    @Override
    public Serializable getPropertyValue(String name) throws UnknownPropertyException {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException();
        if (!propertyExists(name))
            throw new UnknownPropertyException("Property not found: " + name);
        return this.properties.get(name);
    }
    
    @Override
    public PropertyI[] getProperties() {
        return  this.properties.values().toArray(new PropertyI[0]);
    }

    @Override
    public MessageI copy() {
        return new Message(this.payload, this.timestamp, this.properties.values());
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