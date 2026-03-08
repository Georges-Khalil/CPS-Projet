package fr.sorbonne_u.cps.pubsub;

import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestStep;
import fr.sorbonne_u.components.utils.tests.TestStepI;
import fr.sorbonne_u.cps.meteo.interfaces.MeteoAlertI;
import fr.sorbonne_u.cps.pubsub.composants.Broker;
import fr.sorbonne_u.cps.pubsub.composants.Bureau;
import fr.sorbonne_u.cps.pubsub.composants.Station;
import fr.sorbonne_u.cps.pubsub.composants.WindTurbine;
import fr.sorbonne_u.cps.pubsub.filters.*;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.message.Message;
import fr.sorbonne_u.cps.pubsub.meteo.MeteoAlert;
import fr.sorbonne_u.cps.pubsub.meteo.WindData;
import fr.sorbonne_u.exceptions.VerboseException;

import java.time.Instant;

/**
 * The purpose of this class is to not overcrowd CVM with many different scenarios,
 * Our first test scenarios should all be written here.
 */
public class Scenario {

    // Right now, everything is below the CVM, but all elements could just be fields from this class.
    // We could have a different class to this one, with a different clock configuration.

    // --- CLOCK CONFIGURATION ---
    /** for test scenarios a {@code Clock} is used to get a time-triggered
     * synchronisation among the actions of components.
     */
    public static String CLOCK_URI = "test-clock";
    /** start virtual instant in the test scenario, as a string to be parsed.
     */
    public static String START_INSTANT = "2026-03-10T09:00:00.00Z";
    /** end virtual instant in the test scenario, as a string to be parsed.
     */
    public static String END_INSTANT = "2026-03-10T09:10:00.00Z";
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

    /**
     * Simple test scenario that registers three components.
     */
    public static TestScenario simpleRegistrationTest(String windTurbine1_URI, String station1_URI, String bureau1_URI) {
        Instant startInstant = Instant.parse(START_INSTANT);
        Instant endInstant = Instant.parse(END_INSTANT);
        // test values
        String key1 = "x";
        Double value1 = 1000.0;
        // instant at which the first client component will register
        Instant turbinRegInstant = startInstant.plusSeconds(200);
        // instant at which the second client component will register
        Instant stationRegInstant = startInstant.plusSeconds(220);
        // instant at which the third client component will register
        Instant bureauRegInstant = startInstant.plusSeconds(240);
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

    /**
     * A more complete test scenario that involves registration, subscription, and publication.
     */
    public static TestScenario fullOperationTest(String windTurbine1_URI, String station1_URI, String bureau1_URI, String station2_URI) throws VerboseException
    {
        Instant startInstant = Instant.parse(START_INSTANT);
        Instant endInstant = Instant.parse(END_INSTANT);

        // instants for actions
        Instant bureauRegInstant = startInstant.plusSeconds(200);
        Instant turbineRegInstant = startInstant.plusSeconds(220);
        Instant stationRegInstant = startInstant.plusSeconds(240);
        Instant stationRegInstant2 = startInstant.plusSeconds(260);
        Instant stationPubInstant = startInstant.plusSeconds(280);

        return new TestScenario(
                CLOCK_URI,
                startInstant,
                endInstant,
                new TestStepI[] {
                        // --- BUREAU ---
                        new TestStep(
                                CLOCK_URI,
                                bureau1_URI,
                                bureauRegInstant,
                                owner -> {
                                    try {
                                        Bureau bureau = (Bureau) owner;
                                        bureau.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.PREMIUM);
                                        bureau.getPublicationPlugin().connectToPublishingPort(
                                                bureau.getRegistrationPlugin().getPublishingPortURI());
                                        bureau.traceMessage("Bureau : Registered as PREMIUM\n");
                                        // Create the weather alerts channel
                                        bureau.getPrivilegedPlugin().createChannel(Bureau.WEATHER_ALERTS_CHANNEL, ".*");
                                        bureau.traceMessage("Bureau : Created weather_alert_channel\n");
                                        bureau.getSubscriptionPlugin().subscribe(Broker.WIND_CHANNEL, new MessageFilter());
                                        bureau.traceMessage("Bureau : Subscribed to wind_channel\n");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }),

                        // --- WIND TURBINE ---
                        new TestStep(
                                CLOCK_URI,
                                windTurbine1_URI,
                                turbineRegInstant,
                                owner -> {
                                    try {
                                        WindTurbine wt = (WindTurbine) owner;
                                        wt.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
                                        wt.traceMessage("WindTurbine : Registered\n");
                                        wt.getSubscriptionPlugin().subscribe(Broker.WIND_CHANNEL, new MessageFilter());
                                        wt.traceMessage("Subscribed to wind_channel\n");

                                        // Subscribe to weather alerts with a specific filter
                                        MessageFilterI filter = new MessageFilter(
                                                new MessageFilterI.PropertyFilterI[] {
                                                        new PropertyFilter("type", new ComparableValueFilter("alert"))
                                                },
                                                new MessageFilterI.PropertiesFilterI[] {
                                                        new PropertiesFilter(new MultiValuesFilter<MeteoAlertI>(new String[] {"Data"},
                                                                args -> args.get(0).getLevel() != MeteoAlert.Level.GREEN))
                                                },
                                                TimeFilter.acceptAny()
                                        );
                                        wt.getSubscriptionPlugin().subscribe(Bureau.WEATHER_ALERTS_CHANNEL, filter);
                                        wt.traceMessage("WindTurbine : Subscribed to weather_alerts_channel with filter " + filter + "\n");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }),

                        // --- STATION ---
                        new TestStep(
                                CLOCK_URI,
                                station1_URI,
                                stationRegInstant,
                                owner -> {
                                    try {
                                        Station station = (Station) owner;
                                        station.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
                                        station.getPublicationPlugin().connectToPublishingPort(
                                                station.getRegistrationPlugin().getPublishingPortURI());
                                        station.traceMessage("Station1 : Registered\n");
                                        // Publish a test message with WindData as payload
                                        Message msg = new Message(new WindData(station.getPosition(), 10.0, 5.0));
                                        msg.putProperty("Type", "wind");
                                        msg.putProperty("ID", station.getUid());
                                        station.getPublicationPlugin().publish(Broker.WIND_CHANNEL, msg);
                                        station.traceMessage("Station1 : Publish a message on wind_channel - " + msg.getPayload() + "\n");

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }),
                        new TestStep(
                                CLOCK_URI,
                                station2_URI,
                                stationRegInstant2,
                                owner -> {
                                    try {
                                        Station station = (Station) owner;
                                        station.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
                                        station.getPublicationPlugin().connectToPublishingPort(
                                                station.getRegistrationPlugin().getPublishingPortURI());
                                        station.traceMessage("Station2 : Registered\n");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }),

                        // --- STATION PUBLICATION ---
                        new TestStep(
                                CLOCK_URI,
                                station1_URI,
                                stationPubInstant,
                                owner -> {
                                    try {
                                        Station station = (Station) owner;
                                        fr.sorbonne_u.cps.pubsub.message.Message msg = 
                                            new fr.sorbonne_u.cps.pubsub.message.Message(
                                                new fr.sorbonne_u.cps.pubsub.meteo.WindData(
                                                    new fr.sorbonne_u.cps.pubsub.meteo.Position(10, 20), 10.0, 5.0));
                                        msg.putProperty("Type", "wind");
                                        msg.putProperty("ID", 1); // fixed ID for simplicity in test
                                        station.getPublicationPlugin().publish(Broker.WIND_CHANNEL, msg);
                                        station.traceMessage("Publish a message on wind_channel - " + msg.getPayload() + "\n");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                })
                });
    }
}
