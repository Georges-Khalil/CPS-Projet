package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.pubsub.exceptions.*;
import fr.sorbonne_u.cps.pubsub.interfaces.*;
import fr.sorbonne_u.cps.pubsub.connectors.ReceivingConnector;
import fr.sorbonne_u.cps.pubsub.ports.PublishingInboundPort;
import fr.sorbonne_u.cps.pubsub.ports.RegistrationInboundPort;
import fr.sorbonne_u.cps.pubsub.ports.ReceivingOutboundPort;

import java.security.acl.NotOwnerException;
import java.util.*;

/**
 * Le courtier du système de publication/souscription.
 * Offre: PublishingCI et RegistrationCI
 * Requiert: ReceivingCI
 */
@OfferedInterfaces(offered = {RegistrationCI.class, PrivilegedClientCI.class})
@RequiredInterfaces(required = {ReceivingCI.class})
public class Broker extends AbstractComponent {

    static class Client {
        final String receiving_uri;
        final ReceivingOutboundPort port;
        final List<String> subscriptions;
        RegistrationCI.RegistrationClassI rc;

        Client(String receivingUri, ReceivingOutboundPort port, RegistrationCI.RegistrationClassI rc) {
            this.receiving_uri = receivingUri;
            this.port = port;
            this.rc = rc;
            this.subscriptions = new ArrayList<>();
        }
    }

    static class Subscription {
        final Client client;
        MessageFilterI filter;

        Subscription(Client client, MessageFilterI filter) {
            this.client = client;
            this.filter = filter;
        }
    }

    static class Channel {
        final String owner_uri;
        List<Subscription> subscribers;
        List<String> whitelist;

        Channel(String owner_uri, String whitelist) {
            this.owner_uri = owner_uri;
            this.subscribers = new ArrayList<>();
            this.whitelist = Arrays.asList(whitelist.split("\t"));
        }

        public void setWhiteList(String whitelist) {
            this.whitelist = Arrays.asList(whitelist.split("\t"));
        }
    }


    private final HashMap<String, Channel> channels;
    private final HashMap<String, Client> clients;

    public static final String BROKER_PUBLISH_URI = "broker-publish";
    protected PublishingInboundPort bpip;
    public static final String BROKER_REGISTRATION_URI = "broker-registration";
    protected RegistrationInboundPort brip;

    protected Broker() throws Exception {
        super(1, 0);
        this.bpip = new PublishingInboundPort(BROKER_PUBLISH_URI, this);
        this.brip = new RegistrationInboundPort(BROKER_REGISTRATION_URI, this);
        this.bpip.publishPort();
        this.brip.publishPort();

        this.channels = new HashMap<>();
        this.clients = new HashMap<>();

        // Default channels
        channels.put("channel 0", new Channel(null, ""));
        channels.put("channel 1", new Channel(null, ""));
        channels.put("channel 2", new Channel(null, ""));
    }

    @Override
    public synchronized void finalise() throws Exception {
        for (Client client : clients.values())
            this.doPortDisconnection(client.port.getPortURI());
        super.finalise();
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.bpip.unpublishPort();
            this.brip.unpublishPort();
            for (Client client : clients.values())
                client.port.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    // ---- Publication ----

    public void publish(String receptionPortURI, String channel, MessageI message) throws Exception {
        if (!subscribed(receptionPortURI, channel))
            throw new NotSubscribedChannelException();

        Channel chan = channels.get(channel);

        for (Subscription entry : chan.subscribers)
            if (entry.filter.match(message))
                entry.client.port.receive(channel, message);
    }

    public void publish(String receptionPortURI, String channel, ArrayList<MessageI> messages) throws Exception {
        for (MessageI m : messages)
            publish(receptionPortURI, channel, m);
    }

    // ---- Registering ----

    public boolean registered(String receptionPortURI) throws Exception {
        return clients.containsKey(receptionPortURI);
    }

    public boolean registered(String receptionPortURI, RegistrationCI.RegistrationClass rc) throws Exception {
        Client current = clients.get(receptionPortURI);
        return current != null && current.rc.equals(rc);
    }

    public String register(String receptionPortURI, RegistrationCI.RegistrationClass rc) throws Exception {
        if (clients.containsKey(receptionPortURI))
            throw new AlreadyRegisteredException();

        ReceivingOutboundPort outPort = new ReceivingOutboundPort(this);
        outPort.publishPort();
        this.doPortConnection(
                outPort.getPortURI(),
                receptionPortURI,
                ReceivingConnector.class.getCanonicalName()
        );

        clients.put(receptionPortURI, new Client(receptionPortURI, outPort, rc));

        return BROKER_PUBLISH_URI;
    }

    public String modifyServiceClass(String receptionPortURI, RegistrationCI.RegistrationClass rc) throws Exception {
        if (!registered(receptionPortURI))
            throw new UnknownClientException();
        clients.get(receptionPortURI).rc = rc;
        return BROKER_PUBLISH_URI;
    }

    public void unregister(String receptionPortURI) throws Exception {
        if (!registered(receptionPortURI))
            throw new UnknownClientException();

        Client client = clients.remove(receptionPortURI);

        for (String channel : client.subscriptions)
            channels.get(channel).subscribers.removeIf(e -> e.client == client);

        this.doPortDisconnection(client.port.getPortURI());
        client.port.unpublishPort();
        client.port.destroyPort();
    }

    public boolean channelExist(String channel) throws Exception {
        return channels.containsKey(channel);
    }

    public boolean subscribed(String receptionPortURI, String channel) throws Exception {
        Client client = clients.get(receptionPortURI);
        if (client == null)
            throw new UnknownClientException();
        if (!channels.containsKey(channel))
            throw new UnknownChannelException();
        return client.subscriptions.contains(channel);
    }

    public void subscribe(String receptionPortURI, String channel, MessageFilterI filter) throws Exception {
        if (!clients.containsKey(receptionPortURI))
            throw new UnknownClientException();
        if (!channels.containsKey(channel))
            throw new UnknownChannelException();

        this.channels.get(channel).subscribers.add(new Subscription(this.clients.get(receptionPortURI), filter));
        this.clients.get(receptionPortURI).subscriptions.add(channel);
    }

    public void unsubscribe(String receptionPortURI, String channel) throws Exception {
        if (!clients.containsKey(receptionPortURI))
            throw new UnknownClientException();
        if (!channelExist(channel))
            throw new UnknownChannelException();
        if (!clients.get(receptionPortURI).subscriptions.contains(channel))
            throw new NotSubscribedChannelException();

        Client client = clients.get(receptionPortURI);
        channels.get(channel).subscribers.removeIf(e -> e.client == client);
    }

    public boolean modifyFilter(String receptionPortURI, String channel, MessageFilterI filter) throws Exception {
        if (!subscribed(receptionPortURI, channel))
            throw new NotSubscribedChannelException();

        Client client = clients.get(receptionPortURI);
        for (Subscription sub : channels.get(channel).subscribers)
            if (sub.client == client) {
                sub.filter = filter;
                return true;
            }
        return false;
    }

    public Boolean channelAuthorised(String uri, String channel) {
      return false; // TODO
  }

    // ---- Priviliged Client ----

    public boolean hasCreatedChannel(String receptionPortURI, String channel) throws Exception {
        return this.channels.get(channel).owner_uri.equals(receptionPortURI);
    }

    public boolean channelQuotaReached(String receptionPortURI) throws Exception {
        return false;
    }

    public void createChannel(String receptionPortURI, String channel, String autorisedUsers) throws Exception {
        this.channels.put(channel, new Channel(receptionPortURI, autorisedUsers));
    }

    public boolean isAuthorisedUser(String channel, String uri) throws Exception {
        if (!channelExist(channel))
            throw new UnknownChannelException();
        return channels.get(channel).whitelist.contains(uri);
    }

    public void modifyAuthorisedUsers(String receptionPortURI, String channel, String authorisedUsers) throws Exception {
        if (!hasCreatedChannel(receptionPortURI, channel))
            throw new NotOwnerException();
        channels.get(channel).setWhiteList(authorisedUsers);
    }

    public void removeAuthorisedUsers(String receptionPortURI, String channel, String users) throws Exception {
        if (!hasCreatedChannel(receptionPortURI, channel))
            throw new NotOwnerException();

        Channel chan = channels.get(channel);
        for (String user : users.split("\t"))
            chan.whitelist.remove(user);
    }

    public void destroyChannel(String receptionPortURI, String channel) throws Exception {
        if (!hasCreatedChannel(receptionPortURI, channel))
            throw new NotOwnerException();
        this.destroyChannelNow(receptionPortURI, channel);
    }

    public void destroyChannelNow(String receptionPortURI, String channel) throws Exception {

        if (this.channels.get(channel).owner_uri.equals(receptionPortURI))
            this.channels.remove(channel);
    }
}
