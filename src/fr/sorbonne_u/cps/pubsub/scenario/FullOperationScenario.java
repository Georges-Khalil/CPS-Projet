package fr.sorbonne_u.cps.pubsub.scenario;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestStep;
import fr.sorbonne_u.components.utils.tests.TestStepI;
import fr.sorbonne_u.cps.meteo.interfaces.MeteoAlertI;
import fr.sorbonne_u.cps.pubsub.components.Broker;
import fr.sorbonne_u.cps.pubsub.components.Bureau;
import fr.sorbonne_u.cps.pubsub.components.Station;
import fr.sorbonne_u.cps.pubsub.components.WindTurbine;
import fr.sorbonne_u.cps.pubsub.filters.*;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.message.Message;
import fr.sorbonne_u.cps.pubsub.meteo.MeteoAlert;
import fr.sorbonne_u.cps.pubsub.meteo.Position;
import fr.sorbonne_u.cps.pubsub.meteo.WindData;

import java.time.Instant;

/**
 * Full operation test scenario that involves registration, subscription, and publication.
 * 
 * Scenario details:
 * 1. A Bureau registers as PREMIUM, creates the weather alerts channel, and subscribes to the wind channel.
 * 2. A WindTurbine registers as FREE, subscribes to the wind channel, and also subscribes to the weather alerts
 *    channel with a specific filter (filtering for alerts that are NOT GREEN).
 * 3. Station 1 registers as FREE and publishes an initial wind data message.
 * 4. Station 2 registers as FREE.
 * 5. Station 1 publishes another wind data message.
 *
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class FullOperationScenario extends AbstractScenario {

    static void bureauStep(Bureau bureau) {
        try {
            bureau.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.PREMIUM);
            bureau.getPublicationPlugin().connectToPublishingPort(
                    bureau.getRegistrationPlugin().getPublishingPortURI());
            bureau.traceMessage("Bureau : Registered as PREMIUM\n");
            // Create the weather alerts channel
            bureau.getPrivilegedPlugin().createChannel(Bureau.WEATHER_ALERTS_CHANNEL, ".*");
            bureau.traceMessage("Bureau : Created weather_alert_channel\n");
            bureau.getSubscriptionPlugin().subscribe(Broker.DEFAULT_PUBLIC_CHANNEL, new MessageFilter());
            bureau.traceMessage("Bureau : Subscribed to wind_channel\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void windTurbineStep(WindTurbine wt) {
        try {
            wt.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
            wt.traceMessage("WindTurbine : Registered\n");
            wt.getSubscriptionPlugin().subscribe(Broker.DEFAULT_PUBLIC_CHANNEL, new MessageFilter());
            wt.traceMessage("Subscribed to wind_channel\n");

            // Subscribe to weather alerts with a specific filter
            MessageFilterI filter = new MessageFilter(
                    new MessageFilterI.PropertyFilterI[]{
                            new PropertyFilter("type", new ComparableValueFilter("alert"))
                    },
                    new MessageFilterI.PropertiesFilterI[]{
                            new PropertiesFilter(new MultiValuesFilter<MeteoAlertI>(new String[]{"Data"},
                                    args -> args.get(0).getLevel() != MeteoAlert.Level.GREEN))
                    },
                    TimeFilter.acceptAny()
            );
            wt.getSubscriptionPlugin().subscribe(Bureau.WEATHER_ALERTS_CHANNEL, filter);
            wt.traceMessage("WindTurbine : Subscribed to weather_alerts_channel with filter " + filter + "\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void station1InitStep(Station station) {
        try {
            station.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
            station.getPublicationPlugin().connectToPublishingPort(
                    station.getRegistrationPlugin().getPublishingPortURI());
            station.traceMessage("Station1 : Registered\n");

            // Publish a test message with WindData as payload
            Message msg = new Message(new WindData(station.getPosition(), 10.0, 5.0));
            msg.putProperty("Type", "wind");
            msg.putProperty("ID", station.getUid());
            station.getPublicationPlugin().publish(Broker.DEFAULT_PUBLIC_CHANNEL, msg);
            station.traceMessage("Station1 : Publish a message on wind_channel - " + msg.getPayload() + "\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void station2Step(Station station) {
        try {
            station.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
            station.getPublicationPlugin().connectToPublishingPort(
                    station.getRegistrationPlugin().getPublishingPortURI());
            station.traceMessage("Station2 : Registered\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void station1PubStep(Station station) {
        try {
            fr.sorbonne_u.cps.pubsub.message.Message msg = new fr.sorbonne_u.cps.pubsub.message.Message(
                    new fr.sorbonne_u.cps.pubsub.meteo.WindData(
                            new fr.sorbonne_u.cps.pubsub.meteo.Position(10, 20),
                            10.0,
                            5.0
                    )
            );
            msg.putProperty("Type", "wind");
            msg.putProperty("ID", station.getUid()); // fixed ID for simplicity in test
            station.getPublicationPlugin().publish(Broker.DEFAULT_PUBLIC_CHANNEL, msg);
            station.traceMessage("Publish a message on wind_channel - " + msg.getPayload() + "\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public FullOperationScenario(AbstractCVM cvm) throws Exception {
        super(3000L, 60.0);

        // ----- Component URIs and TestScenario -----
        // Warning: each component URI must be unique
        // Warning: all created components need to be present in the test scenario they're given (otherwise assertion error)

        String windTurbine1_URI = "windTurbine1";
        String station1_URI = "station1";
        String bureau1_URI = "bureau1";
        String station2_URI = "station2";

        TestScenario testScenario = this.getScenario(windTurbine1_URI, station1_URI, bureau1_URI, station2_URI);

        AbstractComponent.createComponent(WindTurbine.class.getCanonicalName(), new Object[] { windTurbine1_URI, new Position(20, 30), testScenario });
        AbstractComponent.createComponent(Station.class.getCanonicalName(), new Object[] { station1_URI, new Position(0, 0), testScenario });
        AbstractComponent.createComponent(Station.class.getCanonicalName(), new Object[] { station2_URI, new Position(50, 30), testScenario });
        AbstractComponent.createComponent(Bureau.class.getCanonicalName(), new Object[] { bureau1_URI, testScenario });

        cvm.toggleTracing(windTurbine1_URI);
        cvm.toggleTracing(station1_URI);
        cvm.toggleTracing(station2_URI);
        cvm.toggleTracing(bureau1_URI);
    }

    private TestScenario getScenario(String windTurbine1_URI, String station1_URI, String bureau1_URI, String station2_URI) {
        Instant start = this.startInstant;

        return new TestScenario(
                this.clockURI,
                start,
                this.endInstant,
                new TestStepI[]{
                        new TestStep(this.clockURI, bureau1_URI, start.plusSeconds(200), owner -> bureauStep((Bureau) owner)),
                        new TestStep(this.clockURI, windTurbine1_URI, start.plusSeconds(260), owner -> windTurbineStep((WindTurbine) owner)),

                        new TestStep(this.clockURI, station1_URI, start.plusSeconds(320), owner -> station1InitStep((Station) owner)),
                        new TestStep(this.clockURI, station2_URI, start.plusSeconds(380), owner -> station2Step((Station) owner)),

                        new TestStep(this.clockURI, station1_URI, start.plusSeconds(440), owner -> station1PubStep((Station) owner))
                });
    }

}
