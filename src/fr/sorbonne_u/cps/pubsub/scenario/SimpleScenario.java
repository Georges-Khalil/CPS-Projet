package fr.sorbonne_u.cps.pubsub.scenario;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestStep;
import fr.sorbonne_u.components.utils.tests.TestStepI;
import fr.sorbonne_u.cps.pubsub.components.Broker;
import fr.sorbonne_u.cps.pubsub.components.Station;
import fr.sorbonne_u.cps.pubsub.components.WindTurbine;
import fr.sorbonne_u.cps.pubsub.filters.*;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.message.Message;
import fr.sorbonne_u.cps.pubsub.meteo.Position;
import fr.sorbonne_u.cps.pubsub.meteo.WindData;

import java.time.Instant;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class SimpleScenario extends AbstractScenario {

    static void windTurbineStep(WindTurbine wt) {
        try {
            wt.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
            wt.traceMessage("WindTurbine : Registered\n");
            wt.getSubscriptionPlugin().subscribe(Broker.WIND_CHANNEL, new MessageFilter());
            wt.traceMessage("Subscribed to wind_channel\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void stationStep(Station station) {
        try {
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
            throw new RuntimeException(e);
        }
    }

    public SimpleScenario(AbstractCVM cvm) throws Exception {
        super(1000L, 1.0);

        // ----- Component URIs and TestScenario -----
        // Warning: each component URI must be unique
        // Warning: all created components need to be present in the test scenario they're given (otherwise assertion error)

        String windTurbine1_URI = "windTurbine1";
        String station_URI = "station1";

        TestScenario testScenario = this.getScenario(windTurbine1_URI, station_URI);

        AbstractComponent.createComponent(
                WindTurbine.class.getCanonicalName(),
                new Object[] {windTurbine1_URI, new Position(20, 30), testScenario}
        );

        AbstractComponent.createComponent(
                Station.class.getCanonicalName(),
                new Object[] {station_URI, new Position(0, 0), testScenario}
        );

        cvm.toggleTracing(windTurbine1_URI);
        cvm.toggleTracing(station_URI);
    }

    private TestScenario getScenario(String windTurbine1_URI, String station_URI) {
        Instant start = this.startInstant;

        return new TestScenario(
                this.clockURI,
                this.startInstant,
                this.endInstant,
                new TestStepI[]{
                        new TestStep(this.clockURI, windTurbine1_URI, start.plusSeconds(1), owner -> windTurbineStep((WindTurbine) owner)),
                        new TestStep(this.clockURI, station_URI, start.plusSeconds(2), owner -> stationStep((Station) owner)),
                });
    }

}
