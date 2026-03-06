package fr.sorbonne_u.cps.pubsub;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.utils.aclocks.ClocksServerOutboundPort;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * The purpose of this class is to not overcrowd CVM with many different scenarios,
 * Our first test scenarios should all be written here.
 */
public class Scenario {

    // Right now, everything is bellow the CVM, but all elements could just be fields from this class.
    // We could have a different class to this one, with a different clock configuration.

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



    // --- NOTES I'VE BEEN TAKING, I'LL REMOVE THEM LATER ---
//    // Instant de démarrage
//    Instant i0 = Instant.parse("2026-02-23T09:00:00.00Z");
//
//    // Création d'une horloge
//    public static final String CLOCK_URI = "horloge-101";
//    AcceleratedClock ac =
//            new AcceleratedClock(
//                    CLOCK_URI,      // URI attribuée à l’horloge
//                    // t0,          // moment du démarrage en temps réel Unix (nous allons préférer utiliser un instant)
//                    i0,             // instant de démarrage du scénario
//                    60.0);          // facteur d’accélération
//
//
//    // Exemple dans le cahier des charge
//    public static final String TEST_CLOCK_URI = "test-clock";
//    public static final Instant START_INSTANT =
//            Instant.parse("2024-01-31T09:00:00.00Z");
//    protected static final long START_DELAY = 3000L;
//    public static final double ACCELERATION_FACTOR = 60.0;
//    long unixEpochStartTimeInNanos =
//            TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis() + START_DELAY);
//
//    // A faire dans le CVM pour créer l'horloge, avec des paramètres modulables
//AbstractComponent.createComponent(
//        ClocksServer.class.getCanonicalName(),
//    new Object[]{
//            TEST_CLOCK_URI,
//                    // URI attribuée à l’horloge
//                    unixEpochStartTimeInNanos, // moment du démarrage en temps réel Unix
//                    START_INSTANT,
//                    // instant de démarrage du scénario
//                    ACCELERATION_FACTOR});
//    // facteur d’acccélération
//
//
//    // Ensuite, chaque composant qui veut se synchroniser sur cette horloge va devoir se connecter au
//    //serveur d’horloge, récupérer une copie de l’horloge en utilisant son URI puis l’utiliser pour faire
//    //sa planification :
//    ClocksServerOutboundPort p = new ClocksServerOutboundPort(this);
//    p.publishPort();
//    this.doPortConnection(
//            p.getPortURI(),
//            ClocksServer.STANDARD_INBOUNDPORT_URI,
//            ClocksServerConnector.class.getCanonicalName()
//    );
//    AcceleratedClock ac = p.getClock(TEST_CLOCK_URI);
//    this.doPortDisconnection(p.getPortURI());
//    // On peut unpublish directement dès qu'on a l'horloge.
//    p.unpublishPort();
//    p.destroyPort();
//    // toujours faire waitUntilStart avant d’utiliser l’horloge pour
//    // calculer des moments et instants
//    ac.waitUntilStart();
}
