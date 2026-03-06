package fr.sorbonne_u.cps.pubsub;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestStep;
import fr.sorbonne_u.components.utils.tests.TestStepI;
import fr.sorbonne_u.cps.pubsub.composants.Broker;
import fr.sorbonne_u.cps.pubsub.composants.Bureau;
import fr.sorbonne_u.cps.pubsub.composants.WindTurbine;
import fr.sorbonne_u.cps.pubsub.composants.Station;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.meteo.Position;
import fr.sorbonne_u.cps.pubsub.meteo.Region;
import fr.sorbonne_u.exceptions.VerboseException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

import java.io.Serializable;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class CVM extends AbstractCVM {

    public CVM() throws Exception {
        super();
    }

    @Override
    public void deploy() throws Exception {

        // ----- Creation of the components -----

        String broker = AbstractComponent.createComponent(
                Broker.class.getCanonicalName(),
                new Object[]{}
        );

        String windTurbine1 = AbstractComponent.createComponent(
                WindTurbine.class.getCanonicalName(),
                new Object[]{new Position(20, 30)}
        );

        String station1 = AbstractComponent.createComponent(
                Station.class.getCanonicalName(),
                new Object[]{new Position(0, 0)}
        );

        String station2 = AbstractComponent.createComponent(
                Station.class.getCanonicalName(),
                new Object[]{new Position(50, 30)}
        );

        String bureau1 = AbstractComponent.createComponent(
                Bureau.class.getCanonicalName(),
                new Object[]{}
        );

        // ----- Creation of the clock -----

        // the start time of the scenario is defined relative to the current time
        long current = System.currentTimeMillis();

        // start time of the components in Unix epoch time in nanoseconds
        long unixEpochStartTimeInNanos = TimeUnit.MILLISECONDS.toNanos(current + DELAY_TO_START);

        // start instant used for time-triggered synchronisation in the test scenario
        Instant startInstant = Instant.parse(START_INSTANT);

        // create the clock server and the clock used to synchronise the
        // components actions in the test scenario
        String clock = AbstractComponent.createComponent(
                ClocksServer.class.getCanonicalName(),
                new Object[] {
                        CLOCK_URI, // must use the same in the test scenario
                        unixEpochStartTimeInNanos,
                        startInstant, // idem
                        ACCELERATION_FACTOR
                });

        // ----- Enable tracing -----
        this.toggleTracing(broker);
        this.toggleTracing(windTurbine1);
        this.toggleTracing(station1);
        this.toggleTracing(station2);
        this.toggleTracing(bureau1);
        this.toggleTracing(clock);

        // The connexions are dynamically created

        super.deploy();
    }

    public static void main(String[] args) throws Exception {
        CVM cvm = new CVM();
        cvm.startStandardLifeCycle(30000L);
        System.exit(0);
    }

    // --------------
    // Test-scenarios
    // --------------

    // --- CLOCK CONFIGURATION ---
    /** for test scenarios a {@code Clock} is used to get a time-triggered
     * synchronisation among the actions of components.
     */
    public static String CLOCK_URI = "test-clock";
    /** start virtual instant in the test scenario, as a string to be parsed.
     */
    public static String START_INSTANT = "2026-01-30T09:00:00.00Z";
    /** end virtual instant in the test scenario, as a string to be parsed.
     */
    public static String END_INSTANT = "2026-01-30T09:02:00.00Z";
    /** A fixed delay, making sure that all components have had time to be created and started
     *  The delay needs to be adjusted depending on how complex the starting phase is.
     */
    protected static final long DELAY_TO_START = 3000L;
    /** the acceleration factor applied to the {@code Instant} time
     * reference to run the test faster or slower than real time.
     * THIS NEEDS TO BE MANIPULATED CAREFULLY (Specifications Appendix A)
     */
    public static double ACCELERATION_FACTOR = 60.0;


    // --- Scenarios ---
    public static TestScenario simpleRegistrationTest(String windTurbine1_URI, String station1_URI, String bureau1_URI) throws VerboseException
    {
        Instant startInstant = Instant.parse(START_INSTANT);
        Instant endInstant = Instant.parse(END_INSTANT);
        // test values
        String key1 = "x";
        Double value1 = 1000.0;
        // instant at which the first client component will register
        Instant turbinRegInstant = startInstant.plusSeconds(10);
        // instant at which the second client component will register
        Instant stationRegInstant = startInstant.plusSeconds(20);
        // instant at which the third client component will register
        Instant bureauRegInstant = startInstant.plusSeconds(30);
        return new TestScenario(
                CLOCK_URI,
                // URI of the clock used to synchronise
                startInstant,
                // start instant on which actions times are based
                endInstant,
                // end instant, the upper bound for actions times
                new TestStepI[] {
                        new TestStep(
                                CLOCK_URI,
                                windTurbine1_URI,
                                // URI of the component performing the action
                                turbinRegInstant,
                                // instant at which the action will be executed
                                owner -> {
                                    // action, as a lambda expression
                                    try {
                                        ((WindTurbine)owner).getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
                                        ((WindTurbine)owner).traceMessage(
                                                "Turbine1 : Registered.\n");
                                    } catch (Exception e) {
                                        System.err.println("WindTurbine1 registration test failed with exception " + e);
                                    }
                                }),
                        new TestStep(
                                CLOCK_URI,
                                station1_URI,
                                // URI of the component performing the action
                                stationRegInstant,
                                // instant at which the action will be executed
                                owner -> {
                                    // action, as a lambda expression
                                    try {
                                        ((Station) owner).getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
                                        owner.traceMessage("Station1 : Registered.\n");
                                        // Serializable v = ((DataStoreClient)owner).getTestAction(key1);
                                        // assert value1.equals(v) : "get test failed!"; on peut assert pour verifier la valeur des messages entre autre
                                    } catch (Exception e) {
                                        System.err.println("Station1 registration test failed with exception " + e);
                                    }
                                }),
                        new TestStep(
                                CLOCK_URI,
                                bureau1_URI, // URI of the component performing the action
                                bureauRegInstant, // instant at which the action will be executed
                                owner -> {
                                    // action, as a lambda expression
                                    try {
                                        ((Bureau) owner).getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
                                        owner.traceMessage("Bureau1 : Registered.\n");
//                                        Serializable v = ((DataStoreClient)owner).getOutPort().remove(key1);
//                                        assert value1.equals(v) : "remove test failed!";
//                                        ((DataStoreClient)owner).traceMessage(
//                                                " removes " + key1 + " getting value " + value1 + "\n");
                                    } catch (Exception e) {
                                        System.err.println("Bureau1 registration test failed with exception " + e);
                                    }
                                })
                });
    }
}
