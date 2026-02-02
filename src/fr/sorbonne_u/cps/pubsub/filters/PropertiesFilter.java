package fr.sorbonne_u.cps.pubsub.filters;

import java.io.Serializable;
import java.util.Objects;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI.PropertyI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.MultiValuesFilterI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.PropertiesFilterI;

public class PropertiesFilter implements PropertiesFilterI {
	private static final long serialVersionUID = 1L;
	
	private final MultiValuesFilterI multiValuesFilter;
	
	public PropertiesFilter(MultiValuesFilterI multiValuesFilter) {
		this.multiValuesFilter = Objects.requireNonNull(multiValuesFilter, "multiValuesFilter must not be null");
	}
	
	@Override
	public MultiValuesFilterI getMultiValuesFilter() {
		return this.multiValuesFilter;
	}

    /**
     * 'Property...' nous permet de match cette fonction avec une property, deux, trois ou alors une liste, une array
     */
	@Override
	public boolean match(PropertyI... properties) {
		if (properties == null) {
			return false;
		}
		String[] names = multiValuesFilter.getNames();
		if (names == null || names.length == 0) {
			return false;
		}
		Serializable[] values = new Serializable[names.length];
        // On vérifie si on peut trouver les propriétés recherchées, et on en extrait les valeurs.
		for (int i = 0; i < names.length; i++) {
			String needed = names[i];
			if (needed == null) {
				return false;
			}
			boolean found = false;
			for (PropertyI property : properties) {
				if (property == null) continue;
				String propName = property.getName();
				if (needed.equals(propName)) {
					values[i] = property.getValue();
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
        // Todo: implémenter un filtre spécifiques aux comparaisons qu'on veut faire entre différentes propriétés.
		return multiValuesFilter.match(values);
	}
}