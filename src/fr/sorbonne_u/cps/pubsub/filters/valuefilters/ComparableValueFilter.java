package fr.sorbonne_u.cps.pubsub.filters.valuefilters;

import java.io.Serializable;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.ValueFilterI;

public class ComparableValueFilter implements ValueFilterI {
	private static final long serialVersionUID = 1L;
	
	public enum Operator {
		LT,
		LE,
		EQ,
		GE,
		GT
	}
	
	private final Comparable<Object> referenceValue;
	
	private final Operator operator;
	
	public ComparableValueFilter(Comparable<Object> referenceValue, Operator operator) {
		assert referenceValue != null;
		assert operator != null;
		this.referenceValue = referenceValue;
		this.operator = operator;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean match(Serializable value) {
		if (!(value instanceof Comparable)) {
			return false;
		}
		
		try {
			int comparison = ((Comparable<Object>) value).compareTo(referenceValue);
			switch (operator) {
				case LT: return comparison < 0;
				case LE: return comparison <= 0;
				case EQ: return comparison == 0;
				case GE: return comparison >= 0;
				case GT: return comparison > 0;
				default: return false;
			}
		} catch (ClassCastException e) {
			return false;
		}
	}
}