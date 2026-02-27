package fr.sorbonne_u.cps.pubsub.filters;

import java.io.Serializable;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.ValueFilterI;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class ComparableValueFilter implements ValueFilterI {
    private static final long serialVersionUID = 1L;
    
    public enum Operator {
        EQ, NE, LT, LE, GT, GE
    }
    
    private final Serializable referenceValue;
    private final Operator operator;

    public ComparableValueFilter(Serializable referenceValue, Operator operator) {
        if (referenceValue == null || operator == null ||
                (operator != Operator.EQ && operator != Operator.NE && !(referenceValue instanceof Comparable)))
            throw new IllegalArgumentException();
        this.referenceValue = referenceValue;
        this.operator = operator;
    }

    public ComparableValueFilter(Serializable referenceValue) {
    this(referenceValue, Operator.EQ);
  }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean match(Serializable value) {
        if (value == null)
            throw new IllegalArgumentException();

        if (this.operator == Operator.EQ || this.operator == Operator.NE)
            return this.referenceValue.equals(value) ^ (this.operator == Operator.NE);

        int comparison = ((Comparable<Object>) value).compareTo(this.referenceValue);
        switch (operator) {
              case LT: return comparison < 0;
              case LE: return comparison <= 0;
              case GE: return comparison >= 0;
              case GT: return comparison > 0;
              default: return false;
        }
    }
}