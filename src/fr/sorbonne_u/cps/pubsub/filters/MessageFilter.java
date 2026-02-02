package fr.sorbonne_u.cps.pubsub.filters;

import java.util.ArrayList;
import java.util.List;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI.PropertyI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI;

public class MessageFilter implements MessageFilterI {
	private static final long serialVersionUID = 1L;
	
	private final PropertyFilterI[] propertyFilters;
	private final PropertiesFilterI[] propertiesFilters;
	private final TimeFilterI timeFilter;
	
	public MessageFilter(PropertyFilterI[] propertyFilters, 
	                     PropertiesFilterI[] propertiesFilters, 
	                     TimeFilterI timeFilter) {
		this.propertyFilters = propertyFilters != null ? propertyFilters : new PropertyFilterI[0];
		this.propertiesFilters = propertiesFilters != null ? propertiesFilters : new PropertiesFilterI[0];
		this.timeFilter = timeFilter != null ? timeFilter : TimeFilter.wildcard();
	}
	
	public MessageFilter(PropertyFilterI... propertyFilters) {
		this(propertyFilters, null, null);
	}
	
	public MessageFilter(TimeFilterI timeFilter, PropertyFilterI... propertyFilters) {
		this(propertyFilters, null, timeFilter);
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
		if (message == null) {
			return false;
		}
		
		if (!timeFilter.match(message.getTimeStamp())) {
			return false;
		}
		
		PropertyI[] messageProperties = message.getProperties();
		
		for (PropertyFilterI propertyFilter : propertyFilters) {
			boolean found = false;
			for (PropertyI property : messageProperties) {
				if (propertyFilter.match(property)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		
		for (PropertiesFilterI propertiesFilter : propertiesFilters) {
			String[] requiredNames = propertiesFilter.getMultiValuesFilter().getNames();
			
			List<PropertyI> relevantProperties = new ArrayList<>();
			for (PropertyI property : messageProperties) {
				for (String name : requiredNames) {
					if (property.getName().equals(name)) {
						relevantProperties.add(property);
						break;
					}
				}
			}
			
			if (relevantProperties.size() != requiredNames.length) {
				return false;
			}
			
			if (!propertiesFilter.match(relevantProperties.toArray(new PropertyI[0]))) {
				return false;
			}
		}
		
		return true;
	}
}