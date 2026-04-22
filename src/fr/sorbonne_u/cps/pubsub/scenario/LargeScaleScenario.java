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
import fr.sorbonne_u.cps.pubsub.filters.MessageFilter;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.message.Message;
import fr.sorbonne_u.cps.pubsub.meteo.Position;
import fr.sorbonne_u.cps.pubsub.meteo.WindData;

import java.time.Instant;

/**
 * Large scale scenario with many components.
 * 
 * Scenario details:
 * - 5 Stations (S1-S5)
 * - 5 WindTurbines (T1-T5)
 * - 2 Bureaus (B1-B2)
 * 
 * Activities:
 * 1. All register.
 * 2. Bureaus create channels and subscribe.
 * 3. Turbines subscribe to different channels with various filters.
 * 4. Stations publish data sequentially.
 * 5. Dynamic unsubscription and re-subscription.
 *
 */
public class LargeScaleScenario extends AbstractScenario {

    public LargeScaleScenario(AbstractCVM cvm) throws Exception {
        super(10000L, 30.0);

        int nStations = 5;
        int nTurbines = 5;
        int nBureaus = 2;

        String[] sURIs = new String[nStations];
        String[] tURIs = new String[nTurbines];
        String[] bURIs = new String[nBureaus];

        for (int i = 0; i < nStations; i++) sURIs[i] = "station-" + (i + 1);
        for (int i = 0; i < nTurbines; i++) tURIs[i] = "turbine-" + (i + 1);
        for (int i = 0; i < nBureaus; i++) bURIs[i] = "bureau-" + (i + 1);

        TestScenario testScenario = this.getScenario(sURIs, tURIs, bURIs);

        for (int i = 0; i < nStations; i++) {
            AbstractComponent.createComponent(Station.class.getCanonicalName(), new Object[]{sURIs[i], new Position(i * 10, i * 10), testScenario});
            cvm.toggleTracing(sURIs[i]);
        }
        for (int i = 0; i < nTurbines; i++) {
            AbstractComponent.createComponent(WindTurbine.class.getCanonicalName(), new Object[]{tURIs[i], new Position(-i * 10, -i * 10), testScenario});
            cvm.toggleTracing(tURIs[i]);
        }
        for (int i = 0; i < nBureaus; i++) {
            AbstractComponent.createComponent(Bureau.class.getCanonicalName(), new Object[]{bURIs[i], testScenario});
            cvm.toggleTracing(bURIs[i]);
        }
    }

    private TestScenario getScenario(String[] s, String[] t, String[] b) {
        Instant start = this.startInstant;
        
        // We define a long list of steps
        TestStepI[] steps = new TestStepI[s.length * 2 + t.length + b.length * 2];
        int idx = 0;

        // 1. Bureaus setup
        for (int i = 0; i < b.length; i++) {
            String uri = b[i];
            steps[idx++] = new TestStep(this.clockURI, uri, start.plusSeconds(10 + i), owner -> {
                try {
                    Bureau bur = (Bureau) owner;
                    bur.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.PREMIUM);
                    bur.getPublicationPlugin().connectToPublishingPort(bur.getRegistrationPlugin().getPublishingPortURI());
                    bur.getPrivilegedPlugin().createChannel("bureau_channel_" + bur.getReflectionInboundPortURI(), ".*");
                    bur.traceMessage("Bureau " + bur.getReflectionInboundPortURI() + " setup complete\n");
                } catch (Exception e) { throw new RuntimeException(e); }
            });
        }

        // 2. Stations register
        for (int i = 0; i < s.length; i++) {
            String uri = s[i];
            steps[idx++] = new TestStep(this.clockURI, uri, start.plusSeconds(20 + i), owner -> {
                try {
                    Station st = (Station) owner;
                    st.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
                    st.getPublicationPlugin().connectToPublishingPort(st.getRegistrationPlugin().getPublishingPortURI());
                    st.traceMessage("Station " + st.getReflectionInboundPortURI() + " registered\n");
                } catch (Exception e) { throw new RuntimeException(e); }
            });
        }

        // 3. Turbines subscribe
        for (int i = 0; i < t.length; i++) {
            String uri = t[i];
            steps[idx++] = new TestStep(this.clockURI, uri, start.plusSeconds(30 + i), owner -> {
                try {
                    WindTurbine wt = (WindTurbine) owner;
                    wt.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
                    wt.getSubscriptionPlugin().subscribe(Broker.DEFAULT_PUBLIC_CHANNEL, new MessageFilter());
                    wt.traceMessage("Turbine " + wt.getReflectionInboundPortURI() + " subscribed to wind\n");
                } catch (Exception e) { throw new RuntimeException(e); }
            });
        }

        // 4. Activity: Sequential publication
        for (int i = 0; i < s.length; i++) {
            String uri = s[i];
            steps[idx++] = new TestStep(this.clockURI, uri, start.plusSeconds(100 + i * 10), owner -> {
                try {
                    Station st = (Station) owner;
                    Message msg = new Message(new WindData(st.getPosition(), 20.0 + st.getUid() % 50, 0.0));
                    msg.putProperty("Type", "wind");
                    msg.putProperty("ID", st.getUid());
                    st.getPublicationPlugin().publish(Broker.DEFAULT_PUBLIC_CHANNEL, msg);
                    st.traceMessage("Station " + st.getReflectionInboundPortURI() + " published wind data\n");
                } catch (Exception e) { throw new RuntimeException(e); }
            });
        }

        // 5. Some more activity...
        for (int i = 0; i < b.length; i++) {
            String uri = b[i];
            steps[idx++] = new TestStep(this.clockURI, uri, start.plusSeconds(200 + i * 10), owner -> {
                try {
                    Bureau bur = (Bureau) owner;
                    bur.traceMessage("Bureau " + bur.getReflectionInboundPortURI() + " is active\n");
                } catch (Exception e) { throw new RuntimeException(e); }
            });
        }

        return new TestScenario(this.clockURI, start, this.endInstant, steps);
    }
}
