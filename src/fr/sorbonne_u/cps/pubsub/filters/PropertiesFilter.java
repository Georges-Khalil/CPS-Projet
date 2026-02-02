package fr.sorbonne_u.cps.pubsub.filters;

import java.io.Serializable;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI.PropertyI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.MultiValuesFilterI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.PropertiesFilterI;

public class PropertiesFilter implements PropertiesFilterI {
	private static final long serialVersionUID = 1L;
	
	private final MultiValuesFilterI multiValuesFilter;
	
	public PropertiesFilter(MultiValuesFilterI multiValuesFilter) {
		assert multiValuesFilter != null;
		this.multiValuesFilter = multiValuesFilter;
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
		assert properties != null && properties.length > 1;
		String[] names = multiValuesFilter.getNames();
		Serializable[] values = new Serializable[names.length];
        // On vérifie si on peut trouver les propriétés recherchées, et on en extrait les valeurs.
		for (int i = 0; i < names.length; i++) {
			String needed = names[i];
			boolean found = false;
			for (PropertyI property : properties) {
				if (property.getName().equals(needed)) {
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