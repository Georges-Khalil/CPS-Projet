package fr.sorbonne_u.cps.pubsub.filters;

import java.time.Instant;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.TimeFilterI;

/**
 * It's an abstract class that we can use by getting a complete object with acceptAfter / Before / Between / Any
 * The method 'match' is override according to our goal
 *
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public abstract class TimeFilter implements TimeFilterI {
    private static final long serialVersionUID = 1L;
    
    public static TimeFilterI acceptAfter(Instant from) {
        if (from == null)
            throw new IllegalArgumentException();
        return new TimeFilterI() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean match(Instant timestamp) {
                if (timestamp == null)
                    throw new IllegalArgumentException();
                return !timestamp.isBefore(from);
            }
        };
    }
    
    public static TimeFilterI acceptBefore(Instant to) {
        if (to == null)
            throw new IllegalArgumentException();
        return new TimeFilterI() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean match(Instant timestamp) {
                if (timestamp == null)
                  throw new IllegalArgumentException();
                return !timestamp.isAfter(to);
            }
        };
    }
    
    public static TimeFilterI acceptBetween(Instant from, Instant to) {
        if (from == null || to == null || to.isBefore(from))
            throw new IllegalArgumentException();
        return new TimeFilterI() {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean match(Instant timestamp) {
                if (timestamp == null)
                  throw new IllegalArgumentException();
                return !timestamp.isBefore(from) && !timestamp.isAfter(to);
            }
        };
    }
    
    public static TimeFilterI acceptAny() {
        return new TimeFilterI() {
            private static final long serialVersionUID = 1L;

        	@Override
			public boolean match(Instant timestamp) {
                if (timestamp == null)
                    throw new IllegalArgumentException();
                return true;
			}
		};
	}
}