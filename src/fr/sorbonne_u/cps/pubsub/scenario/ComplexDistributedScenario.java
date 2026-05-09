package fr.sorbonne_u.cps.pubsub.scenario;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestStep;
import fr.sorbonne_u.components.utils.tests.TestStepI;
import fr.sorbonne_u.cps.pubsub.DistributedCVM;
import fr.sorbonne_u.cps.pubsub.components.Broker;
import fr.sorbonne_u.cps.pubsub.components.Bureau;
import fr.sorbonne_u.cps.pubsub.components.Station;
import fr.sorbonne_u.cps.pubsub.components.WindTurbine;
import fr.sorbonne_u.cps.pubsub.exceptions.AlreadyRegisteredException;
import fr.sorbonne_u.cps.pubsub.filters.ComparableValueFilter;
import fr.sorbonne_u.cps.pubsub.filters.MessageFilter;
import fr.sorbonne_u.cps.pubsub.filters.PropertyFilter;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.message.Message;
import fr.sorbonne_u.cps.pubsub.meteo.Position;
import fr.sorbonne_u.cps.pubsub.meteo.WindData;

import java.time.Instant;

/**
 * Scenario testing dynamic modification of filters.
 *
 * Scenario details:
 * 1. WindTurbine registers and subscribes to WIND_CHANNEL with a filter for force > 10.0.
 * 2. Station publishes message with force = 5.0 (Filtered out).
 * 3. Station publishes message with force = 15.0 (Received).
 * 4. WindTurbine modifies filter to force > 20.0.
 * 5. Station publishes message with force = 15.0 (Filtered out now).
 *
 */
public class ComplexDistributedScenario extends AbstractScenario {

    protected final static String ALERT_CHANNEL = "ALERT_CHAN";

    // ---------- SETUP ---------- //

    static void setupStation(Station station) throws Exception {
        station.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
        station.getPublicationPlugin().connectToPublishingPort(station.getRegistrationPlugin().getPublishingPortURI());
        station.traceMessage("Station: Registered\n");
    }

    static void setupBureau(Bureau bureau) throws Exception {
        bureau.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.PREMIUM);
        bureau.getPublicationPlugin().connectToPublishingPort(bureau.getRegistrationPlugin().getPublishingPortURI());
        MessageFilterI filter = new MessageFilter(
                new PropertyFilter("force", new ComparableValueFilter(10.0, ComparableValueFilter.Operator.GT)));
        bureau.getSubscriptionPlugin().subscribe(Broker.DEFAULT_PUBLIC_CHANNEL, filter);
        bureau.traceMessage("Bureau: Registered && Subscribed to wind with a filter force > 10\n");

        bureau.getPrivilegedPlugin().createChannel(ALERT_CHANNEL, "");
        bureau.traceMessage("Bureau: New channel created\n");
    }

    static void setupTurbine(WindTurbine wt) throws Exception {
        wt.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
        MessageFilterI filter = new MessageFilter(
                new PropertyFilter("force", new ComparableValueFilter(10.0, ComparableValueFilter.Operator.GT)));
        wt.getSubscriptionPlugin().subscribe(ALERT_CHANNEL, filter);
        wt.traceMessage("WindTurbine: Subscribed to wind with filter force > 10.0\n");
    }

    // ---------- ACTIONS ---------- //

    static void publishMessage(Station station, double force) throws Exception {
        Message msg = new Message(new WindData(station.getPosition(), force, 0.0));
        msg.putProperty("force", force);
        station.getPublicationPlugin().publish(Broker.DEFAULT_PUBLIC_CHANNEL, msg);
        station.traceMessage("Station: Published wind data with force=" + force + "\n");
    }

//    static void modifyFilter(WindTurbine wt) throws Exception {
//        MessageFilterI newFilter = new MessageFilter(
//                new PropertyFilter("force", new ComparableValueFilter(20.0, ComparableValueFilter.Operator.GT)));
//        wt.getSubscriptionPlugin().modifyFilter(Broker.DEFAULT_PUBLIC_CHANNEL, newFilter);
//        wt.traceMessage("WindTurbine: Modified filter to force > 20.0\n");
//    }

    public ComplexDistributedScenario(AbstractCVM cvm, String group, String[] groups) throws Exception {
        super(2000L, 10.0);
        assert groups.length == 3;

        String turbineURI = "filter-turbine";
        String stationURI = "filter-station";
        String bureauURI = "filter-bureau";
        TestScenario testScenario = this.getScenario(turbineURI, stationURI, bureauURI);

        if (group.equals(groups[0])) {
            AbstractComponent.createComponent(WindTurbine.class.getCanonicalName(), new Object[]{turbineURI, new Position(0, 0), testScenario});
            cvm.toggleTracing(turbineURI);
        } else if (group.equals(groups[1])) {
            AbstractComponent.createComponent(Station.class.getCanonicalName(), new Object[]{stationURI, new Position(0, 0), testScenario});
            cvm.toggleTracing(stationURI);
        } else if (group.equals(groups[2])) {
            AbstractComponent.createComponent(Bureau.class.getCanonicalName(), new Object[]{bureauURI, testScenario});
            cvm.toggleTracing(bureauURI);
        } else
            throw new RuntimeException("Unknow group : " + group);
    }

    private TestScenario getScenario(String t1, String s1, String b1) {
        Instant start = this.startInstant;
        return new TestScenario(clockURI, start, this.endInstant, new TestStepI[]{
                new TestStep(clockURI, s1, start.plusSeconds(10), thrust(o -> setupStation((Station) o))),
                new TestStep(clockURI, b1, start.plusSeconds(10), thrust(o -> setupBureau((Bureau) o))),
                new TestStep(clockURI, t1, start.plusSeconds(15), thrust(o -> setupTurbine((WindTurbine)o))),
                new TestStep(clockURI, s1, start.plusSeconds(20), thrust(o -> publishMessage((Station) o, 5.0))),
                new TestStep(clockURI, s1, start.plusSeconds(30), thrust(o -> publishMessage((Station) o, 15.0))),
//                new TestStep(clockURI, t1, start.plusSeconds(40), thrust(o -> modifyFilter((WindTurbine) o))),
                new TestStep(clockURI, s1, start.plusSeconds(50), thrust(o -> publishMessage((Station) o, 15.0))),
                new TestStep(clockURI, s1, start.plusSeconds(60), thrust(o -> publishMessage((Station) o, 25.0)))
        });
    }
}
