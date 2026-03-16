package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.cps.meteo.interfaces.RegionI;
import fr.sorbonne_u.cps.meteo.interfaces.WindDataI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.message.Message;
import fr.sorbonne_u.cps.pubsub.meteo.MeteoAlert;
import fr.sorbonne_u.cps.pubsub.meteo.MeteoAlert.Level;
import fr.sorbonne_u.cps.pubsub.meteo.Position;
import fr.sorbonne_u.cps.meteo.interfaces.MeteoAlertI;
import fr.sorbonne_u.cps.pubsub.plugins.ClientPrivilegedPlugin;
import fr.sorbonne_u.cps.pubsub.plugins.ClientPublicationPlugin;
import fr.sorbonne_u.cps.pubsub.plugins.ClientRegistrationPlugin;
import fr.sorbonne_u.cps.pubsub.plugins.ClientSubscriptionPlugin;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.cps.pubsub.utils.URIGenerator;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

/**
 * The Bureau publishes weather alerts on the pub/sub system.
 * Uses plugins for registration, publication (privileged), and subscription.
 *
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class Bureau extends AbstractComponent implements ClientI {

    public static String WEATHER_ALERTS_CHANNEL = "weather_alerts_channel";

    /** URI that identifies this client in the pub/sub system. */
    protected final String receptionPortURI;

    /** Plugin for registration. */
    protected ClientRegistrationPlugin registrationPlugin;

    public ClientRegistrationPlugin getRegistrationPlugin() {
        return registrationPlugin;
    }

    /** Plugin for publication (privileged, PREMIUM). */
    protected ClientPublicationPlugin publicationPlugin;

    public ClientPublicationPlugin getPublicationPlugin() {
        return publicationPlugin;
    }

    /** Plugin for subscription and receiving. */
    protected ClientSubscriptionPlugin subscriptionPlugin;

    public ClientSubscriptionPlugin getSubscriptionPlugin() {
        return subscriptionPlugin;
    }

    /** Plugin for privileged channel management. */
    protected ClientPrivilegedPlugin privilegedPlugin;

    public ClientPrivilegedPlugin getPrivilegedPlugin() {
        return privilegedPlugin;
    }

    protected final HashMap<Integer, RegionI> stations;

    /**
     * Test scenario configuration for the Bureau component.
     */
    protected final TestScenario testScenario;

    protected Bureau(String reflectionInboundPortURI, TestScenario testScenario) throws Exception {
        super(reflectionInboundPortURI,1, 1);
        this.testScenario = testScenario;

        this.receptionPortURI = URIGenerator.getNew(this);

        // Create and install the subscription plugin (creates the ReceivingInboundPort)
        this.subscriptionPlugin = new ClientSubscriptionPlugin(this.receptionPortURI);
        this.installPlugin(this.subscriptionPlugin);

        // Create and install the registration plugin
        this.registrationPlugin = new ClientRegistrationPlugin(this.receptionPortURI);
        this.installPlugin(this.registrationPlugin);

        // Create and install the publication plugin (PREMIUM class -> PrivilegedClientOutboundPort)
        this.publicationPlugin = new ClientPublicationPlugin(RegistrationCI.RegistrationClass.PREMIUM);
        this.installPlugin(this.publicationPlugin);

        // Create and install the privileged client plugin
        this.privilegedPlugin = new ClientPrivilegedPlugin();
        this.installPlugin(this.privilegedPlugin);

        // Wire plugin references
        this.subscriptionPlugin.setRegistrationPlugin(this.registrationPlugin);
        this.publicationPlugin.setRegistrationPlugin(this.registrationPlugin);
        this.privilegedPlugin.setPluginReferences(this.registrationPlugin, this.publicationPlugin);

        this.stations = new HashMap<>();
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
        this.finalisePlugin(ClientPrivilegedPlugin.PLUGIN_URI);
        this.finalisePlugin(ClientPublicationPlugin.PLUGIN_URI);
        this.finalisePlugin(ClientRegistrationPlugin.PLUGIN_URI);
        this.finalisePlugin(ClientSubscriptionPlugin.PLUGIN_URI);
        super.finalise();
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.uninstallPlugin(ClientPrivilegedPlugin.PLUGIN_URI);
            this.uninstallPlugin(ClientPublicationPlugin.PLUGIN_URI);
            this.uninstallPlugin(ClientRegistrationPlugin.PLUGIN_URI);
            this.uninstallPlugin(ClientSubscriptionPlugin.PLUGIN_URI);
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    protected void receiveFromStationsInfo(MessageI message) throws Exception {
        this.stations.put((Integer) message.getPropertyValue("ID"), (RegionI) message.getPayload());
    }

    protected void receiveWindData(MessageI message) throws Exception {
        WindDataI windData = (WindDataI) message.getPayload();
        double force = windData.force();

        RegionI region = this.stations.get((Integer) message.getPropertyValue("ID"));

        if (region == null) {
            // Skip creating alert if station info not available yet
            return;
        }

        MeteoAlert alert1 = new MeteoAlert(
                ((Position) windData.getPosition()).x < 0
                        ? MeteoAlertI.AlertType.STORM : MeteoAlertI.AlertType.ICY_STORM,
                force < 20 ? Level.GREEN : force < 60 ? Level.YELLOW
                        : force < 100 ? Level.ORANGE : force < 150 ? Level.RED : Level.SCARLET,
                new RegionI[] { region },
                Instant.now(),
                Duration.ofHours(((Position) windData.getPosition()).x % 7)
        );
        Message msg1 = new Message(alert1);
        msg1.putProperty("type", "alert");

        this.publicationPlugin.publish(WEATHER_ALERTS_CHANNEL, msg1);
        this.traceMessage("Alert published on '" + WEATHER_ALERTS_CHANNEL + "' - " + alert1 + "\n");
    }

    @Override
    public void receiveOne(String channel, MessageI message) throws Exception {
        this.traceMessage("Message received on '" + channel +
                "' | payload=" + message.getPayload() + "\n");

        if (channel.equals(WEATHER_ALERTS_CHANNEL))
            this.receiveFromStationsInfo(message);
        else
            this.receiveWindData(message);
    }

    @Override
    public void receiveMultiple(String channel, MessageI[] messages) throws Exception {
        for (MessageI m : messages)
            this.receiveOne(channel, m);
    }
}