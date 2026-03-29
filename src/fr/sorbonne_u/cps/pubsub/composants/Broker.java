package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.pubsub.exceptions.*;
import fr.sorbonne_u.cps.pubsub.interfaces.*;
import fr.sorbonne_u.cps.pubsub.connectors.ReceivingConnector;
import fr.sorbonne_u.cps.pubsub.ports.PrivilegedClientInboundPort;
import fr.sorbonne_u.cps.pubsub.ports.RegistrationInboundPort;
import fr.sorbonne_u.cps.pubsub.ports.ReceivingOutboundPort;

import java.security.acl.NotOwnerException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
        volatile RegistrationCI.RegistrationClassI rc;

        Client(String receivingUri, ReceivingOutboundPort port, RegistrationCI.RegistrationClassI rc) {
            this.receiving_uri = receivingUri;
            this.port = port;
            this.rc = rc;
            this.subscriptions = Collections.synchronizedList(new ArrayList<>());
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
        final List<Subscription> subscribers;
        volatile List<String> whitelist;

        Channel(String owner_uri, String whitelist) {
            this.owner_uri = owner_uri;
            this.subscribers = new ArrayList<>(); // TODO: better list ?
            this.setWhiteList(whitelist);
        }

        public void setWhiteList(String whitelist) {
            this.whitelist = Arrays.asList(whitelist.split("\t"));
        }
    }

    public static final String WIND_CHANNEL = "wind_channel";
    public static final String BROKER_REGISTRATION_URI = "broker-registration";

    private static final String BROKER_PUBLISH_URI = "broker-publish"; // WILL change in the future

    protected final PrivilegedClientInboundPort bpip;
    protected final RegistrationInboundPort brip;

    protected final HashMap<String, Channel> channels;
    protected final HashMap<String, Client> clients;
    protected final ReadWriteLock channels_lock, clients_lock;

    protected ExecutorService reception_pool, delivery_pool;

    protected Broker() throws Exception {
        super(1, 1);
        this.bpip = new PrivilegedClientInboundPort(BROKER_PUBLISH_URI, this);
        this.brip = new RegistrationInboundPort(BROKER_REGISTRATION_URI, this);
        this.bpip.publishPort();
        this.brip.publishPort();

        this.channels = new HashMap<>();
        this.clients = new HashMap<>();
        this.channels_lock = new ReentrantReadWriteLock();
        this.clients_lock = new ReentrantReadWriteLock();

        this.reception_pool = Executors.newFixedThreadPool(2);
        this.delivery_pool = Executors.newFixedThreadPool(4);

        // Default channels
        this.channels.put(WIND_CHANNEL, new Channel(null, ""));
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
            this.reception_pool.shutdown();
            this.delivery_pool.shutdown();
            this.reception_pool.awaitTermination(1, TimeUnit.SECONDS);
            this.delivery_pool.awaitTermination(1, TimeUnit.SECONDS);

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

        this.channels_lock.readLock().lock();
        try {
            if (!registered(receptionPortURI))
                throw new UnknownClientException();
            if (!channelExist(channel))
                throw new UnknownChannelException();
        } finally {
            this.channels_lock.readLock().unlock();
        }

        reception_pool.submit(() -> {
            try {
                propagateMessage(channel, message);
            } catch (Exception e) {
                this.traceMessage("Propagation error: " + e.getMessage() + "\n");
            }
        });
    }

    protected void propagateMessage(String channel, MessageI message) throws UnknownChannelException {
        this.channels_lock.readLock().lock();
        try {
            Channel chan = channels.get(channel);
            if (chan == null)
                throw new UnknownChannelException();

            for (Subscription entry : chan.subscribers)
                if (entry.filter.match(message))
                    delivery_pool.submit(() -> {
                        try {
                            entry.client.port.receive(channel, message);
                        } catch (Exception e) {
                            this.traceMessage("Delivery error to "
                                    + entry.client.receiving_uri + ": " + e.getMessage() + "\n");
                        }
                    });
        } finally {
            this.channels_lock.readLock().unlock();
        }

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
        this.clients_lock.readLock().lock();
        try {
            return this.clients.containsKey(receptionPortURI);
        } finally {
            this.clients_lock.readLock().unlock();
        }
    }

    public boolean registered(String receptionPortURI, RegistrationCI.RegistrationClass rc) throws Exception {
        if (receptionPortURI == null || rc == null || receptionPortURI.isEmpty())
            throw new IllegalArgumentException();
        this.clients_lock.readLock().lock();
        try {
            Client client = this.clients.get(receptionPortURI);
            return client != null && client.rc.equals(rc);
        } finally {
            this.clients_lock.readLock().unlock();
        }
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

        this.clients_lock.writeLock().lock();
        try {
            this.clients.put(receptionPortURI, new Client(receptionPortURI, outPort, rc));
        } finally {
            this.clients_lock.writeLock().unlock();
        }
        return BROKER_PUBLISH_URI;
    }

    public String modifyServiceClass(String receptionPortURI, RegistrationCI.RegistrationClass rc) throws Exception {
        if (rc == null)
            throw new IllegalArgumentException();
        if (!registered(receptionPortURI))
            throw new UnknownClientException();
        this.clients_lock.writeLock().lock();
        try {
            this.clients.get(receptionPortURI).rc = rc;
        } finally {
            this.clients_lock.writeLock().unlock();
        }
        return BROKER_PUBLISH_URI;
    }

    public void unregister(String receptionPortURI) throws Exception {
        if (!registered(receptionPortURI))
            throw new UnknownClientException();

        this.channels_lock.writeLock().lock();
        this.clients_lock.writeLock().lock();
        try {
            Client client = this.clients.remove(receptionPortURI);
            for (String channel : client.subscriptions)
                this.channels.get(channel).subscribers.removeIf(e -> e.client == client);

            this.doPortDisconnection(client.port.getPortURI());
            client.port.unpublishPort();
            client.port.destroyPort();
        } finally {
            this.channels_lock.writeLock().unlock();
            this.clients_lock.writeLock().unlock();
        }
    }

    public boolean channelExist(String channel) throws Exception {
        if (channel == null || channel.isEmpty())
            throw new IllegalArgumentException();
        this.channels_lock.readLock().lock();
        try {
            return this.channels.containsKey(channel);
        } finally {
            this.channels_lock.readLock().unlock();
        }
    }

    public boolean subscribed(String receptionPortURI, String channel) throws Exception {
        if (!registered(receptionPortURI))
            throw new UnknownClientException();
        if (!channelExist(channel))
            throw new UnknownChannelException();

        this.clients_lock.readLock().lock();
        try {
            return this.clients.get(receptionPortURI).subscriptions.contains(channel);
        } finally {
            this.clients_lock.readLock().unlock();
        }
    }

    public void subscribe(String receptionPortURI, String channel, MessageFilterI filter) throws Exception {
        if (filter == null)
            throw new IllegalArgumentException();
        if (subscribed(receptionPortURI, channel))
            throw new RuntimeException("Already Subscribed");

        this.channels_lock.writeLock().lock();
        this.clients_lock.readLock().lock();
        try {
            this.channels.get(channel).subscribers.add(new Subscription(this.clients.get(receptionPortURI), filter));
            this.clients.get(receptionPortURI).subscriptions.add(channel);
        } finally {
            this.channels_lock.writeLock().unlock();
            this.clients_lock.readLock().unlock();
        }
    }

    public void unsubscribe(String receptionPortURI, String channel) throws Exception {
        if (!subscribed(receptionPortURI, channel))
            throw new NotSubscribedChannelException();

        this.channels_lock.writeLock().lock();
        this.clients_lock.readLock().lock();
        try {
            Client client = this.clients.get(receptionPortURI);
            this.channels.get(channel).subscribers.removeIf(e -> e.client == client);
            client.subscriptions.remove(channel);
        } finally {
            this.channels_lock.writeLock().unlock();
            this.clients_lock.readLock().unlock();
        }
    }

    public boolean modifyFilter(String receptionPortURI, String channel, MessageFilterI filter) throws Exception {
        if (filter == null)
            throw new IllegalArgumentException();
        if (!subscribed(receptionPortURI, channel))
            throw new NotSubscribedChannelException();

        this.channels_lock.writeLock().lock();
        this.clients_lock.readLock().lock();
        try {
            Client client = this.clients.get(receptionPortURI);
            for (Subscription sub : this.channels.get(channel).subscribers)
                if (sub.client == client) {
                    sub.filter = filter;
                    return true;
                }
        } finally {
            this.channels_lock.writeLock().unlock();
            this.clients_lock.readLock().unlock();
        }
        return false;
    }

    public Boolean channelAuthorised(String receptionPortURI, String channel) throws Exception {
        if (!registered(receptionPortURI))
            throw new UnknownClientException();
        if (channelExist(channel))
            return false;

        this.clients_lock.writeLock().lock();
        try {
            return this.clients.get(receptionPortURI).rc == RegistrationCI.RegistrationClass.PREMIUM;
        } finally {
            this.clients_lock.writeLock().unlock();
        }
  }

    // ---- Privileged Client ----

    public boolean hasCreatedChannel(String receptionPortURI, String channel) throws Exception {
        if (!registered(receptionPortURI))
            throw new UnknownClientException();
        if (!channelExist(channel))
            throw new UnknownChannelException();
        this.channels_lock.readLock().lock();
        try {
            return this.channels.get(channel).owner_uri.equals(receptionPortURI);
        } finally {
            this.channels_lock.readLock().unlock();
        }
    }

    public boolean channelQuotaReached(String receptionPortURI) throws Exception {
        if (!registered(receptionPortURI))
            throw new UnknownClientException();
        return false; // TODO
    }

    public void createChannel(String receptionPortURI, String channel, String authorisedUsers) throws Exception {
        if (!channelAuthorised(receptionPortURI, channel))
            throw new UnauthorisedClientException();
        this.channels_lock.writeLock().lock();
        try {
            this.channels.put(channel, new Channel(receptionPortURI, authorisedUsers));
        } finally {
            this.channels_lock.writeLock().unlock();
        }
    }

    public boolean isAuthorisedUser(String channel, String uri) throws Exception {
        if (!registered(uri))
            throw new UnknownClientException();
        if (!channelExist(channel))
            throw new UnknownChannelException();
        this.channels_lock.readLock().lock();
        try {
            return channels.get(channel).whitelist.contains(uri);
        } finally {
            this.channels_lock.readLock().unlock();
        }
    }

    public void modifyAuthorisedUsers(String receptionPortURI, String channel, String authorisedUsers) throws Exception {
        if (authorisedUsers == null || authorisedUsers.isEmpty())
            throw new IllegalArgumentException();
        if (!hasCreatedChannel(receptionPortURI, channel))
            throw new NotOwnerException();
        this.channels_lock.writeLock().lock();
        try {
            this.channels.get(channel).setWhiteList(authorisedUsers);
        } finally {
            this.channels_lock.writeLock().unlock();
        }
    }

    public void removeAuthorisedUsers(String receptionPortURI, String channel, String users) throws Exception {
        if (users == null || users.isEmpty())
            throw new IllegalArgumentException();
        if (!hasCreatedChannel(receptionPortURI, channel))
            throw new NotOwnerException();

        this.channels_lock.writeLock().lock();
        try {
            Channel chan = this.channels.get(channel);
            for (String user : users.split("\t"))
                chan.whitelist.remove(user); // to check
        } finally {
            this.channels_lock.writeLock().unlock();
        }
    }

    public void destroyChannel(String receptionPortURI, String channel) throws Exception {
        if (!hasCreatedChannel(receptionPortURI, channel))
            throw new NotOwnerException();
        this.destroyChannelNow(receptionPortURI, channel);
    }

    public void destroyChannelNow(String receptionPortURI, String channel) throws Exception {
        if (!hasCreatedChannel(receptionPortURI, channel))
            throw new NotOwnerException();

        this.channels_lock.writeLock().lock();
        try {
            Channel chan = this.channels.remove(channel);
            for (Subscription sub : chan.subscribers)
                sub.client.subscriptions.remove(channel);
        } finally {
            this.channels_lock.writeLock().unlock();
        }
    }


    public void asyncPublishAndNotify(String receptionPortURI, String channel, MessageI message, String notificationInboundPortURI) {
        // TODO
    }

    public void asyncPublishAndNotify(String receptionPortURI, String channel, ArrayList<MessageI> messages, String notificationInboundPortURI) {
        // TODO
    }
}
