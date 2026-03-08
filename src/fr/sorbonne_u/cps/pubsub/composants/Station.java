package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.cps.meteo.interfaces.PositionI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.cps.pubsub.plugins.ClientPublicationPlugin;
import fr.sorbonne_u.cps.pubsub.plugins.ClientRegistrationPlugin;
import fr.sorbonne_u.cps.pubsub.plugins.ClientSubscriptionPlugin;
import fr.sorbonne_u.cps.pubsub.utils.URIGenerator;

/**
 * The station sends data on the pub/sub system.
 * Uses plugins for registration and publication.
 *
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class Station extends AbstractComponent implements ClientI {

    protected final PositionI position;
    protected final int uid;

    public int getUid() {
        return this.uid;
    }
    public PositionI getPosition() {
        return this.position;
    }

    /** URI that identifies this client in the pub/sub system. */
    protected final String receptionPortURI;

    /** Plugin for registration. */
    protected ClientRegistrationPlugin registrationPlugin;

    public ClientRegistrationPlugin getRegistrationPlugin() {
        return registrationPlugin;
    }

    /** Plugin for publication. */
    protected ClientPublicationPlugin publicationPlugin;

    public ClientPublicationPlugin getPublicationPlugin() {
        return publicationPlugin;
    }

    /** Plugin for subscription (needed for ReceivingInboundPort). */
    protected ClientSubscriptionPlugin subscriptionPlugin;

    /**
     * Test scenario configuration for the Station component.
     */
    protected final TestScenario testScenario;

    protected Station(String reflectionInboundPortURI, PositionI position, TestScenario testScenario) throws Exception {
        super(reflectionInboundPortURI, 1, 1);
        this.position = position;
        this.testScenario = testScenario;
        this.uid = URIGenerator.getUID();

        this.receptionPortURI = URIGenerator.getNew(this);

        // Create and install the subscription plugin (creates the ReceivingInboundPort)
        this.subscriptionPlugin = new ClientSubscriptionPlugin(this.receptionPortURI);
        this.installPlugin(this.subscriptionPlugin);

        // Create and install the registration plugin
        this.registrationPlugin = new ClientRegistrationPlugin(this.receptionPortURI);
        this.installPlugin(this.registrationPlugin);

        // Create and install the publication plugin (FREE class)
        this.publicationPlugin = new ClientPublicationPlugin(RegistrationCI.RegistrationClass.FREE);
        this.installPlugin(this.publicationPlugin);

        // Wire plugin references
        this.subscriptionPlugin.setRegistrationPlugin(this.registrationPlugin);
        this.publicationPlugin.setRegistrationPlugin(this.registrationPlugin);
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        // initialise the clock with the given URI, retrieving it from the clock server;
        // this must be done by the component before running the test scenario
        this.initialiseClock(ClocksServer.STANDARD_INBOUNDPORT_URI, this.testScenario.getClockURI());
        // make the component execute its actions in the test scenario
        this.executeTestScenario(this.testScenario);
    }

    @Override
    public synchronized void finalise() throws Exception {
        this.finalisePlugin(ClientPublicationPlugin.PLUGIN_URI);
        this.finalisePlugin(ClientRegistrationPlugin.PLUGIN_URI);
        this.finalisePlugin(ClientSubscriptionPlugin.PLUGIN_URI);
        super.finalise();
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.uninstallPlugin(ClientPublicationPlugin.PLUGIN_URI);
            this.uninstallPlugin(ClientRegistrationPlugin.PLUGIN_URI);
            this.uninstallPlugin(ClientSubscriptionPlugin.PLUGIN_URI);
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    @Override // Not a primary receiver
    public void receiveOne(String channel, MessageI message) { }

    @Override
    public void receiveMultiple(String channel, MessageI[] messages) { }
}
