package fr.sorbonne_u.cps.pubsub.components;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.gossip.interfaces.GossipImplementationI;
import fr.sorbonne_u.cps.gossip.interfaces.GossipMessageI;
import fr.sorbonne_u.cps.gossip.interfaces.GossipReceiverCI;
import fr.sorbonne_u.cps.gossip.interfaces.GossipSenderCI;
import fr.sorbonne_u.cps.pubsub.connectors.AbnormalTerminationNotificationConnector;
import fr.sorbonne_u.cps.pubsub.connectors.GossipConnector;
import fr.sorbonne_u.cps.pubsub.connectors.ReceivingConnector;
import fr.sorbonne_u.cps.pubsub.exceptions.*;
import fr.sorbonne_u.cps.pubsub.interfaces.*;
import fr.sorbonne_u.cps.pubsub.message.GossipMessage;
import fr.sorbonne_u.cps.pubsub.ports.*;

import java.security.acl.NotOwnerException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The broker of the publication / subscription system.
 * Offers: PrivilegedClientCI (& PublishingCI) & RegistrationCI
 * Requires: ReceivingCI
 *
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
@OfferedInterfaces(offered = {RegistrationCI.class, PrivilegedClientCI.class, GossipReceiverCI.class})
@RequiredInterfaces(required = {ReceivingCI.class, GossipSenderCI.class})
public class Broker extends AbstractComponent implements GossipImplementationI {

    static class Client {
        private final String receiving_uri;
        private final ReceivingOutboundPort port; // Nullable : please check before use (create methods)
        private final List<String> subscriptions;
        private volatile RegistrationCI.RegistrationClassI rc;

        Client(String receivingUri, ReceivingOutboundPort port, RegistrationCI.RegistrationClassI rc) {
            this.receiving_uri = receivingUri;
            this.port = port;
            this.rc = rc;
            this.subscriptions = Collections.synchronizedList(new ArrayList<>());
        }
        public String getReceivingUri() { return receiving_uri; }
        public ReceivingOutboundPort getPort() { return port; }

        public RegistrationCI.RegistrationClassI getRegistrationClass() { return rc; }
        public void setRegistrationClass(RegistrationCI.RegistrationClassI rc) {
            if (rc == null) throw new IllegalArgumentException("Registration class cannot be null");
            this.rc = rc;
        }
        public boolean isPremium() { return rc == RegistrationCI.RegistrationClass.PREMIUM; }

        // Subscription management
        public List<String> getSubscriptions() { return Collections.unmodifiableList(subscriptions); }
        public void addSubscription(String channel) {
            if (channel == null || channel.isEmpty()) throw new IllegalArgumentException();
            subscriptions.add(channel);
        }
        public void removeSubscription(String channel) { subscriptions.remove(channel); }
        public boolean isSubscribedTo(String channel) { return subscriptions.contains(channel); }

    }

    static class Subscription {
        private final Client client;
        private volatile MessageFilterI filter;

        Subscription(Client client, MessageFilterI filter) {
            if (client == null || filter == null) throw new IllegalArgumentException();
            this.client = client;
            this.filter = filter;
        }

        public Client getClient() { return client; }

        public MessageFilterI getFilter() { return filter; }

        public void setFilter(MessageFilterI filter) {
            if (filter == null) throw new IllegalArgumentException("Filter cannot be null");
            this.filter = filter;
        }

        public boolean filterMatches(MessageI message) {
            return filter.match(message);
        }

    }

    static class Channel {
        final String owner_uri; // todo : make field private (si on a le temps, sinon remove todo)
        final Map<String, Subscription> subscribers;
        private volatile String regex;

        Channel(String owner_uri, String regex) {
            this.owner_uri = owner_uri;
            this.subscribers = new HashMap<>();
            this.regex = regex;
        }

        public void setRegex(String regex) {
            this.regex = regex;
        }

        public boolean isAuthorized(String uri) {
            return regex.isEmpty() || uri.matches(this.regex);
        }
    }

    public static final int CHANNEL_CREATION_QUOTA = 3;
    public static final String DEFAULT_PUBLIC_CHANNEL = "wind_channel";
    public static final String BROKER_REGISTRATION_URI = "broker-registration";

    private static final String BROKER_PUBLISH_URI = "broker-publish"; // WILL change in the future
    private static final String RECEPTION_POOL_URI = "reception-pool";
    private static final String PROPAGATION_POOL_URI = "propagation-pool";
    private static final String DELIVERY_POOL_URI = "delivery-pool";
    private static final String GOSSIP_POOL_URI = "gossip-pool";

    private static final int RECEPTION_POOL_SIZE = 2;
    private static final int PROPAGATION_POOL_SIZE = 2;
    private static final int DELIVERY_POOL_SIZE = 4;
    private static final int    GOSSIP_POOL_SIZE = 2;

    protected final PrivilegedClientInboundPort bpip;
    protected final RegistrationInboundPort brip;
    protected final GossipReceiverInboundPort grip;

    protected final HashMap<String, Channel> channels;
    protected final HashMap<String, Client> clients;
    protected final ReadWriteLock channels_lock, clients_lock;

    protected final List<GossipSenderOutboundPort> gossipNeighbours;
    private final Map<String, Instant> seenMessages = new ConcurrentHashMap<>();

    protected Broker() throws Exception {
        super(1, 0);
        this.bpip = new PrivilegedClientInboundPort(BROKER_PUBLISH_URI, this);
        this.brip = new RegistrationInboundPort(BROKER_REGISTRATION_URI, this);
        this.bpip.publishPort();
        this.brip.publishPort();

        this.channels = new HashMap<>();
        this.clients = new HashMap<>();
        this.channels_lock = new ReentrantReadWriteLock();
        this.clients_lock = new ReentrantReadWriteLock();
        this.gossipNeighbours = new ArrayList<>();

        // Pool instantiation using what is available in BCM4Java (also shuts down automatically)
        this.createNewExecutorService(RECEPTION_POOL_URI, RECEPTION_POOL_SIZE, false);
        this.createNewExecutorService(PROPAGATION_POOL_URI, PROPAGATION_POOL_SIZE, false);
        this.createNewExecutorService(DELIVERY_POOL_URI, DELIVERY_POOL_SIZE, false);

        // Gossip
        this.createNewExecutorService(GOSSIP_POOL_URI, GOSSIP_POOL_SIZE, false);
        this.grip = new GossipReceiverInboundPort("gossip-receiver-" + reflectionInboundPortURI, this);
        this.grip.publishPort();

        // Default channels
        this.channels.put(DEFAULT_PUBLIC_CHANNEL, new Channel(null, ""));
    }

    @Override
    public synchronized void finalise() throws Exception {
        for (Client client : this.clients.values())
            this.doPortDisconnection(client.getPort().getPortURI());
        super.finalise();
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.bpip.unpublishPort();
            this.brip.unpublishPort();
            for (Client client : this.clients.values())
                client.getPort().unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    // ---- Publication ----

    public void publish(String receptionPortURI, String channel, MessageI message) throws Exception {  // TODO: gossip here || send by big packet instead of sending each message individually
        if (message == null)
            throw new IllegalArgumentException();

        this.validatePublicationRequest(receptionPortURI, channel);
        this.submitPublication(channel, Collections.singletonList(message));
    }

    protected void validatePublicationRequest(String receptionPortURI, String channel) throws Exception {
        if (receptionPortURI == null || receptionPortURI.isEmpty() || channel == null || channel.isEmpty())
            throw new IllegalArgumentException();
        if (!registered(receptionPortURI))
            throw new UnknownClientException();
        if (!channelExist(channel))
            throw new UnknownChannelException();
    }

    protected void submitPublication(String channel, List<MessageI> messages) {
        this.runTask(RECEPTION_POOL_URI, (owner) -> this.acceptPublication(channel, messages));
    }

    protected void submitPublication(String channel, List<MessageI> messages, String notificationInboundPortURI) {
        this.runTask(RECEPTION_POOL_URI, (owner) -> {
            try {
                this.acceptPublication(channel, messages);
            } catch (Exception e) {
                this.traceMessage("Publication error: " + e.getMessage() + "\n");
                if (notificationInboundPortURI != null) {
                    this.notifyError(channel, messages, notificationInboundPortURI, e);
                }
            }
        });
    }

    protected void notifyError(String channel, List<MessageI> messages, String notificationInboundPortURI, Throwable cause) {
        this.runTask(PROPAGATION_POOL_URI, (owner) -> {
            try {
                AbnormalTerminationNotificationOutboundPort port = new AbnormalTerminationNotificationOutboundPort(this);
                port.publishPort();
                this.doPortConnection(
                        port.getPortURI(),
                        notificationInboundPortURI,
                        AbnormalTerminationNotificationConnector.class.getCanonicalName()
                );
                if (messages.size() == 1) {
                    port.notifyAbnormalTermination(channel, messages.get(0), cause);
                } else {
                    port.notifyAbnormalTermination(channel, messages.toArray(new MessageI[0]), cause);
                }
                this.doPortDisconnection(port.getPortURI());
                port.unpublishPort();
                port.destroyPort();
            } catch (Exception e) {
                this.traceMessage("Failed to notify client: " + e.getMessage() + "\n");
            }
        });
    }

    protected void acceptPublication(String channel, List<MessageI> messages) {
        for (MessageI message : messages) {
            this.runTask(PROPAGATION_POOL_URI, (owner) -> {
                try {
                    this.propagateMessage(channel, message);
                } catch (Exception e) {
                    this.traceMessage("Propagation error: " + e.getMessage() + "\n");
                }
            });
        }
    }

    protected void propagateMessage(String channel, MessageI message) throws UnknownChannelException {
        Collection<Subscription> subscribers;

        this.channels_lock.readLock().lock();
        try {
            Channel chan = channels.get(channel);
            if (chan == null)
                throw new UnknownChannelException();
            subscribers = chan.subscribers.values();
        } finally {
            this.channels_lock.readLock().unlock();
        }

        for (Subscription entry : subscribers)
            if (entry.filterMatches(message))
                this.runTask(DELIVERY_POOL_URI, (owner) -> this.deliverMessage(channel, message, entry.getClient()));

    }

    protected void deliverMessage(String channel, MessageI message, Client client) {
        try {
            client.getPort().receive(channel, message);
        } catch (Exception e) {
            this.traceMessage("Delivery error to "
                    + client.getReceivingUri() + ": " + e.getMessage() + "\n");
        }
    }

    public void publish(String receptionPortURI, String channel, ArrayList<MessageI> messages) throws Exception {  // TODO: gossip here
        if (messages == null || messages.isEmpty())
            throw new IllegalArgumentException();
        for (MessageI message : messages)
            if (message == null)
                throw new IllegalArgumentException();

        this.validatePublicationRequest(receptionPortURI, channel);
        this.submitPublication(channel, new ArrayList<>(messages));
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
            return client != null && client.getRegistrationClass().equals(rc);
        } finally {
            this.clients_lock.readLock().unlock();
        }
    }

    public String register(String receptionPortURI, RegistrationCI.RegistrationClass rc) throws Exception { // TODO: gossip here; Is it useless ???
        if (rc == null)
            throw new IllegalArgumentException();
        if (registered(receptionPortURI))
            throw new AlreadyRegisteredException();

        ReceivingOutboundPort outPort = null;
        if (!receptionPortURI.startsWith("#")) {
            outPort = new ReceivingOutboundPort(this);
            outPort.publishPort();
            this.doPortConnection(
                    outPort.getPortURI(),
                    receptionPortURI,
                    ReceivingConnector.class.getCanonicalName()
            );
        }

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
            this.clients.get(receptionPortURI).setRegistrationClass(rc);
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
            for (String channel : client.getSubscriptions())
                this.channels.get(channel).subscribers.remove(receptionPortURI);

            this.doPortDisconnection(client.getPort().getPortURI());
            client.getPort().unpublishPort();
            client.getPort().destroyPort();
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
            return this.clients.get(receptionPortURI).isSubscribedTo(channel);
        } finally {
            this.clients_lock.readLock().unlock();
        }
    }

    public void subscribe(String receptionPortURI, String channel, MessageFilterI filter) throws Exception {
        if (filter == null)
            throw new IllegalArgumentException();
        if (subscribed(receptionPortURI, channel))
            throw new RuntimeException("Already Subscribed");
        if (!this.channels.get(channel).isAuthorized(receptionPortURI))
            throw new UnauthorisedClientException();

        this.channels_lock.writeLock().lock();
        this.clients_lock.readLock().lock();
        try {
            this.channels.get(channel).subscribers.put(receptionPortURI, new Subscription(this.clients.get(receptionPortURI), filter));
            this.clients.get(receptionPortURI).addSubscription(channel);
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
            this.channels.get(channel).subscribers.remove(receptionPortURI);
            client.removeSubscription(channel);
        } finally {
            this.channels_lock.writeLock().unlock();
            this.clients_lock.readLock().unlock();
        }
    }

    public boolean modifyFilter(String receptionPortURI, String channel, MessageFilterI filter) throws Exception {
        if (filter == null)
            return false; // Si on ne peut pas appliquer le filtre, on doit return false.
        if (!subscribed(receptionPortURI, channel))
            return false;

        this.channels_lock.writeLock().lock();
        this.clients_lock.readLock().lock();
        try {
            Client client = this.clients.get(receptionPortURI);
            Subscription sub = this.channels.get(channel).subscribers.get(receptionPortURI);
            if (sub == null)
                return false;
            sub.setFilter(filter);
        } finally {
            this.channels_lock.writeLock().unlock();
            this.clients_lock.readLock().unlock();
        }
        return true;
    }

    public Boolean channelAuthorised(String receptionPortURI, String channel) throws Exception {
        if (!registered(receptionPortURI))
            throw new UnknownClientException();
        if (!channelExist(channel))
            throw new UnknownChannelException();

        this.clients_lock.writeLock().lock();
        try {
            return this.channels.get(channel).isAuthorized(receptionPortURI);
        } finally {
            this.clients_lock.writeLock().unlock();
        }
  }

    // ---- Privileged Client ----

    protected boolean isPremium(String receptionPortURI) throws Exception {
        if (!registered(receptionPortURI))
            throw new UnknownClientException();
        return this.clients.get(receptionPortURI).isPremium();
    }

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
        this.channels_lock.readLock().lock();
        try {
            int count = 0;
            for (Channel chan : this.channels.values())
                if (chan.owner_uri != null && chan.owner_uri.equals(receptionPortURI))
                    count++;
            return count >= CHANNEL_CREATION_QUOTA;
        } finally {
            this.channels_lock.readLock().unlock();
        }
    }

    public void createChannel(String receptionPortURI, String channel, String regex) throws Exception { // TODO: gossip here
        if (regex == null)
            throw new IllegalArgumentException();
        if (!isPremium(receptionPortURI))
            throw new UnauthorisedClientException();
        if (channelExist(channel))
            throw new AlreadyExistingChannelException();
        if (channelQuotaReached(receptionPortURI))
            throw new ChannelQuotaExceededException();
        this.channels_lock.writeLock().lock();
        try {
            this.channels.put(channel, new Channel(receptionPortURI, regex));
        } finally {
            this.channels_lock.writeLock().unlock();
        }
    }

    public void modifyAuthorisedUsers(String receptionPortURI, String channel, String regex) throws Exception {
        if (regex == null)
            throw new IllegalArgumentException();
        if (!hasCreatedChannel(receptionPortURI, channel))
            throw new NotOwnerException();
        this.channels_lock.writeLock().lock();
        try {
            this.channels.get(channel).setRegex(regex);
        } finally {
            this.channels_lock.writeLock().unlock();
        }
    }

    public void destroyChannel(String receptionPortURI, String channel) throws Exception {
        if (!hasCreatedChannel(receptionPortURI, channel))
            throw new NotOwnerException();
        this.destroyChannelNow(receptionPortURI, channel);
    }

    public void destroyChannelNow(String receptionPortURI, String channel) throws Exception { // TODO: gossip here
        if (!hasCreatedChannel(receptionPortURI, channel))
            throw new NotOwnerException();

        this.channels_lock.writeLock().lock();
        try {
            Channel chan = this.channels.remove(channel);
            for (Subscription sub : chan.subscribers.values())
                sub.getClient().removeSubscription(channel);
        } finally {
            this.channels_lock.writeLock().unlock();
        }
    }


    public void asyncPublishAndNotify(String receptionPortURI, String channel, MessageI message, String notificationInboundPortURI) throws Exception {
        if (message == null)
            throw new IllegalArgumentException();
        if (notificationInboundPortURI == null || notificationInboundPortURI.isEmpty())
            throw new IllegalArgumentException();

        this.validatePublicationRequest(receptionPortURI, channel);
        this.submitPublication(channel, Collections.singletonList(message), notificationInboundPortURI);
    }

    public void asyncPublishAndNotify(String receptionPortURI, String channel, ArrayList<MessageI> messages, String notificationInboundPortURI) throws Exception {
        if (messages == null || messages.isEmpty())
            throw new IllegalArgumentException();
        for (MessageI message : messages)
            if (message == null)
                throw new IllegalArgumentException();
        if (notificationInboundPortURI == null || notificationInboundPortURI.isEmpty())
            throw new IllegalArgumentException();

        this.validatePublicationRequest(receptionPortURI, channel);
        this.submitPublication(channel, new ArrayList<>(messages), notificationInboundPortURI);
    }

    @Override
    public void update(GossipMessageI[] fromSender) { // TODO: change URI for all the brokers (no static ports ...)
        List<GossipMessageI> toForward = new ArrayList<>();

        for (GossipMessageI m : fromSender) {
            if (this.seenMessages.containsKey(m.gossipMessageURI()) || !(m instanceof GossipMessage)) // TODO: Maybe no continue because Mr. Malenfant doesn't like that
                continue;
            this.seenMessages.put(m.gossipMessageURI(), m.timestamp());

            GossipMessage msg = (GossipMessage) m;
            switch (msg.type) {
                case PUBLICATION:
                    this.runTask(PROPAGATION_POOL_URI, owner -> {
                        try {
                            this.propagateMessage(msg.channel, msg.pubSubMessage);
                        } catch (Exception e) {
                            // ...
                        }
                    });
                    break;
                case REGISTRATION:
                    this.clients_lock.writeLock().lock();
                    try {
                        if (!this.clients.containsKey(msg.clientURI))
                            this.clients.put(msg.clientURI, new Client(msg.clientURI, null, msg.clientRC)); // to check
                    } finally {
                        this.clients_lock.writeLock().unlock();
                    }
                    break;
                case CHANNEL_CREATION:
                    this.channels_lock.writeLock().lock();
                    try {
                        if (!this.channels.containsKey(msg.channelName))
                            this.channels.put(msg.channelName,
                                    new Channel(msg.channelOwner, msg.channelRegex));
                    } finally {
                        this.channels_lock.writeLock().unlock();
                    }
                    break;
                case CHANNEL_DESTRUCTION:
                    this.channels_lock.writeLock().lock();
                    try {
                        Channel chan = this.channels.remove(msg.channelName);
                        for (Subscription sub : chan.subscribers.values())
                            sub.getClient().removeSubscription(msg.channelName);
                    } finally {
                        this.channels_lock.writeLock().unlock();
                    }
            }
            try {
                toForward.add(m.copyWithNewEmitterURI(this.grip.getPortURI()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        try {
            if (!toForward.isEmpty())
                this.gossipToNeighbours(toForward.toArray(new GossipMessageI[0]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void receive(GossipMessageI[] gossipMessages) throws Exception {
        this.runTask(GOSSIP_POOL_URI, owner -> this.update(gossipMessages));
    }

    public void connectToNeighbour(String neighbourGossipURI) throws Exception {
        GossipSenderOutboundPort p = new GossipSenderOutboundPort(this);
        p.publishPort();
        this.doPortConnection(p.getPortURI(), neighbourGossipURI, GossipConnector.class.getCanonicalName());
        this.gossipNeighbours.add(p);
    }

    public void gossipToNeighbours(GossipMessageI[] messages) throws Exception {
        for (GossipSenderOutboundPort gsop : this.gossipNeighbours)
            gsop.send(messages);
    }

}
