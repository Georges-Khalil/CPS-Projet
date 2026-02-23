package fr.sorbonne_u.cps.pubsub.filters;

import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI;

import java.io.Serializable;
import java.lang.reflect.Array;

public class MultiValuesFilter<T> implements MessageFilterI.MultiValuesFilterI {

  @FunctionalInterface
  public interface Executable<T> {
    boolean execute(T... values);
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
      return this.lambda.execute((T[]) values);
    } catch (ClassCastException e) {
      return false;
    }
  }
}
