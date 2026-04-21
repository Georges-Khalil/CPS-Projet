package fr.sorbonne_u.cps.pubsub.filters;

import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.function.Predicate;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class MultiValuesFilter<T> implements MessageFilterI.MultiValuesFilterI {

    final String[] names;
    final Predicate<ArrayList<T>> lambda;

    public MultiValuesFilter(String[] names, Predicate<ArrayList<T>> lambda) {
        if (names == null || lambda == null || names.length == 0) throw new IllegalArgumentException();
        for (String name : names)
            if (name == null || name.isEmpty()) throw new IllegalArgumentException();
        this.names = names;
        this.lambda = lambda;
    }

    @Override
    public String[] getNames() {
        return this.names.clone();
    }

    @Override
    public boolean match(Serializable... values) {
        if (values == null) throw new IllegalArgumentException();

        if (values.length != this.names.length) return false;

        try {
            ArrayList<T> arr = new ArrayList<>();
            for (Serializable value : values)
                arr.add((T) value);
            // Use of an ArrayList to avoid modifications on the original array
            // & because a simple cast is impossible (Serializable != T)
            return this.lambda.test(arr);
        } catch (ClassCastException e) {
            return false;
        }
    }
}
