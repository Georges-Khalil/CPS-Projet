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
 * The broker of the publication / subscription system.
 * Offers: PrivilegedClientCI (& PublishingCI) & RegistrationCI
 * Requires: ReceivingCI
 *
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
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
            this.setWhiteList(whitelist);
        }

        public void setWhiteList(String whitelist) {
            this.whitelist = Arrays.asList(whitelist.split("\t"));
        }
    }


    public static final String BROKER_REGISTRATION_URI = "broker-registration";
    private static final String BROKER_PUBLISH_URI = "broker-publish"; // WILL change in the future

    protected final PublishingInboundPort bpip;
    protected final RegistrationInboundPort brip;
    protected final HashMap<String, Channel> channels;
    protected final HashMap<String, Client> clients;

    protected Broker() throws Exception {
        super(1, 0);
        this.bpip = new PublishingInboundPort(BROKER_PUBLISH_URI, this);
        this.brip = new RegistrationInboundPort(BROKER_REGISTRATION_URI, this);
        this.bpip.publishPort();
        this.brip.publishPort();

        this.channels = new HashMap<>();
        this.clients = new HashMap<>();

        // Default channels
        this.channels.put("channel0", new Channel(null, ""));
        this.channels.put("channel1", new Channel(null, ""));
        this.channels.put("channel2", new Channel(null, ""));
    }

    @Override
    public synchronized void finalise() throws Exception {
        for (Client client : this.clients.values())
            this.doPortDisconnection(client.port.getPortURI());
        super.finalise();
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.bpip.unpublishPort();
            this.brip.unpublishPort();
            for (Client client : this.clients.values())
                client.port.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    // ---- Publication ----

    public void publish(String receptionPortURI, String channel, MessageI message) throws Exception {
        if (message == null)
            throw new IllegalArgumentException();
        if (!registered(receptionPortURI))
            throw new UnknownClientException();
        if (!channelExist(channel))
            throw new UnknownChannelException();

        Channel chan = this.channels.get(channel);

        for (Subscription entry : chan.subscribers)
            if (entry.filter.match(message))
                entry.client.port.receive(channel, message);
    }

    public void publish(String receptionPortURI, String channel, ArrayList<MessageI> messages) throws Exception {
        if (messages == null)
            throw new IllegalArgumentException();

        for (MessageI m : messages)
            publish(receptionPortURI, channel, m);
    }

    // ---- Registering ----

    public boolean registered(String receptionPortURI) throws Exception {
        if (receptionPortURI == null || receptionPortURI.isEmpty())
            throw new IllegalArgumentException();
        return this.clients.containsKey(receptionPortURI);
    }

    public boolean registered(String receptionPortURI, RegistrationCI.RegistrationClass rc) throws Exception {
        if (receptionPortURI == null || rc == null || receptionPortURI.isEmpty())
            throw new IllegalArgumentException();
        Client client = this.clients.get(receptionPortURI);
        return client != null && client.rc.equals(rc);
    }

    public String register(String receptionPortURI, RegistrationCI.RegistrationClass rc) throws Exception {
        if (rc == null)
            throw new IllegalArgumentException();
        if (registered(receptionPortURI))
            throw new AlreadyRegisteredException();

        ReceivingOutboundPort outPort = new ReceivingOutboundPort(this);
        outPort.publishPort();
        this.doPortConnection(
                outPort.getPortURI(),
                receptionPortURI,
                ReceivingConnector.class.getCanonicalName()
        );

        this.clients.put(receptionPortURI, new Client(receptionPortURI, outPort, rc));
        return BROKER_PUBLISH_URI;
    }

    public String modifyServiceClass(String receptionPortURI, RegistrationCI.RegistrationClass rc) throws Exception {
        if (rc == null)
            throw new IllegalArgumentException();
        if (!registered(receptionPortURI))
            throw new UnknownClientException();
        this.clients.get(receptionPortURI).rc = rc;
        return BROKER_PUBLISH_URI;
    }

    public void unregister(String receptionPortURI) throws Exception {
        if (!registered(receptionPortURI))
            throw new UnknownClientException();

        Client client = this.clients.remove(receptionPortURI);

        for (String channel : client.subscriptions)
            this.channels.get(channel).subscribers.removeIf(e -> e.client == client);

        this.doPortDisconnection(client.port.getPortURI());
        client.port.unpublishPort();
        client.port.destroyPort();
    }

    public boolean channelExist(String channel) throws Exception {
        if (channel == null || channel.isEmpty())
            throw new IllegalArgumentException();
        return this.channels.containsKey(channel);
    }

    public boolean subscribed(String receptionPortURI, String channel) throws Exception {
        if (!registered(receptionPortURI))
            throw new UnknownClientException();
        if (!channelExist(channel))
            throw new UnknownChannelException();

        return this.clients.get(receptionPortURI).subscriptions.contains(channel);
    }

    public void subscribe(String receptionPortURI, String channel, MessageFilterI filter) throws Exception {
        if (filter == null)
            throw new IllegalArgumentException();
        if (subscribed(receptionPortURI, channel))
            throw new UnknownClientException();

        this.channels.get(channel).subscribers.add(new Subscription(this.clients.get(receptionPortURI), filter));
        this.clients.get(receptionPortURI).subscriptions.add(channel);
    }

    public void unsubscribe(String receptionPortURI, String channel) throws Exception {
        if (!subscribed(receptionPortURI, channel))
            throw new NotSubscribedChannelException();

        Client client = this.clients.get(receptionPortURI);
        this.channels.get(channel).subscribers.removeIf(e -> e.client == client);
        client.subscriptions.remove(channel);
    }

    public boolean modifyFilter(String receptionPortURI, String channel, MessageFilterI filter) throws Exception {
        if (filter == null)
            throw new IllegalArgumentException();
        if (!subscribed(receptionPortURI, channel))
            throw new NotSubscribedChannelException();

        Client client = this.clients.get(receptionPortURI);
        for (Subscription sub : this.channels.get(channel).subscribers)
            if (sub.client == client) {
                sub.filter = filter;
                return true;
            }
        return false;
    }

    public Boolean channelAuthorised(String receptionPortURI, String channel) throws Exception {
        if (!registered(receptionPortURI))
            throw new UnknownClientException();
        if (channelExist(channel))
            return false;

        return this.clients.get(receptionPortURI).rc == RegistrationCI.RegistrationClass.PREMIUM;
  }

    // ---- Privileged Client ----

    public boolean hasCreatedChannel(String receptionPortURI, String channel) throws Exception {
        if (!registered(receptionPortURI))
            throw new UnknownClientException();
        if (!channelExist(channel))
            throw new UnknownChannelException();
        return this.channels.get(channel).owner_uri.equals(receptionPortURI);
    }

    public boolean channelQuotaReached(String receptionPortURI) throws Exception {
        if (!registered(receptionPortURI))
            throw new UnknownClientException();
        return false; // TODO
    }

    public void createChannel(String receptionPortURI, String channel, String authorisedUsers) throws Exception {
        if (!channelAuthorised(receptionPortURI, channel))
            throw new UnauthorisedClientException();
        this.channels.put(channel, new Channel(receptionPortURI, authorisedUsers));
    }

    public boolean isAuthorisedUser(String channel, String uri) throws Exception {
        if (!registered(uri))
            throw new UnknownClientException();
        if (!channelExist(channel))
            throw new UnknownChannelException();
        return channels.get(channel).whitelist.contains(uri);
    }

    public void modifyAuthorisedUsers(String receptionPortURI, String channel, String authorisedUsers) throws Exception {
        if (authorisedUsers == null || authorisedUsers.isEmpty())
            throw new IllegalArgumentException();
        if (!hasCreatedChannel(receptionPortURI, channel))
            throw new NotOwnerException();
        this.channels.get(channel).setWhiteList(authorisedUsers);
    }

    public void removeAuthorisedUsers(String receptionPortURI, String channel, String users) throws Exception {
        if (users == null || users.isEmpty())
            throw new IllegalArgumentException();
        if (!hasCreatedChannel(receptionPortURI, channel))
            throw new NotOwnerException();

        Channel chan = this.channels.get(channel);
        for (String user : users.split("\t"))
            chan.whitelist.remove(user);
    }

    public void destroyChannel(String receptionPortURI, String channel) throws Exception {
        if (!hasCreatedChannel(receptionPortURI, channel))
            throw new NotOwnerException();
        this.destroyChannelNow(receptionPortURI, channel);
    }

    public void destroyChannelNow(String receptionPortURI, String channel) throws Exception {
        if (!hasCreatedChannel(receptionPortURI, channel))
            throw new NotOwnerException();

        Channel chan = this.channels.remove(channel);
        for (Subscription sub : chan.subscribers)
            sub.client.subscriptions.remove(channel);
    }
}
