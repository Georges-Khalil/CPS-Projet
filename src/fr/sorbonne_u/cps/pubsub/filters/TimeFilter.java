package fr.sorbonne_u.cps.pubsub.filters;

import java.time.Instant;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.TimeFilterI;

/**
 * La classe ici est abstraite, mais en créant des objets TimeFilter avec from(), to(),
 *  between() ou wildcard(), un objet 'complet' est rendu avec une implémentation différente
 *  de la méthode match().
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