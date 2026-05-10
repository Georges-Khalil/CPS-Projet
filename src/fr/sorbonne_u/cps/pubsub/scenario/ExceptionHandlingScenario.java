package fr.sorbonne_u.cps.pubsub.scenario;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestStep;
import fr.sorbonne_u.components.utils.tests.TestStepI;
import fr.sorbonne_u.cps.pubsub.components.Broker;
import fr.sorbonne_u.cps.pubsub.components.Bureau;
import fr.sorbonne_u.cps.pubsub.exceptions.*;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;

import java.time.Instant;

/**
 * Scenario covering cases where exceptions are reached.
 * 
 * Scenario details:
 * 1. Bureau1 tries to create a channel before registering (UnknownClientException).
 * 2. Bureau1 registers as PREMIUM and creates a channel.
 * 3. Bureau1 tries to create the same channel again (AlreadyExistingChannelException).
 * 4. Bureau1 creates more channels until quota is exceeded (ChannelQuotaExceededException).
 * 5. Bureau2 registers as FREE and tries to create a channel (UnauthorisedClientException).
 * 6. Bureau1 tries to destroy a non-existing channel (UnknownChannelException).
 *
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class ExceptionHandlingScenario extends AbstractScenario {

    static void unregisteredActionStep(Bureau bureau) {
        bureau.traceMessage("Step 1: Bureau trying to create channel before registering...\n");
        try {
            try {
                // Manually connect the port to the broker even if not registered
                bureau.getPublicationPlugin().connectToPublishingPort(Broker.BROKER_PUBLISH_URI);
                
                bureau.getPrivilegedPlugin().createChannel("pre_register_channel", ".*");
                assert false : "Should have thrown UnknownClientException";
            } catch (Exception e) {
                if (e.toString().contains("UnknownClientException")) {
                    bureau.traceMessage("Caught expected UnknownClientException\n");
                } else {
                    throw e;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void registerAndCreateStep(Bureau bureau) {
        bureau.traceMessage("Step 2: Bureau registering as PREMIUM and creating 'test_channel'...\n");
        try {
            bureau.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.PREMIUM);
            bureau.getPublicationPlugin().connectToPublishingPort(bureau.getRegistrationPlugin().getPublishingPortURI());
            bureau.getPrivilegedPlugin().createChannel("test_channel", ".*");
            bureau.traceMessage("Channel 'test_channel' created successfully\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void duplicateChannelStep(Bureau bureau) {
        bureau.traceMessage("Step 3: Bureau trying to create 'test_channel' again...\n");
        try {
            try {
                bureau.getPrivilegedPlugin().createChannel("test_channel", ".*");
                assert false : "Should have thrown AlreadyExistingChannelException";
            } catch (Exception e) {
                if (e.toString().contains("AlreadyExistingChannelException")) {
                    bureau.traceMessage("Caught expected AlreadyExistingChannelException\n");
                } else {
                    throw e;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void quotaExceededStep(Bureau bureau) {
        bureau.traceMessage("Step 4: Bureau trying to exceed channel quota (limit is 3)...\n");
        try {
            // Already created 1 (test_channel)
            bureau.getPrivilegedPlugin().createChannel("chan2", ".*");
            bureau.getPrivilegedPlugin().createChannel("chan3", ".*");
            bureau.traceMessage("Created chan2 and chan3. Now trying chan4...\n");
            
            try {
                bureau.getPrivilegedPlugin().createChannel("chan4", ".*");
                assert false : "Should have thrown ChannelQuotaExceededException";
            } catch (Exception e) {
                if (e.toString().contains("ChannelQuotaExceededException")) {
                    bureau.traceMessage("Caught expected ChannelQuotaExceededException\n");
                } else {
                    throw e;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void unauthorisedCreationStep(Bureau bureau) {
        bureau.traceMessage("Step 5: Bureau2 (FREE) trying to create a channel...\n");
        try {
            bureau.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
            bureau.getPublicationPlugin().connectToPublishingPort(Broker.BROKER_PUBLISH_URI);
            
            try {
                bureau.getPrivilegedPlugin().createChannel("free_channel", ".*");
                assert false : "Should have thrown UnauthorisedClientException";
            } catch (Exception e) {
                if (e.toString().contains("UnauthorisedClientException")) {
                    bureau.traceMessage("Caught expected UnauthorisedClientException\n");
                } else {
                    throw e;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void unknownChannelStep(Bureau bureau) {
        bureau.traceMessage("Step 6: Bureau1 trying to destroy non-existing channel...\n");
        try {
            try {
                bureau.getPrivilegedPlugin().destroyChannel("ghost_channel");
                assert false : "Should have thrown UnknownChannelException";
            } catch (Exception e) {
                if (e.toString().contains("UnknownChannelException")) {
                    bureau.traceMessage("Caught expected UnknownChannelException\n");
                } else {
                    throw e;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ExceptionHandlingScenario(AbstractCVM cvm) throws Exception {
        super(5000L, 10.0);

        String bureau1_URI = "exception-bureau-1";
        String bureau2_URI = "exception-bureau-2";

        TestScenario scenario1 = getScenario1(bureau1_URI);
        TestScenario scenario2 = getScenario2(bureau2_URI);

        AbstractComponent.createComponent(Bureau.class.getCanonicalName(), new Object[]{bureau1_URI, scenario1});
        AbstractComponent.createComponent(Bureau.class.getCanonicalName(), new Object[]{bureau2_URI, scenario2});

        cvm.toggleTracing(bureau1_URI);
        cvm.toggleTracing(bureau2_URI);
    }

    private TestScenario getScenario1(String b1) {
        Instant start = this.startInstant;
        return new TestScenario(this.clockURI, start, this.endInstant, new TestStepI[]{
                new TestStep(this.clockURI, b1, start.plusSeconds(10), owner -> unregisteredActionStep((Bureau) owner)),
                new TestStep(this.clockURI, b1, start.plusSeconds(20), owner -> registerAndCreateStep((Bureau) owner)),
                new TestStep(this.clockURI, b1, start.plusSeconds(30), owner -> duplicateChannelStep((Bureau) owner)),
                new TestStep(this.clockURI, b1, start.plusSeconds(40), owner -> quotaExceededStep((Bureau) owner)),
                new TestStep(this.clockURI, b1, start.plusSeconds(60), owner -> unknownChannelStep((Bureau) owner))
        });
    }

    private TestScenario getScenario2(String b2) {
        Instant start = this.startInstant;
        return new TestScenario(this.clockURI, start, this.endInstant, new TestStepI[]{
                new TestStep(this.clockURI, b2, start.plusSeconds(50), owner -> unauthorisedCreationStep((Bureau) owner))
        });
    }
}
