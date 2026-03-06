package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.utils.aclocks.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class TestComponent extends AbstractComponent {

    private TestScenario testScenario;

    TestComponent(TestScenario testScenario) {
        // As specified in the specifications, each component has their test scenario given in the constructor like here.
        super(1, 0);
        this.testScenario = testScenario;
    }

    /**
     * Method execute() as expected for testing each component on their testScenario.
     * Almost all components should have an execute() that looks like this.
     * @throws Exception
     */
    public void execute() throws Exception {
        // initialise the clock with the given URI, retrieving it from the clock server;
        // this must be done by the component before running the test scenario
        this.initialiseClock(ClocksServer.STANDARD_INBOUNDPORT_URI, this.testScenario.getClockURI());
        // make the component execute its actions in the test scenario
        this.executeTestScenario(this.testScenario);
    }


    // I've been trying out stuff, but I'll delete it later.
    /**
     *   Method we won't need, I'll delete it later when I am sure of it.
     */
    protected AcceleratedClock obtainClock(int ClockNumber) throws Exception {
        ClocksServerOutboundPort p = new ClocksServerOutboundPort(this);
        p.publishPort();
        this.doPortConnection(
                p.getPortURI(),
                ClocksServer.STANDARD_INBOUNDPORT_URI,
                ClocksServerConnector.class.getCanonicalName()
        );
        AcceleratedClock ac = p.getClock(this.horloge_uris.get(ClockNumber));
        this.doPortDisconnection(p.getPortURI());
        // On peut unpublish directement dès qu'on a l'horloge.
        p.unpublishPort();
        p.destroyPort();
        // toujours faire waitUntilStart avant d’utiliser l’horloge pour
        // calculer des moments et instants
        ac.waitUntilStart();
        return ac;
    }

    public static final String HORLOGE_URI = "test-clock";
    public static final Instant START_INSTANT =
            Instant.parse("2024-01-31T09:00:00.00Z");
    protected static final long START_DELAY = 3000L;
    public static final double ACCELERATION_FACTOR = 60.0;
    long unixEpochStartTimeInNanos =
            TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis() + START_DELAY);
    private HashMap<Integer, String> horloge_uris;
}
