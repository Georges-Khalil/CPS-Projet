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
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.message.Message;
import fr.sorbonne_u.cps.pubsub.meteo.Position;
import fr.sorbonne_u.cps.pubsub.meteo.WindData;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Future;

/**
 * Timed scenario dedicated to audit 2.
 * Exercises concurrent publications together with the advanced reception
 * methods implemented in the subscription plugin.
 *
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class AsyncAuditScenario extends AbstractScenario {

    protected static void registerAndSubscribe(WindTurbine windTurbine) {
        try {
            windTurbine.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
            windTurbine.getSubscriptionPlugin().subscribe(Broker.WIND_CHANNEL, new MessageFilter());
            windTurbine.traceMessage("Async turbine registered and subscribed\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static void registerStation(Station station) {
        try {
            station.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
            station.getPublicationPlugin().connectToPublishingPort(
                    station.getRegistrationPlugin().getPublishingPortURI());
            station.traceMessage("Station ready for async audit\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static void publishWind(Station station, double x, double y, String label) {
        try {
            Message message = new Message(new WindData(station.getPosition(), x, y));
            message.putProperty("Type", "wind");
            message.putProperty("ID", station.getUid());

            station.traceMessage(label + " publish start\n");
            station.getPublicationPlugin().publish(Broker.WIND_CHANNEL, message);
            station.traceMessage(label + " publish returned\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static void waitForNextMessage(WindTurbine windTurbine) {
        try {
            windTurbine.traceMessage("waitForNextMessage started\n");
            MessageI message = windTurbine.getSubscriptionPlugin().waitForNextMessage(Broker.WIND_CHANNEL);
            windTurbine.traceMessage(
                    "waitForNextMessage returned payload=" + message.getPayload() + "\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static void futureNextMessage(WindTurbine windTurbine) {
        try {
            windTurbine.traceMessage("getNextMessage started\n");
            Future<MessageI> future = windTurbine.getSubscriptionPlugin().getNextMessage(Broker.WIND_CHANNEL);
            windTurbine.traceMessage("getNextMessage returned a future\n");
            MessageI message = future.get();
            windTurbine.traceMessage("Future completed payload=" + message.getPayload() + "\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static void timedWait(WindTurbine windTurbine) {
        try {
            windTurbine.traceMessage("timed wait started\n");
            MessageI message = windTurbine.getSubscriptionPlugin().waitForNextMessage(
                    Broker.WIND_CHANNEL,
                    Duration.ofSeconds(2));
            windTurbine.traceMessage(
                    "timed wait returned " + (message == null ? "null" : message.getPayload()) + "\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AsyncAuditScenario(AbstractCVM cvm) throws Exception {
        super(5000L, 20.0);

        String windTurbineURI = "async-turbine";
        String station1URI = "async-station-1";
        String station2URI = "async-station-2";

        TestScenario testScenario = this.getScenario(windTurbineURI, station1URI, station2URI);

        AbstractComponent.createComponent(
                WindTurbine.class.getCanonicalName(),
                new Object[] {windTurbineURI, new Position(20, 30), testScenario}
        );
        AbstractComponent.createComponent(
                Station.class.getCanonicalName(),
                new Object[] {station1URI, new Position(0, 0), testScenario}
        );
        AbstractComponent.createComponent(
                Station.class.getCanonicalName(),
                new Object[] {station2URI, new Position(10, 5), testScenario}
        );

        cvm.toggleTracing(windTurbineURI);
        cvm.toggleTracing(station1URI);
        cvm.toggleTracing(station2URI);
    }

    protected TestScenario getScenario(String windTurbineURI, String station1URI, String station2URI) {
        Instant start = this.startInstant;

        return new TestScenario(
                this.clockURI,
                this.startInstant,
                this.endInstant,
                new TestStepI[] {
                        new TestStep(
                                this.clockURI,
                                windTurbineURI,
                                start.plusSeconds(1),
                                owner -> registerAndSubscribe((WindTurbine) owner)),
                        new TestStep(
                                this.clockURI,
                                station1URI,
                                start.plusSeconds(2),
                                owner -> registerStation((Station) owner)),
                        new TestStep(
                                this.clockURI,
                                station2URI,
                                start.plusSeconds(2),
                                owner -> registerStation((Station) owner)),
                        new TestStep(
                                this.clockURI,
                                windTurbineURI,
                                start.plusSeconds(4),
                                owner -> waitForNextMessage((WindTurbine) owner)),
                        new TestStep(
                                this.clockURI,
                                station1URI,
                                start.plusSeconds(5),
                                owner -> publishWind((Station) owner, 12.0, 2.0, "station-1-first")),
                        new TestStep(
                                this.clockURI,
                                station2URI,
                                start.plusSeconds(5),
                                owner -> publishWind((Station) owner, 18.0, 4.0, "station-2-first")),
                        new TestStep(
                                this.clockURI,
                                windTurbineURI,
                                start.plusSeconds(7),
                                owner -> futureNextMessage((WindTurbine) owner)),
                        new TestStep(
                                this.clockURI,
                                station1URI,
                                start.plusSeconds(8),
                                owner -> publishWind((Station) owner, 25.0, 6.0, "station-1-second")),
                        new TestStep(
                                this.clockURI,
                                windTurbineURI,
                                start.plusSeconds(10),
                                owner -> timedWait((WindTurbine) owner))
                });
    }
}