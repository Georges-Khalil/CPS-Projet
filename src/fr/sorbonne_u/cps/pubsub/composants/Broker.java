package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.pubsub.interfaces.*;
import fr.sorbonne_u.cps.pubsub.connectors.ReceivingConnector;
import fr.sorbonne_u.cps.pubsub.ports.BrokerPublishingInboundPort;
import fr.sorbonne_u.cps.pubsub.ports.BrokerRegistrationInboundPort;
import fr.sorbonne_u.cps.pubsub.ports.BrokerReceivingOutboundPort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Le courtier du système de publication/souscription.
 * Offre: PublishingCI et RegistrationCI
 * Requiert: ReceivingCI
 */
@OfferedInterfaces(offered = {PublishingCI.class, RegistrationCI.class})
@RequiredInterfaces(required = {ReceivingCI.class})
public class Broker extends AbstractComponent {

    public static final String BROKER_PUBLISH_URI = "broker-publish";
    protected BrokerPublishingInboundPort bpip;
    public static final String BROKER_REGISTRATION_URI = "broker-registration";
    protected BrokerRegistrationInboundPort brip;

    /** Nombre de canaux prédéfinis pour les clients FREE. */
    public static final int NB_FREE_CHANNELS = 3;

    /** Clients enregistrés : receptionPortURI -> classe de service. */
    private final Map<String, RegistrationCI.RegistrationClass> registeredClients = new HashMap<>();
    /** Ports sortants vers les clients : receptionPortURI -> outbound port. */
    private final Map<String, BrokerReceivingOutboundPort> clientOutboundPorts = new HashMap<>();
    /** Ensemble des canaux existants. */
    private final Set<String> channels = new HashSet<>();
    /** Abonnements : canal -> (receptionPortURI -> filtre). */
    private final Map<String, Map<String, MessageFilterI>> subscriptions = new HashMap<>();

    protected Broker() throws Exception {
        super(1, 0);
        this.bpip = new BrokerPublishingInboundPort(BROKER_PUBLISH_URI, this);
        this.brip = new BrokerRegistrationInboundPort(BROKER_REGISTRATION_URI, this);
        this.bpip.publishPort();
        this.brip.publishPort();

        // Créer les canaux prédéfinis pour les clients FREE
        for (int i = 0; i < NB_FREE_CHANNELS; i++) {
            String channelName = "channel" + i;
            channels.add(channelName);
            subscriptions.put(channelName, new HashMap<>());
        }
    }

    /** Retourne l'URI du port d'enregistrement du courtier. */
    public static String registrationPortURI() {
        return BROKER_REGISTRATION_URI;
    }

    @Override
    public synchronized void finalise() throws Exception {
        for (BrokerReceivingOutboundPort port : clientOutboundPorts.values()) {
            this.doPortDisconnection(port.getPortURI());
        }
        super.finalise();
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.bpip.unpublishPort();
            this.brip.unpublishPort();
            for (BrokerReceivingOutboundPort port : clientOutboundPorts.values()) {
                port.unpublishPort();
            }
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    // ---- Publication ----

    public void publish(String receptionPortURI, String channel, MessageI message) throws Exception {
        this.traceMessage("Broker: publish sur " + channel + "\n");

        Map<String, MessageFilterI> channelSubs = subscriptions.get(channel);
        if (channelSubs == null || channelSubs.isEmpty()) {
            this.traceMessage("Broker: aucun abonné sur " + channel + "\n");
            return;
        }

        for (Map.Entry<String, MessageFilterI> entry : channelSubs.entrySet()) {
            String subscriberURI = entry.getKey();
            MessageFilterI filter = entry.getValue();

            if (filter == null || filter.match(message)) {
                BrokerReceivingOutboundPort outPort = clientOutboundPorts.get(subscriberURI);
                if (outPort != null) {
                    this.traceMessage("Broker: livraison à " + subscriberURI + "\n");
                    outPort.receive(channel, message);
                }
            }
        }
    }

    public void publish(String receptionPortURI, String channel, ArrayList<MessageI> messages) throws Exception {
        for (MessageI m : messages) {
            publish(receptionPortURI, channel, m);
        }
    }

    // ---- Enregistrement ----

    public boolean registered(String receptionPortURI) throws Exception {
        return registeredClients.containsKey(receptionPortURI);
    }

    public boolean registered(String receptionPortURI, RegistrationCI.RegistrationClass rc) throws Exception {
        RegistrationCI.RegistrationClass current = registeredClients.get(receptionPortURI);
        return current != null && current.equals(rc);
    }

    public String register(String receptionPortURI, RegistrationCI.RegistrationClass rc) throws Exception {
        this.traceMessage("Broker: register " + receptionPortURI + " classe=" + rc + "\n");

        if (registeredClients.containsKey(receptionPortURI)) {
            throw new Exception("AlreadyRegisteredException: " + receptionPortURI);
        }

        registeredClients.put(receptionPortURI, rc);

        // Créer un port sortant pour envoyer des messages à ce client
        BrokerReceivingOutboundPort outPort = new BrokerReceivingOutboundPort(this);
        outPort.publishPort();
        this.doPortConnection(
                outPort.getPortURI(),
                receptionPortURI,
                ReceivingConnector.class.getCanonicalName()
        );
        clientOutboundPorts.put(receptionPortURI, outPort);

        return BROKER_PUBLISH_URI;
    }

    public String modifyServiceClass(String receptionPortURI, RegistrationCI.RegistrationClass rc) throws Exception {
        if (!registeredClients.containsKey(receptionPortURI)) {
            throw new Exception("UnknownClientException: " + receptionPortURI);
        }
        registeredClients.put(receptionPortURI, rc);
        return BROKER_PUBLISH_URI;
    }

    public void unregister(String receptionPortURI) throws Exception {
        if (!registeredClients.containsKey(receptionPortURI)) {
            throw new Exception("UnknownClientException: " + receptionPortURI);
        }
        registeredClients.remove(receptionPortURI);

        for (Map<String, MessageFilterI> subs : subscriptions.values()) {
            subs.remove(receptionPortURI);
        }

        BrokerReceivingOutboundPort outPort = clientOutboundPorts.remove(receptionPortURI);
        if (outPort != null) {
            this.doPortDisconnection(outPort.getPortURI());
            outPort.unpublishPort();
            outPort.destroyPort();
        }
    }

    public boolean channelExists(String channel) throws Exception {
        return channels.contains(channel);
    }

    public boolean subscribed(String receptionPortURI, String channel) throws Exception {
        Map<String, MessageFilterI> channelSubs = subscriptions.get(channel);
        return channelSubs != null && channelSubs.containsKey(receptionPortURI);
    }

    public void subscribe(String receptionPortURI, String channel, MessageFilterI filter) throws Exception {
        this.traceMessage("Broker: " + receptionPortURI + " s'abonne à " + channel + "\n");

        if (!registeredClients.containsKey(receptionPortURI)) {
            throw new Exception("UnknownClientException: " + receptionPortURI);
        }
        if (!channels.contains(channel)) {
            throw new Exception("UnknownChannelException: " + channel);
        }

        subscriptions.get(channel).put(receptionPortURI, filter);
    }

    public void unsubscribe(String receptionPortURI, String channel) throws Exception {
        if (!registeredClients.containsKey(receptionPortURI)) {
            throw new Exception("UnknownClientException: " + receptionPortURI);
        }
        Map<String, MessageFilterI> channelSubs = subscriptions.get(channel);
        if (channelSubs != null) {
            channelSubs.remove(receptionPortURI);
        }
    }

    public boolean modifyFilter(String receptionPortURI, String channel, MessageFilterI filter) throws Exception {
        if (!registeredClients.containsKey(receptionPortURI)) {
            throw new Exception("UnknownClientException: " + receptionPortURI);
        }
        Map<String, MessageFilterI> channelSubs = subscriptions.get(channel);
        if (channelSubs != null && channelSubs.containsKey(receptionPortURI)) {
            channelSubs.put(receptionPortURI, filter);
            return true;
        }
        return false;
    }
}
