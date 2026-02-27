package fr.sorbonne_u.cps.pubsub.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class URIGenerator {

  private static AtomicInteger counter = new AtomicInteger(0);

  public static String getNew(Object o) {
    return o.getClass().getCanonicalName() + counter.getAndAdd(1);
  }

}
