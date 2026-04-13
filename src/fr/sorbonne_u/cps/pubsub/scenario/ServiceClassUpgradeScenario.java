package fr.sorbonne_u.cps.pubsub.scenario;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestStep;
import fr.sorbonne_u.components.utils.tests.TestStepI;
import fr.sorbonne_u.cps.pubsub.components.Bureau;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;

import java.time.Instant;

/**
 * Scenario testing service class upgrade (FREE to PREMIUM).
 * 
 * Scenario details:
 * 1. Bureau registers as FREE.
 * 2. Bureau tries to create a channel (Should fail).
 * 3. Bureau upgrades to PREMIUM.
 * 4. Bureau tries to create a channel (Should succeed).
 *
 */
public class ServiceClassUpgradeScenario extends AbstractScenario {

    static void setupFree(Bureau bureau) {
        try {
            bureau.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.FREE);
            bureau.getPublicationPlugin().connectToPublishingPort(bureau.getRegistrationPlugin().getPublishingPortURI());
            bureau.traceMessage("Bureau: Registered as FREE\n");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    static void tryCreateChannelFail(Bureau bureau) {
        try {
            bureau.traceMessage("Bureau: Trying to create channel as FREE (expecting failure)\n");
            boolean caught = false;
            try {
                bureau.getPrivilegedPlugin().createChannel("premium_channel", ".*");
            } catch (Exception e) {
                caught = true;
                bureau.traceMessage("Bureau: Caught expected exception: " + e + "\n");
            }
            assert caught : "Should have failed to create channel as FREE";
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    static void upgradeToPremium(Bureau bureau) {
        try {
            bureau.getRegistrationPlugin().modifyServiceClass(RegistrationCI.RegistrationClass.PREMIUM);
            // Reconnect publication port because URI might have changed
            bureau.getPublicationPlugin().connectToPublishingPort(bureau.getRegistrationPlugin().getPublishingPortURI());
            bureau.traceMessage("Bureau: Upgraded to PREMIUM\n");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    static void tryCreateChannelSuccess(Bureau bureau) {
        try {
            bureau.traceMessage("Bureau: Trying to create channel as PREMIUM\n");
            bureau.getPrivilegedPlugin().createChannel("premium_channel", ".*");
            bureau.traceMessage("Bureau: Successfully created channel 'premium_channel'\n");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public ServiceClassUpgradeScenario(AbstractCVM cvm) throws Exception {
        super(3000L, 10.0);

        String bureauURI = "upgrade-bureau";
        TestScenario testScenario = this.getScenario(bureauURI);

        AbstractComponent.createComponent(Bureau.class.getCanonicalName(), new Object[]{bureauURI, testScenario});
        cvm.toggleTracing(bureauURI);
    }

    private TestScenario getScenario(String b1) {
        Instant start = this.startInstant;
        return new TestScenario(this.clockURI, start, this.endInstant, new TestStepI[]{
                new TestStep(this.clockURI, b1, start.plusSeconds(10), owner -> setupFree((Bureau) owner)),
                new TestStep(this.clockURI, b1, start.plusSeconds(20), owner -> tryCreateChannelFail((Bureau) owner)),
                new TestStep(this.clockURI, b1, start.plusSeconds(30), owner -> upgradeToPremium((Bureau) owner)),
                new TestStep(this.clockURI, b1, start.plusSeconds(40), owner -> tryCreateChannelSuccess((Bureau) owner))
        });
    }
}
