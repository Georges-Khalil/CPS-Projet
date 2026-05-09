package fr.sorbonne_u.cps.pubsub.utils;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class URIGenerator {

    private final static AtomicInteger counter = new AtomicInteger(0),
                                       uid_counter = new AtomicInteger(0);

    private final static Random random = new Random();

    public static String getNew(String prefix) {
        return prefix + "-" + counter.getAndAdd(1) + "-" + random.nextLong();
    }

    public static int getUID() {
        return uid_counter.getAndAdd(1);
    }

}
