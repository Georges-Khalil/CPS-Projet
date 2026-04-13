package fr.sorbonne_u.cps.pubsub.scenario;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestStep;
import fr.sorbonne_u.components.utils.tests.TestStepI;
import fr.sorbonne_u.cps.pubsub.components.Broker;
import fr.sorbonne_u.cps.pubsub.components.Bureau;
import fr.sorbonne_u.cps.pubsub.components.Station;
import fr.sorbonne_u.cps.pubsub.components.WindTurbine;
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
 * Scenario testing multiple subscriptions for a single component with complex filtering
 * and interleaved message reception from multiple sources and channels.
 * 
 * Scenario details:
 * - 2 Bureaus (B1, B2)
 * - 3 Stations (S1, S2, S3)
 * - 1 WindTurbine (multi-turbine)
 * 
 * Channels:
 * - WIND_CHANNEL (Default)
 * - alert_channel (Created by B1)
 * - private_channel (Created by B2)
 * 
 * Subscription for multi-turbine:
 * - WIND_CHANNEL: No filter (receive all)
 * - alert_channel: Filter for Level.RED or Level.SCARLET
 * - private_channel: Filter for force > 50.0
 * 
 * Expected behavior:
 * - multi-turbine receives all wind data.
 * - multi-turbine only receives critical alerts.
 * - multi-turbine only receives strong wind data from the private channel.
 * - Reception of messages should be interleaved as publications occur at different times.
 */
public class MultipleSubscriptionScenario extends AbstractScenario {

    static void setupBureau1(Bureau bureau) {
        try {
            bureau.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.PREMIUM);
            bureau.getPublicationPlugin().connectToPublishingPort(bureau.getRegistrationPlugin().getPublishingPortURI());
            bureau.getPrivilegedPlugin().createChannel("alert_channel", ".*");
            bureau.traceMessage("Bureau1: Setup complete, created 'alert_channel'\n");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    static void setupBureau2(Bureau bureau) {
        try {
            bureau.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.PREMIUM);
            bureau.getPublicationPlugin().connectToPublishingPort(bureau.getRegistrationPlugin().getPublishingPortURI());
            bureau.getPrivilegedPlugin().createChannel("private_channel", ".*");
            bureau.traceMessage("Bureau2: Setup complete, created 'private_channel'\n");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    static void setupStation(Station station) {
        try {
            station.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
            station.getPublicationPlugin().connectToPublishingPort(station.getRegistrationPlugin().getPublishingPortURI());
            station.traceMessage("Station " + station.getReflectionInboundPortURI() + ": Registered\n");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    static void setupMultiTurbine(WindTurbine wt) {
        try {
            wt.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
            
            // 1. Subscribe to WIND_CHANNEL (all messages)
            wt.getSubscriptionPlugin().subscribe(Broker.WIND_CHANNEL, new MessageFilter());
            
            // 2. Subscribe to alert_channel (Only RED or SCARLET)
            MessageFilterI alertFilter = new MessageFilter(
                new MessageFilterI.PropertyFilterI[]{
                    new PropertyFilter("level", new ComparableValueFilter(fr.sorbonne_u.cps.pubsub.meteo.MeteoAlert.Level.RED, ComparableValueFilter.Operator.GE))
                }
            );
            wt.getSubscriptionPlugin().subscribe("alert_channel", alertFilter);
            
            // 3. Subscribe to private_channel (Only force > 50.0)
            MessageFilterI forceFilter = new MessageFilter(
                new MessageFilterI.PropertyFilterI[]{
                    new PropertyFilter("force", new ComparableValueFilter(50.0, ComparableValueFilter.Operator.GT))
                }
            );
            wt.getSubscriptionPlugin().subscribe("private_channel", forceFilter);
            
            wt.traceMessage("multi-turbine: Subscribed to 3 channels with filters\n");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    static void publishWind(Station station, String channel, double force) {
        try {
            Message msg = new Message(new WindData(station.getPosition(), force, 0.0));
            msg.putProperty("force", force);
            station.getPublicationPlugin().publish(channel, msg);
            station.traceMessage("Station " + station.getReflectionInboundPortURI() + ": Published to " + channel + " (force=" + force + ")\n");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    static void publishAlert(Bureau bureau, fr.sorbonne_u.cps.pubsub.meteo.MeteoAlert.Level level, String text) {
        try {
            Message msg = new Message(text);
            msg.putProperty("level", level);
            bureau.getPublicationPlugin().publish("alert_channel", msg);
            bureau.traceMessage("Bureau " + bureau.getReflectionInboundPortURI() + ": Published alert " + level + ": " + text + "\n");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public MultipleSubscriptionScenario(AbstractCVM cvm) throws Exception {
        super(5000L, 20.0);

        String b1URI = "multi-bureau-1";
        String b2URI = "multi-bureau-2";
        String s1URI = "multi-station-1";
        String s2URI = "multi-station-2";
        String s3URI = "multi-station-3";
        String turbineURI = "multi-turbine";

        TestScenario testScenario = this.getScenario(b1URI, b2URI, s1URI, s2URI, s3URI, turbineURI);

        AbstractComponent.createComponent(Bureau.class.getCanonicalName(), new Object[]{b1URI, testScenario});
        AbstractComponent.createComponent(Bureau.class.getCanonicalName(), new Object[]{b2URI, testScenario});
        AbstractComponent.createComponent(Station.class.getCanonicalName(), new Object[]{s1URI, new Position(0, 0), testScenario});
        AbstractComponent.createComponent(Station.class.getCanonicalName(), new Object[]{s2URI, new Position(10, 10), testScenario});
        AbstractComponent.createComponent(Station.class.getCanonicalName(), new Object[]{s3URI, new Position(20, 20), testScenario});
        AbstractComponent.createComponent(WindTurbine.class.getCanonicalName(), new Object[]{turbineURI, new Position(5, 5), testScenario});

        cvm.toggleTracing(b1URI);
        cvm.toggleTracing(b2URI);
        cvm.toggleTracing(s1URI);
        cvm.toggleTracing(s2URI);
        cvm.toggleTracing(s3URI);
        cvm.toggleTracing(turbineURI);
    }

    private TestScenario getScenario(String b1, String b2, String s1, String s2, String s3, String t1) {
        Instant start = this.startInstant;
        return new TestScenario(this.clockURI, start, this.endInstant, new TestStepI[]{
                // Setup
                new TestStep(this.clockURI, b1, start.plusSeconds(10), owner -> setupBureau1((Bureau) owner)),
                new TestStep(this.clockURI, b2, start.plusSeconds(15), owner -> setupBureau2((Bureau) owner)),
                new TestStep(this.clockURI, s1, start.plusSeconds(20), owner -> setupStation((Station) owner)),
                new TestStep(this.clockURI, s2, start.plusSeconds(25), owner -> setupStation((Station) owner)),
                new TestStep(this.clockURI, s3, start.plusSeconds(30), owner -> setupStation((Station) owner)),
                new TestStep(this.clockURI, t1, start.plusSeconds(40), owner -> setupMultiTurbine((WindTurbine) owner)),

                // Interleaved activity
                // 1. Station 1 -> WIND_CHANNEL (Received)
                new TestStep(this.clockURI, s1, start.plusSeconds(60), owner -> publishWind((Station) owner, Broker.WIND_CHANNEL, 10.0)),
                
                // 2. Bureau 1 -> alert_channel (Level.GREEN, Filtered out)
                new TestStep(this.clockURI, b1, start.plusSeconds(70), owner -> publishAlert((Bureau) owner, fr.sorbonne_u.cps.pubsub.meteo.MeteoAlert.Level.GREEN, "Everything is fine")),
                
                // 3. Station 2 -> private_channel (force 20.0, Filtered out)
                new TestStep(this.clockURI, s2, start.plusSeconds(80), owner -> publishWind((Station) owner, "private_channel", 20.0)),
                
                // 4. Bureau 1 -> alert_channel (Level.RED, Received)
                new TestStep(this.clockURI, b1, start.plusSeconds(90), owner -> publishAlert((Bureau) owner, fr.sorbonne_u.cps.pubsub.meteo.MeteoAlert.Level.RED, "CRITICAL: Storm coming!")),
                
                // 5. Station 3 -> private_channel (force 80.0, Received)
                new TestStep(this.clockURI, s3, start.plusSeconds(100), owner -> publishWind((Station) owner, "private_channel", 80.0)),
                
                // 6. Station 2 -> WIND_CHANNEL (force 40.0, Received)
                new TestStep(this.clockURI, s2, start.plusSeconds(110), owner -> publishWind((Station) owner, Broker.WIND_CHANNEL, 40.0)),
                
                // 7. Bureau 1 -> alert_channel (Level.SCARLET, Received)
                new TestStep(this.clockURI, b1, start.plusSeconds(120), owner -> publishAlert((Bureau) owner, fr.sorbonne_u.cps.pubsub.meteo.MeteoAlert.Level.SCARLET, "EVACUATE NOW!")),
                
                // 8. Station 1 -> private_channel (force 10.0, Filtered out)
                new TestStep(this.clockURI, s1, start.plusSeconds(130), owner -> publishWind((Station) owner, "private_channel", 10.0))
        });
    }
}
