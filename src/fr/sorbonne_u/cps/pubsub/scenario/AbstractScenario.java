package fr.sorbonne_u.cps.pubsub.scenario;

import fr.sorbonne_u.components.ComponentI;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public abstract class AbstractScenario {

    // Right now, everything is below the CVM, but all elements could just be fields from this class.
    // We could have a different class to this one, with a different clock configuration.

    public static final String clockURI = "test-clock";

    /** A fixed delay, making sure that all components have had time to be created and started
     *  The delay needs to be adjusted depending on how complex the starting phase is.
     */
    public final long startDelay;
    public final double accelerationFactor;
    public final Instant startInstant, endInstant;

    protected AbstractScenario(long startDelay, double accelerationFactor) {
        if (startDelay < 0 || accelerationFactor <= 0)
            throw new IllegalArgumentException();

        this.startDelay = startDelay;
        this.accelerationFactor = accelerationFactor;
        this.startInstant = Instant.now().plus(24, ChronoUnit.HOURS);
        this.endInstant = Instant.now().plus(25, ChronoUnit.HOURS);
    }

    protected interface ThrustLambda {
        void run(ComponentI owner) throws Exception;
    }

    protected static class Thrust implements ComponentI.FComponentTask {
        private final ThrustLambda lambda;
        Thrust(ThrustLambda lambda) {
            this.lambda = lambda;
        }
        public void run(ComponentI owner) {
            try {
                this.lambda.run(owner);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected Thrust thrust(ThrustLambda lambda) {
        return new Thrust(lambda);
    }
}
