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
	
	public static TimeFilterI from(Instant from) {
		return new TimeFilterI() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public boolean match(Instant timestamp) {
				assert timestamp != null;
				return !timestamp.isBefore(from);
			}
		};
	}
	
	public static TimeFilterI to(Instant to) {
		return new TimeFilterI() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public boolean match(Instant timestamp) {
				assert timestamp != null;
				return !timestamp.isAfter(to);
			}
		};
	}
	
	public static TimeFilterI between(Instant from, Instant to) {
		return new TimeFilterI() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public boolean match(Instant timestamp) {
				assert timestamp != null;
				return !timestamp.isBefore(from) && !timestamp.isAfter(to);
			}
		};
	}
	
	public static TimeFilterI joker() {
		return new TimeFilterI() {
			private static final long serialVersionUID = 1L;

            // TODO: A voir, pour l'instant on accepte toujours comme s'il n'y avait pas de timefilter.
			@Override
			public boolean match(Instant timestamp) {
				return true;
			}
		};
	}
}