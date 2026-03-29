package fr.sorbonne_u.cps.pubsub.scenario;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestStep;
import fr.sorbonne_u.components.utils.tests.TestStepI;
import fr.sorbonne_u.cps.pubsub.composants.Bureau;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.meteo.Position;

import java.time.Instant;

/**
 * Scenario testing channel management: creation, modification of authorized users, and destruction.
 * 
 * Scenario details:
 * 1. Bureau registers as PREMIUM.
 * 2. Bureau creates a channel "private_channel" with a whitelist (only "allowed_station").
 * 3. Bureau modifies authorized users to allow "everyone" (.*).
 * 4. Bureau destroys the channel.
 */
public class ChannelManagementScenario extends AbstractScenario {

    static void setupStep(Bureau bureau) {
        try {
            bureau.getRegistrationPlugin().register(RegistrationCI.RegistrationClass.PREMIUM);
            bureau.getPublicationPlugin().connectToPublishingPort(bureau.getRegistrationPlugin().getPublishingPortURI());
            bureau.traceMessage("Bureau: Registered as PREMIUM\n");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    static void createChannelStep(Bureau bureau) {
        try {
            String channel = "private_channel";
            bureau.getPrivilegedPlugin().createChannel(channel, "allowed_station");
            bureau.traceMessage("Bureau: Created channel '" + channel + "' with whitelist 'allowed_station'\n");
            
            boolean hasIt = bureau.getPrivilegedPlugin().hasCreatedChannel(channel);
            assert hasIt : "Bureau should be the owner of the channel";
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    static void modifyChannelStep(Bureau bureau) {
        try {
            String channel = "private_channel";
            bureau.getPrivilegedPlugin().modifyAuthorisedUsers(channel, ".*");
            bureau.traceMessage("Bureau: Modified authorized users for '" + channel + "' to '.*'\n");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    static void destroyChannelStep(Bureau bureau) {
        try {
            String channel = "private_channel";
            bureau.getPrivilegedPlugin().destroyChannel(channel);
            bureau.traceMessage("Bureau: Destroyed channel '" + channel + "'\n");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public ChannelManagementScenario(AbstractCVM cvm) throws Exception {
        super(5000L, 10.0);

        String bureauURI = "mgmt-bureau";
        TestScenario testScenario = this.getScenario(bureauURI);

        AbstractComponent.createComponent(Bureau.class.getCanonicalName(), new Object[]{bureauURI, testScenario});
        cvm.toggleTracing(bureauURI);
    }

    private TestScenario getScenario(String b1) {
        Instant start = this.startInstant;
        return new TestScenario(this.clockURI, start, this.endInstant, new TestStepI[]{
                new TestStep(this.clockURI, b1, start.plusSeconds(10), owner -> setupStep((Bureau) owner)),
                new TestStep(this.clockURI, b1, start.plusSeconds(20), owner -> createChannelStep((Bureau) owner)),
                new TestStep(this.clockURI, b1, start.plusSeconds(30), owner -> modifyChannelStep((Bureau) owner)),
                new TestStep(this.clockURI, b1, start.plusSeconds(40), owner -> destroyChannelStep((Bureau) owner))
        });
    }
}
