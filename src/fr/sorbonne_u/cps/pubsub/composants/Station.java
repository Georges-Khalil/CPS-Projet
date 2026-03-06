package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.meteo.interfaces.PositionI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.message.Message;
import fr.sorbonne_u.cps.pubsub.meteo.WindData;
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

    protected Station(PositionI position) throws Exception {
        super(1, 0);
        this.position = position;
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

        // Register with the broker
        this.registrationPlugin.register(RegistrationCI.RegistrationClass.FREE);

        // Connect the publication plugin to the broker's publishing port
        this.publicationPlugin.connectToPublishingPort(
                this.registrationPlugin.getPublishingPortURI());
        this.traceMessage("Station : Registered\n");

        // Wait for clients to subscribe
        Thread.sleep(1000);

        // Publish a test message with WindData as payload
        Message msg = new Message(new WindData(this.position, 10.0, 5.0));
        msg.putProperty("Type", "wind");
        msg.putProperty("ID", this.uid);

        this.publicationPlugin.publish(Broker.WIND_CHANNEL, msg);
        this.traceMessage("Publish a message on wind_channel - " + msg.getPayload() + "\n");
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
