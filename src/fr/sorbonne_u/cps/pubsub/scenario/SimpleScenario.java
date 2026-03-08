package fr.sorbonne_u.cps.pubsub.scenario;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
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
import fr.sorbonne_u.cps.pubsub.meteo.Position;
import fr.sorbonne_u.cps.pubsub.meteo.WindData;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class SimpleScenario extends AbstractScenario {

    static void windTurbineStep(WindTurbine wt) {
        try {
            wt.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
            wt.traceMessage("WindTurbine : Registered\n");
            wt.getSubscriptionPlugin().subscribe(Broker.WIND_CHANNEL, new MessageFilter());
            wt.traceMessage("Subscribed to wind_channel\n");

            // Subscribe to wind channel with a large filter
            MessageFilterI filter = new MessageFilter(
                    new PropertyFilter("type", new ComparableValueFilter("wind"))
            );
            wt.getSubscriptionPlugin().subscribe(Broker.WIND_CHANNEL, filter);
            wt.traceMessage("WindTurbine : Subscribed to weather_alerts_channel with filter " + filter + "\n");
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
        super(3000L, 60.0);

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
        Instant startInstant = Instant.now().plus(24, ChronoUnit.HOURS);
        Instant endInstant = Instant.now().plus(25, ChronoUnit.HOURS);

        // instants for actions
        Instant turbineRegInstant = startInstant.plusSeconds(220);
        Instant stationRegInstant = startInstant.plusSeconds(240);

        return new TestScenario(
                this.clockURI,
                startInstant,
                endInstant,
                new TestStepI[]{
                        new TestStep(this.clockURI, windTurbine1_URI, turbineRegInstant, owner -> windTurbineStep((WindTurbine) owner)),
                        new TestStep(this.clockURI, station_URI, stationRegInstant, owner -> stationStep((Station) owner)),
                });
    }

}
