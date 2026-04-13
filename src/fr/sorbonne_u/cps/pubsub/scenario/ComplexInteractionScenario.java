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
 * A complex scenario with multiple stations, wind turbines, and a central bureau.
 * 
 * Scenario details:
 * 1. Setup Phase: A central Bureau registers as PREMIUM, creates the weather alerts channel, 
 *    and subscribes to wind data. Three stations register and connect for publication.
 * 2. Subscription Phase: Turbine 1 subscribes to all wind data. Turbine 2 subscribes only to
 *    RED or SCARLET weather alerts.
 * 3. Normal Activity: Station 1 and Station 2 publish moderate wind data.
 * 4. Alert Activity: Station 1 publishes high wind (80.0), which causes the Bureau (acting
 *    as a subscriber to wind and publisher to alerts) to publish a weather alert.
 * 5. Dynamic Changes: Turbine 1 unsubscribes from the wind channel and switches its
 *    subscription to receive all weather alerts.
 * 6. Final Activity: Station 3 publishes extreme wind data (160.0), triggering further alerts.
 *
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class ComplexInteractionScenario extends AbstractScenario {

    // --- Steps for Bureau ---
    static void bureauSetupStep(Bureau bureau) {
        try {
            bureau.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.PREMIUM);
            bureau.getPublicationPlugin().connectToPublishingPort(
                    bureau.getRegistrationPlugin().getPublishingPortURI());
            bureau.getPrivilegedPlugin().createChannel(Bureau.WEATHER_ALERTS_CHANNEL, ".*");
            bureau.getSubscriptionPlugin().subscribe(Broker.WIND_CHANNEL, new MessageFilter());
            bureau.traceMessage("Bureau: Setup complete (PREMIUM, created channel, subscribed to wind)\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // --- Steps for WindTurbine ---
    static void turbineSubscribeStep(WindTurbine wt, String channel, MessageFilterI filter, String msg) {
        try {
            if (!wt.getRegistrationPlugin().registered()) {
                wt.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
            }
            wt.getSubscriptionPlugin().subscribe(channel, filter);
            wt.traceMessage("WindTurbine: " + msg + " to " + channel + " with filter " + filter + "\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void turbineUnsubscribeStep(WindTurbine wt, String channel) {
        try {
            wt.getSubscriptionPlugin().unsubscribe(channel);
            wt.traceMessage("WindTurbine: Unsubscribed from " + channel + "\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // --- Steps for Station ---
    static void stationSetupStep(Station station) {
        try {
            station.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
            station.getPublicationPlugin().connectToPublishingPort(
                    station.getRegistrationPlugin().getPublishingPortURI());
            station.traceMessage("Station: Registered and connected to publication\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void stationPublishStep(Station station, double windForce, double windDirection) {
        try {
            Message msg = new Message(new WindData(station.getPosition(), windForce, windDirection));
            msg.putProperty("Type", "wind");
            msg.putProperty("ID", station.getUid());
            station.getPublicationPlugin().publish(Broker.WIND_CHANNEL, msg);
            station.traceMessage("Station: Published wind data (force=" + windForce + ")\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ComplexInteractionScenario(AbstractCVM cvm) throws Exception {
        super(5000L, 20.0); // More delay for setup, slower acceleration for better trace readability

        String turbine1_URI = "turbine-1";
        String turbine2_URI = "turbine-2";
        String station1_URI = "station-1";
        String station2_URI = "station-2";
        String station3_URI = "station-3";
        String bureau_URI = "bureau-central";

        TestScenario testScenario = this.getScenario(turbine1_URI, turbine2_URI, station1_URI, station2_URI, station3_URI, bureau_URI);

        AbstractComponent.createComponent(WindTurbine.class.getCanonicalName(), new Object[] { turbine1_URI, new Position(10, 10), testScenario });
        AbstractComponent.createComponent(WindTurbine.class.getCanonicalName(), new Object[] { turbine2_URI, new Position(-10, -10), testScenario });
        AbstractComponent.createComponent(Station.class.getCanonicalName(), new Object[] { station1_URI, new Position(5, 5), testScenario });
        AbstractComponent.createComponent(Station.class.getCanonicalName(), new Object[] { station2_URI, new Position(-5, -5), testScenario });
        AbstractComponent.createComponent(Station.class.getCanonicalName(), new Object[] { station3_URI, new Position(100, 100), testScenario });
        AbstractComponent.createComponent(Bureau.class.getCanonicalName(), new Object[] { bureau_URI, testScenario });

        cvm.toggleTracing(turbine1_URI);
        cvm.toggleTracing(turbine2_URI);
        cvm.toggleTracing(station1_URI);
        cvm.toggleTracing(station2_URI);
        cvm.toggleTracing(station3_URI);
        cvm.toggleTracing(bureau_URI);
    }

    private TestScenario getScenario(String t1, String t2, String s1, String s2, String s3, String b) {
        Instant start = this.startInstant;

        // Filters
        MessageFilterI filterHighWind = new MessageFilter(
                new MessageFilterI.PropertyFilterI[]{
                        new PropertyFilter("type", new ComparableValueFilter("alert"))
                },
                new MessageFilterI.PropertiesFilterI[]{
                        new PropertiesFilter(new MultiValuesFilter<MeteoAlertI>(new String[]{"Data"},
                                args -> args.get(0).getLevel() == MeteoAlert.Level.RED || args.get(0).getLevel() == MeteoAlert.Level.SCARLET))
                },
                TimeFilter.acceptAny()
        );

        MessageFilterI filterAnyAlert = new MessageFilter(
                new MessageFilterI.PropertyFilterI[]{
                        new PropertyFilter("type", new ComparableValueFilter("alert"))
                },
                null,
                TimeFilter.acceptAny()
        );

        return new TestScenario(
                this.clockURI, start, this.endInstant,
                new TestStepI[]{
                        // 1. Setup Phase
                        new TestStep(this.clockURI, b, start.plusSeconds(10), owner -> bureauSetupStep((Bureau) owner)),
                        new TestStep(this.clockURI, s1, start.plusSeconds(40), owner -> stationSetupStep((Station) owner)),
                        new TestStep(this.clockURI, s2, start.plusSeconds(40), owner -> stationSetupStep((Station) owner)),
                        new TestStep(this.clockURI, s3, start.plusSeconds(40), owner -> stationSetupStep((Station) owner)),

                        // 2. Subscriptions Phase
                        new TestStep(this.clockURI, t1, start.plusSeconds(60), owner -> turbineSubscribeStep((WindTurbine) owner, Broker.WIND_CHANNEL, new MessageFilter(), "Subscribed to all wind data")),
                        new TestStep(this.clockURI, t2, start.plusSeconds(80), owner -> turbineSubscribeStep((WindTurbine) owner, Bureau.WEATHER_ALERTS_CHANNEL, filterHighWind, "Subscribed only to RED/SCARLET alerts")),

                        // 3. Normal Activity
                        new TestStep(this.clockURI, s1, start.plusSeconds(100), owner -> stationPublishStep((Station) owner, 15.0, 0.0)),
                        new TestStep(this.clockURI, s2, start.plusSeconds(120), owner -> stationPublishStep((Station) owner, 25.0, 45.0)),

                        // 4. Alert Activity (Station 1 publishes high wind -> Bureau publishes alert)
                        // Bureau will publish alert based on force 80.0
                        new TestStep(this.clockURI, s1, start.plusSeconds(200), owner -> stationPublishStep((Station) owner, 80.0, 90.0)),

                        // 5. Dynamic Changes
                        new TestStep(this.clockURI, t1, start.plusSeconds(240), owner -> turbineUnsubscribeStep((WindTurbine) owner, Broker.WIND_CHANNEL)),
                        new TestStep(this.clockURI, t1, start.plusSeconds(260), owner -> turbineSubscribeStep((WindTurbine) owner, Bureau.WEATHER_ALERTS_CHANNEL, filterAnyAlert, "Subscribed to all alerts")),

                        // 6. Final Activity (Extreme wind)
                        new TestStep(this.clockURI, s3, start.plusSeconds(300), owner -> stationPublishStep((Station) owner, 160.0, 180.0)),
                });
    }
}
