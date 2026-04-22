package fr.sorbonne_u.cps.pubsub.scenario;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestStep;
import fr.sorbonne_u.components.utils.tests.TestStepI;
import fr.sorbonne_u.cps.pubsub.components.Station;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.message.Message;
import fr.sorbonne_u.cps.pubsub.meteo.Position;
import fr.sorbonne_u.cps.pubsub.meteo.WindData;

import java.time.Instant;

/**
 * Test scenario to verify asynchronous publication error notification.
 * It attempts to publish to a non-existent channel using asyncPublishAndNotify.
 *
 *
 * // NE MARCHE PAS CORRECTEMENT -> Il faut gérer l'exception de publication
 */
public class AsyncErrorScenario extends AbstractScenario {

    protected static void registerStation(Station station) {
        try {
            station.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
            station.getPublicationPlugin().connectToPublishingPort(
                    station.getRegistrationPlugin().getPublishingPortURI());
            station.traceMessage("Station ready for async error test\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static void publishAsyncToUnknownChannel(Station station) {
        try {
            Message message = new Message(new WindData(station.getPosition(), 10.0, 5.0));
            station.traceMessage("Attempting async publish to UNKNOWN channel\n");
            // This channel does not exist, should trigger notification
            station.getPublicationPlugin().asyncPublishAndNotify("unknown-channel", message);
            station.traceMessage("asyncPublishAndNotify returned (asynchronous call)\n");
        } catch (Exception e) {
            station.traceMessage("Immediate exception (should not happen for pool error): " + e.getMessage() + "\n");
        }
    }

    public AsyncErrorScenario(AbstractCVM cvm) throws Exception {
        super(5000L, 1.0);

        String stationURI = "error-station";
        TestScenario testScenario = this.getScenario(stationURI);

        AbstractComponent.createComponent(
                Station.class.getCanonicalName(),
                new Object[] {stationURI, new Position(0, 0), testScenario}
        );

        cvm.toggleTracing(stationURI);
    }

    protected TestScenario getScenario(String stationURI) {
        Instant start = this.startInstant;

        return new TestScenario(
                this.clockURI,
                this.startInstant,
                this.endInstant,
                new TestStepI[] {
                        new TestStep(
                                this.clockURI,
                                stationURI,
                                start.plusSeconds(1),
                                owner -> registerStation((Station) owner)),
                        new TestStep(
                                this.clockURI,
                                stationURI,
                                start.plusSeconds(3),
                                owner -> publishAsyncToUnknownChannel((Station) owner))
                });
    }
}
