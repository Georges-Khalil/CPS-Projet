package fr.sorbonne_u.cps.pubsub.scenario;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestStep;
import fr.sorbonne_u.components.utils.tests.TestStepI;
import fr.sorbonne_u.cps.pubsub.composants.Broker;
import fr.sorbonne_u.cps.pubsub.composants.Station;
import fr.sorbonne_u.cps.pubsub.composants.WindTurbine;
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
public class FilterModificationScenario extends AbstractScenario {

    static void setupTurbine(WindTurbine wt) {
        try {
            wt.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
            MessageFilterI filter = new MessageFilter(
                new MessageFilterI.PropertyFilterI[]{
                    new PropertyFilter("force", new ComparableValueFilter(10.0, ComparableValueFilter.Operator.GT))
                }
            );
            wt.getSubscriptionPlugin().subscribe(Broker.WIND_CHANNEL, filter);
            wt.traceMessage("WindTurbine: Subscribed to wind with filter force > 10.0\n");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    static void setupStation(Station station) {
        try {
            station.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
            station.getPublicationPlugin().connectToPublishingPort(station.getRegistrationPlugin().getPublishingPortURI());
            station.traceMessage("Station: Registered\n");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    static void publishMessage(Station station, double force) {
        try {
            Message msg = new Message(new WindData(station.getPosition(), force, 0.0));
            msg.putProperty("force", force);
            station.getPublicationPlugin().publish(Broker.WIND_CHANNEL, msg);
            station.traceMessage("Station: Published wind data with force=" + force + "\n");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    static void modifyFilter(WindTurbine wt) {
        try {
            MessageFilterI newFilter = new MessageFilter(
                new MessageFilterI.PropertyFilterI[]{
                    new PropertyFilter("force", new ComparableValueFilter(20.0, ComparableValueFilter.Operator.GT))
                }
            );
            wt.getSubscriptionPlugin().modifyFilter(Broker.WIND_CHANNEL, newFilter);
            wt.traceMessage("WindTurbine: Modified filter to force > 20.0\n");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public FilterModificationScenario(AbstractCVM cvm) throws Exception {
        super(3000L, 10.0);

        String turbineURI = "filter-turbine";
        String stationURI = "filter-station";
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
                new TestStep(this.clockURI, s1, start.plusSeconds(15), owner -> setupStation((Station) owner)),
                new TestStep(this.clockURI, s1, start.plusSeconds(20), owner -> publishMessage((Station) owner, 5.0)),
                new TestStep(this.clockURI, s1, start.plusSeconds(30), owner -> publishMessage((Station) owner, 15.0)),
                new TestStep(this.clockURI, t1, start.plusSeconds(40), owner -> modifyFilter((WindTurbine) owner)),
                new TestStep(this.clockURI, s1, start.plusSeconds(50), owner -> publishMessage((Station) owner, 15.0)),
                new TestStep(this.clockURI, s1, start.plusSeconds(60), owner -> publishMessage((Station) owner, 25.0))
        });
    }
}
