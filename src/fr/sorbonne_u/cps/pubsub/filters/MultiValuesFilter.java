package fr.sorbonne_u.cps.pubsub.filters;

import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI;

import java.io.Serializable;
import java.util.ArrayList;

public class MultiValuesFilter<T> implements MessageFilterI.MultiValuesFilterI {

  @FunctionalInterface
  public interface Executable<T> {
    boolean execute(ArrayList<T> values);
  }

  final String[] names;
  final Executable<T> lambda;

  public MultiValuesFilter(String[] names, Executable<T> lambda) {
    if (names == null || lambda == null || names.length == 0)
      throw new IllegalArgumentException();
    for (String name : names)
      if (name == null || name.isEmpty())
        throw new IllegalArgumentException();
    this.names = names;
    this.lambda = lambda;
  }

  @Override
  public String[] getNames() {
    return this.names.clone();
  }

  @Override
  public boolean match(Serializable... values) {
    if (values == null)
      throw new IllegalArgumentException();

    if (values.length != this.names.length)
      return false;

    try {
      ArrayList<T> arr = new ArrayList<>();
      for (Serializable value : values)
        arr.add((T) value);
      return this.lambda.execute(arr);
    } catch (ClassCastException e) {
      return false;
    }
  }
}
