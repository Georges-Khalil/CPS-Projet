package fr.sorbonne_u.cps.pubsub.scenario;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestStep;
import fr.sorbonne_u.components.utils.tests.TestStepI;
import fr.sorbonne_u.cps.pubsub.composants.Broker;
import fr.sorbonne_u.cps.pubsub.composants.Station;
import fr.sorbonne_u.cps.pubsub.composants.WindTurbine;
import fr.sorbonne_u.cps.pubsub.filters.MessageFilter;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.message.Message;
import fr.sorbonne_u.cps.pubsub.meteo.Position;
import fr.sorbonne_u.cps.pubsub.meteo.WindData;

import java.time.Instant;

/**
 * Scenario testing unregistration and re-registration.
 * 
 * Scenario details:
 * 1. WindTurbine registers and subscribes to WIND_CHANNEL.
 * 2. Station publishes to WIND_CHANNEL (Received).
 * 3. WindTurbine unregisters.
 * 4. Station publishes to WIND_CHANNEL (Not received).
 * 5. WindTurbine re-registers and re-subscribes.
 * 6. Station publishes to WIND_CHANNEL (Received).
 *
 */
public class UnregisterRedeployScenario extends AbstractScenario {

    static void setupTurbine(WindTurbine wt) {
        try {
            wt.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
            wt.getSubscriptionPlugin().subscribe(Broker.WIND_CHANNEL, new MessageFilter());
            wt.traceMessage("WindTurbine: Registered and subscribed\n");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    static void stationPub(Station station, String label) {
        try {
            if (!station.getRegistrationPlugin().registered()) {
                station.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
                station.getPublicationPlugin().connectToPublishingPort(station.getRegistrationPlugin().getPublishingPortURI());
            }
            Message msg = new Message("Data: " + label);
            station.getPublicationPlugin().publish(Broker.WIND_CHANNEL, msg);
            station.traceMessage("Station: Published '" + label + "'\n");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    static void unregisterTurbine(WindTurbine wt) {
        try {
            wt.getRegistrationPlugin().unregister();
            wt.traceMessage("WindTurbine: Unregistered\n");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public UnregisterRedeployScenario(AbstractCVM cvm) throws Exception {
        super(3000L, 10.0);

        String turbineURI = "unreg-turbine";
        String stationURI = "unreg-station";
        TestScenario testScenario = this.getScenario(turbineURI, stationURI);

        AbstractComponent.createComponent(WindTurbine.class.getCanonicalName(), new Object[]{turbineURI, new Position(0, 0), testScenario});
        AbstractComponent.createComponent(Station.class.getCanonicalName(), new Object[]{stationURI, new Position(0, 0), testScenario});

        cvm.toggleTracing(turbineURI);
        cvm.toggleTracing(stationURI);
    }

    private TestScenario getScenario(String t1, String s1) {
        Instant start = this.startInstant;
        return new TestScenario(this.clockURI, start, this.endInstant, new TestStepI[]{
                new TestStep(this.clockURI, t1, start.plusSeconds(10), owner -> setupTurbine((WindTurbine) owner)),
                new TestStep(this.clockURI, s1, start.plusSeconds(20), owner -> stationPub((Station) owner, "FirstMsg")),
                new TestStep(this.clockURI, t1, start.plusSeconds(30), owner -> unregisterTurbine((WindTurbine) owner)),
                new TestStep(this.clockURI, s1, start.plusSeconds(40), owner -> stationPub((Station) owner, "SecondMsg")),
                new TestStep(this.clockURI, t1, start.plusSeconds(50), owner -> setupTurbine((WindTurbine) owner)),
                new TestStep(this.clockURI, s1, start.plusSeconds(60), owner -> stationPub((Station) owner, "ThirdMsg"))
        });
    }
}
