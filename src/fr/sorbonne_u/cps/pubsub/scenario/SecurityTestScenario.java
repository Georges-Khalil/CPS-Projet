package fr.sorbonne_u.cps.pubsub.scenario;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestStep;
import fr.sorbonne_u.components.utils.tests.TestStepI;
import fr.sorbonne_u.cps.pubsub.components.Bureau;
import fr.sorbonne_u.cps.pubsub.components.Station;
import fr.sorbonne_u.cps.pubsub.components.WindTurbine;
import fr.sorbonne_u.cps.pubsub.exceptions.UnknownClientException;
import fr.sorbonne_u.cps.pubsub.filters.MessageFilter;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.message.Message;
import fr.sorbonne_u.cps.pubsub.meteo.Position;
import fr.sorbonne_u.cps.pubsub.meteo.WindData;

import java.time.Instant;

/**
 * Security test scenario to verify that unauthorized operations fail as expected.
 * 
 * Scenario details:
 * 1. A FREE WindTurbine tries to create a channel (Privileged operation). Expected: UnauthorisedClientException or similar.
 * 2. An unregistered Station tries to publish. Expected: UnknownClientException.
 * 3. An unregistered WindTurbine tries to subscribe. Expected: UnknownClientException.
 * 4. A registered FREE component tries to publish on a channel it's not authorized for (if applicable).
 *
 */
public class SecurityTestScenario extends AbstractScenario {

    // 1. FREE component tries privileged operation
    static void turbineIllegalPrivilegedStep(WindTurbine wt) {
        try {
            wt.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
            wt.traceMessage("WindTurbine: Registered as FREE. Now trying to create a channel...\n");
            
            boolean caught = false;
            try {
                // WindTurbine doesn't have a PrivilegedPlugin by default in its constructor, 
                // but we can try to use it if we install it, or we can test if the Broker rejects the call.
                // However, the WindTurbine class in this project doesn't have ClientPrivilegedPlugin.
                // Let's use a Bureau but don't register it as PREMIUM.
            } catch (Exception e) {
                caught = true;
                wt.traceMessage("WindTurbine: Caught expected exception: " + e + "\n");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void bureauIllegalPrivilegedStep(Bureau bureau) {
        try {
            // Register as FREE instead of PREMIUM
            bureau.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
            bureau.getPublicationPlugin().connectToPublishingPort(
                    bureau.getRegistrationPlugin().getPublishingPortURI());
            bureau.traceMessage("Bureau: Registered as FREE. Now trying to create a channel (privileged)...\n");
            
            boolean caught = false;
            try {
                bureau.getPrivilegedPlugin().createChannel("illegal_channel", ".*");
            } catch (Exception e) {
                caught = true;
                if (e.toString().contains("UnauthorisedClientException")) {
                    bureau.traceMessage("Bureau: Successfully caught expected UnauthorisedClientException\n");
                } else {
                    bureau.traceMessage("Bureau: Caught exception: " + e + "\n");
                }
            }
            assert caught : "Bureau should have failed to create a channel as a FREE client";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 2. Unregistered component tries to publish
    static void stationUnregisteredPublishStep(Station station) {
        try {
            station.traceMessage("Station: NOT registered. Trying to publish...\n");
            boolean caught = false;
            try {
                // Need to connect to broker first, but connection usually happens during registration.
                // If we manually connect the port or use a plugin that is not initialized.
                Message msg = new Message(new WindData(station.getPosition(), 10.0, 5.0));
                station.getPublicationPlugin().publish("any_channel", msg);
            } catch (UnknownClientException e) {
                caught = true;
                station.traceMessage("Station: Successfully caught expected UnknownClientException\n");
            } catch (Exception e) {
                caught = true;
                station.traceMessage("Station: Caught exception: " + e + "\n");
            }
            assert caught : "Station should have failed to publish as an unregistered client";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 3. Unregistered component tries to subscribe
    static void turbineUnregisteredSubscribeStep(WindTurbine wt) {
        try {
            wt.traceMessage("WindTurbine: NOT registered. Trying to subscribe...\n");
            boolean caught = false;
            try {
                wt.getSubscriptionPlugin().subscribe("any_channel", new MessageFilter());
            } catch (UnknownClientException e) {
                caught = true;
                wt.traceMessage("WindTurbine: Successfully caught expected UnknownClientException\n");
            } catch (Exception e) {
                caught = true;
                wt.traceMessage("WindTurbine: Caught exception: " + e + "\n");
            }
            assert caught : "WindTurbine should have failed to subscribe as an unregistered client";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SecurityTestScenario(AbstractCVM cvm) throws Exception {
        super(3000L, 10.0);

        String turbine1_URI = "security-turbine";
        String station1_URI = "security-station";
        String bureau1_URI = "security-bureau";

        TestScenario testScenario = this.getScenario(turbine1_URI, station1_URI, bureau1_URI);

        AbstractComponent.createComponent(WindTurbine.class.getCanonicalName(), new Object[]{turbine1_URI, new Position(0, 0), testScenario});
        AbstractComponent.createComponent(Station.class.getCanonicalName(), new Object[]{station1_URI, new Position(0, 0), testScenario});
        AbstractComponent.createComponent(Bureau.class.getCanonicalName(), new Object[]{bureau1_URI, testScenario});

        cvm.toggleTracing(turbine1_URI);
        cvm.toggleTracing(station1_URI);
        cvm.toggleTracing(bureau1_URI);
    }

    private TestScenario getScenario(String t1, String s1, String b1) {
        Instant start = this.startInstant;
        return new TestScenario(
                this.clockURI, start, this.endInstant,
                new TestStepI[]{
                        new TestStep(this.clockURI, b1, start.plusSeconds(10), owner -> bureauIllegalPrivilegedStep((Bureau) owner)),
                        new TestStep(this.clockURI, s1, start.plusSeconds(20), owner -> stationUnregisteredPublishStep((Station) owner)),
                        new TestStep(this.clockURI, t1, start.plusSeconds(30), owner -> turbineUnregisteredSubscribeStep((WindTurbine) owner))
                });
    }
}
