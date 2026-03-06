package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.meteo.interfaces.MeteoAlertI;
import fr.sorbonne_u.cps.meteo.interfaces.PositionI;
import fr.sorbonne_u.cps.pubsub.filters.*;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.PropertyFilterI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.PropertiesFilterI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.meteo.MeteoAlert;
import fr.sorbonne_u.cps.pubsub.plugins.ClientRegistrationPlugin;
import fr.sorbonne_u.cps.pubsub.plugins.ClientSubscriptionPlugin;
import fr.sorbonne_u.cps.pubsub.utils.URIGenerator;

/**
 * The Wind Turbine subscribes & receives messages (data, alerts).
 * Uses plugins for registration and subscription.
 *
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class WindTurbine extends AbstractComponent implements ClientI {

    protected final PositionI position;

    /** URI that identifies this client in the pub/sub system. */
    protected final String receptionPortURI;

    /** Plugin for registration. */
    protected ClientRegistrationPlugin registrationPlugin;

    public ClientRegistrationPlugin getRegistrationPlugin() {
        return registrationPlugin;
    }

    /** Plugin for subscription and receiving. */
    protected ClientSubscriptionPlugin subscriptionPlugin;

    public ClientSubscriptionPlugin getSubscriptionPlugin() {
        return subscriptionPlugin;
    }

    protected WindTurbine(PositionI position) throws Exception {
        super(1, 0);
        this.position = position;

        this.receptionPortURI = URIGenerator.getNew(this);

        // Create and install the subscription plugin (creates the ReceivingInboundPort)
        this.subscriptionPlugin = new ClientSubscriptionPlugin(this.receptionPortURI);
        this.installPlugin(this.subscriptionPlugin);

        // Create and install the registration plugin (connects to broker)
        this.registrationPlugin = new ClientRegistrationPlugin(this.receptionPortURI);
        this.installPlugin(this.registrationPlugin);

        // Wire plugin references
        this.subscriptionPlugin.setRegistrationPlugin(this.registrationPlugin);
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        // Register with the broker
        this.registrationPlugin.register(RegistrationCI.RegistrationClass.FREE);
        this.traceMessage("WindTurbine : Registered\n");

        // Subscribe to wind channel with accept-all filter
        this.subscriptionPlugin.subscribe(Broker.WIND_CHANNEL, new MessageFilter());
        this.traceMessage("Subscribed to wind_channel\n");

        // Subscribe to weather alerts with a specific filter
        MessageFilterI filter = new MessageFilter(
            new PropertyFilterI[] {
                    new PropertyFilter("type", new ComparableValueFilter("alert"))
            },
            new PropertiesFilterI[] {
                    new PropertiesFilter(new MultiValuesFilter<MeteoAlertI>(new String[] {"Data"},
                        args -> args.get(0).getLevel() != MeteoAlert.Level.GREEN))
            },
            TimeFilter.acceptAny()
        );
        this.subscriptionPlugin.subscribe(Bureau.WEATHER_ALERTS_CHANNEL, filter);
        this.traceMessage("Subscribed to weather_alerts_channel with filter: type=alert\n");
    }

    @Override
    public synchronized void finalise() throws Exception {
        this.finalisePlugin(ClientRegistrationPlugin.PLUGIN_URI);
        this.finalisePlugin(ClientSubscriptionPlugin.PLUGIN_URI);
        super.finalise();
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.uninstallPlugin(ClientSubscriptionPlugin.PLUGIN_URI);
            this.uninstallPlugin(ClientRegistrationPlugin.PLUGIN_URI);
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    @Override
    public void receiveOne(String channel, MessageI message) {
        this.traceMessage("Message received on '" + channel +
                "' | payload=" + message.getPayload() + " | timestamp=" + message.getTimeStamp() + "\n");
    }

    @Override
    public void receiveMultiple(String channel, MessageI[] messages) {
        for (MessageI m : messages)
            this.receiveOne(channel, m);
    }
}