package fr.sorbonne_u.cps.pubsub.scenario;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public abstract class AbstractScenario {

    // Right now, everything is below the CVM, but all elements could just be fields from this class.
    // We could have a different class to this one, with a different clock configuration.

    public String clockURI = "test-clock";

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
}
