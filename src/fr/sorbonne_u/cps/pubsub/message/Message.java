package fr.sorbonne_u.cps.pubsub.message;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import fr.sorbonne_u.cps.pubsub.exceptions.UnknownPropertyException;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;

public class Message implements MessageI {
	private static final long serialVersionUID = 1L;
	
	private final List<PropertyI> properties;
	
	private Serializable payload;
	
	private final Instant timestamp;
	
	public Message() {
		this.properties = new ArrayList<>();
		this.timestamp = Instant.now();
		this.payload = null;
	}

  public Message(Serializable payload, Instant timestamp) {
    this.timestamp = timestamp;
    this.properties = new ArrayList<>();
    this.payload = payload;
  }

  public Message(Serializable payload) {
    this.timestamp = Instant.now();
    this.properties = new ArrayList<>();
    this.payload = payload;
  }
	
	public Message(Instant timestamp, List<PropertyI> properties) {
		this.timestamp = timestamp;
		this.properties = new ArrayList<>(properties);
		this.payload = null;
	}
	
	@Override
	public boolean propertyExists(String name) {
		if (name == null || name.isEmpty()) return false;
		for (PropertyI p : properties) {
			if (p.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void putProperty(String name, Serializable value) {
		assert name != null && !name.isEmpty();
		assert !propertyExists(name);
		properties.add(new Property(name, value));
	}
	
	@Override
	public void removeProperty(String name) throws UnknownPropertyException {
		assert name != null && !name.isEmpty();
		for (int i = 0; i < properties.size(); i++) {
			if (properties.get(i).getName().equals(name)) {
				properties.remove(i);
				return;
			}
		}
		throw new UnknownPropertyException("Property not found: " + name);
	}
	
	@Override
	public Serializable getPropertyValue(String name) throws UnknownPropertyException {
		assert name != null && !name.isEmpty();
		for (PropertyI p : properties) {
			if (p.getName().equals(name)) {
				return p.getValue();
			}
		}
		throw new UnknownPropertyException("Property not found: " + name);
	}
	
	@Override
	public PropertyI[] getProperties() {
		return properties.toArray(new PropertyI[0]);
	}
	
	@Override
	public MessageI copy() {
		Message copy = new Message(this.timestamp, this.properties);
		copy.payload = this.payload;
		return copy;
	}
	
	@Override
	public void setPayload(Serializable payload) {
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