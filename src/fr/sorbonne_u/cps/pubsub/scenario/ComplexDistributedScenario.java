package fr.sorbonne_u.cps.pubsub.scenario;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestStep;
import fr.sorbonne_u.components.utils.tests.TestStepI;
import fr.sorbonne_u.cps.meteo.interfaces.MeteoAlertI;
import fr.sorbonne_u.cps.pubsub.DistributedCVM;
import fr.sorbonne_u.cps.pubsub.components.Broker;
import fr.sorbonne_u.cps.pubsub.components.Bureau;
import fr.sorbonne_u.cps.pubsub.components.Station;
import fr.sorbonne_u.cps.pubsub.components.WindTurbine;
import fr.sorbonne_u.cps.pubsub.exceptions.AlreadyRegisteredException;
import fr.sorbonne_u.cps.pubsub.filters.AlertLevelFilter;
import fr.sorbonne_u.cps.pubsub.filters.ComparableValueFilter;
import fr.sorbonne_u.cps.pubsub.filters.MessageFilter;
import fr.sorbonne_u.cps.pubsub.filters.PropertyFilter;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.message.Message;
import fr.sorbonne_u.cps.pubsub.meteo.Position;
import fr.sorbonne_u.cps.pubsub.meteo.WindData;
import fr.sorbonne_u.cps.pubsub.meteo.messages.WindMessage;

import java.time.Instant;

/**
 * Scenario testing dynamic modification of filters.
 *
 * Scenario details:
 * 1. Setup components
 * 2. Turbines 1, 2 and 3 receive 3 messages
 * 4. Turbine 1 modifies filter to level RED required
 * 2. Turbines 2 and 3 receive 4 messages but the turbine 1 receives only 1 message (force = 130)
 *
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class ComplexDistributedScenario extends AbstractScenario {

    // ---------- SETUP ---------- //

    static void setupStation(Station station) throws Exception {
        station.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
        station.getPublicationPlugin().connectToPublishingPort(station.getRegistrationPlugin().getPublishingPortURI());
        station.traceMessage("Station: Registered\n");
    }

    static void setupBureau(Bureau bureau) throws Exception {
        bureau.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.PREMIUM);
        bureau.getPublicationPlugin().connectToPublishingPort(bureau.getRegistrationPlugin().getPublishingPortURI());
        bureau.getSubscriptionPlugin().subscribe(Broker.DEFAULT_PUBLIC_CHANNEL, Bureau.WindFilter);
        bureau.traceMessage("Bureau: Registered && Subscribed to wind with a filter force > 10\n");

        bureau.getPrivilegedPlugin().createChannel(Bureau.WEATHER_ALERTS_CHANNEL, ".*");
        bureau.traceMessage("Bureau: New channel created\n");
    }

    static void setupTurbine(WindTurbine wt) throws Exception {
        wt.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
        MessageFilterI filter = new MessageFilter(
                new PropertyFilter("Type", new ComparableValueFilter("Alert")),
                new PropertyFilter("AlertLevel", new AlertLevelFilter(MeteoAlertI.Level.GREEN))
        );
        wt.getSubscriptionPlugin().subscribe(Bureau.WEATHER_ALERTS_CHANNEL, filter);
        wt.traceMessage("WindTurbine: Subscribed to alerts with filter level required >= GREEN\n");
    }

    // ---------- ACTIONS ---------- //

    static void publishMessage(Station station, double force) throws Exception {
        MessageI msg = new WindMessage(station, force, 0.0);
        station.getPublicationPlugin().publish(Broker.DEFAULT_PUBLIC_CHANNEL, msg);
        station.traceMessage("Station: Published wind data with force=" + force + "\n");
    }

    static void modifyFilter(WindTurbine wt, MeteoAlertI.Level level) throws Exception {
        MessageFilterI filter = new MessageFilter(
                new PropertyFilter("AlertLevel", new AlertLevelFilter(level)));
        wt.getSubscriptionPlugin().modifyFilter(Bureau.WEATHER_ALERTS_CHANNEL, filter);
        wt.traceMessage("WindTurbine: Modify filter with level required >= " + level.name() + "\n");
    }

    public ComplexDistributedScenario(AbstractCVM cvm, String group, String[] groups) throws Exception {
        super(2000L, 10.0);
        assert groups.length == 3;

        String t1 = "turbine1", t2 = "turbine2", t3 = "turbine3";
        String s1 = "station1", s2 = "station2";
        String b1 = "bureau1";
        TestScenario testScenario = this.getScenario(t1, t2, t3, s1, s2, b1);

        if (group.equals(groups[0])) {
            AbstractComponent.createComponent(WindTurbine.class.getCanonicalName(), new Object[]{t1, new Position(0, 150), testScenario});
            AbstractComponent.createComponent(WindTurbine.class.getCanonicalName(), new Object[]{t2, new Position(70, 40), testScenario});
            cvm.toggleTracing(t1);
            cvm.toggleTracing(t2);
        } else if (group.equals(groups[1])) {
            AbstractComponent.createComponent(WindTurbine.class.getCanonicalName(), new Object[]{t3, new Position(150, 0), testScenario});
            AbstractComponent.createComponent(Station.class.getCanonicalName(), new Object[]{s1, new Position(0, 0), testScenario});
            cvm.toggleTracing(t3);
            cvm.toggleTracing(s1);
        } else if (group.equals(groups[2])) {
            AbstractComponent.createComponent(Station.class.getCanonicalName(), new Object[]{s2, new Position(40, 23), testScenario});
            AbstractComponent.createComponent(Bureau.class.getCanonicalName(), new Object[]{b1, testScenario});
            cvm.toggleTracing(s2);
            cvm.toggleTracing(b1);
        } else
            throw new RuntimeException("Unknow group : " + group);
    }

    private TestScenario getScenario(String t1, String t2, String t3, String s1, String s2, String b1) {
        Instant start = this.startInstant;
        return new TestScenario(clockURI, start, this.endInstant, new TestStepI[]{
                new TestStep(clockURI, b1, start.plusSeconds(10), thrust(o -> setupBureau((Bureau) o))),
                new TestStep(clockURI, s1, start.plusSeconds(15), thrust(o -> setupStation((Station) o))),
                new TestStep(clockURI, s2, start.plusSeconds(15), thrust(o -> setupStation((Station) o))),
                new TestStep(clockURI, t1, start.plusSeconds(15), thrust(o -> setupTurbine((WindTurbine)o))),
                new TestStep(clockURI, t2, start.plusSeconds(15), thrust(o -> setupTurbine((WindTurbine)o))),
                new TestStep(clockURI, t3, start.plusSeconds(15), thrust(o -> setupTurbine((WindTurbine)o))),

                new TestStep(clockURI, s1, start.plusSeconds(20), thrust(o -> publishMessage((Station) o, 5.0))),
                new TestStep(clockURI, s1, start.plusSeconds(30), thrust(o -> publishMessage((Station) o, 15.0))),
                new TestStep(clockURI, s2, start.plusSeconds(35), thrust(o -> publishMessage((Station) o, 50))),

                new TestStep(clockURI, t1, start.plusSeconds(50), thrust(o -> modifyFilter((WindTurbine) o, MeteoAlertI.Level.RED))),

                new TestStep(clockURI, s2, start.plusSeconds(60), thrust(o -> publishMessage((Station) o, 15.0))),
                new TestStep(clockURI, s1, start.plusSeconds(65), thrust(o -> publishMessage((Station) o, 69.0))),
                new TestStep(clockURI, s2, start.plusSeconds(68), thrust(o -> publishMessage((Station) o, 130.0))),
                new TestStep(clockURI, s1, start.plusSeconds(83), thrust(o -> publishMessage((Station) o, 11.0)))
        });
    }
}
