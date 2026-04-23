package fr.sorbonne_u.cps.pubsub.components;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.pubsub.exceptions.*;
import fr.sorbonne_u.cps.pubsub.interfaces.*;
import fr.sorbonne_u.cps.pubsub.connectors.AbnormalTerminationNotificationConnector;
import fr.sorbonne_u.cps.pubsub.connectors.ReceivingConnector;
import fr.sorbonne_u.cps.pubsub.ports.AbnormalTerminationNotificationOutboundPort;
import fr.sorbonne_u.cps.pubsub.ports.PrivilegedClientInboundPort;
import fr.sorbonne_u.cps.pubsub.ports.RegistrationInboundPort;
import fr.sorbonne_u.cps.pubsub.ports.ReceivingOutboundPort;

import java.security.acl.NotOwnerException;
import java.util.*;
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
        final ReceivingOutboundPort port; // Nullable : please check before use (create methods)
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
        volatile MessageFilterI filter;

        Subscription(Client client, MessageFilterI filter) {
            this.client = client;
            this.filter = filter;
        }
    }

    static class Channel {
        final String owner_uri;
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

    public static final String DEFAULT_PUBLIC_CHANNEL = "wind_channel";
    public static final String BROKER_REGISTRATION_URI = "broker-registration";

    private static final String BROKER_PUBLISH_URI = "broker-publish"; // WILL change in the future
    private static final String RECEPTION_POOL_URI = "reception-pool";
    private static final String PROPAGATION_POOL_URI = "propagation-pool";
    private static final String DELIVERY_POOL_URI = "delivery-pool";

    private static final int RECEPTION_POOL_SIZE = 2;
    private static final int PROPAGATION_POOL_SIZE = 2;
    private static final int DELIVERY_POOL_SIZE = 4;

    protected final PrivilegedClientInboundPort bpip;
    protected final RegistrationInboundPort brip;

    protected final HashMap<String, Channel> channels;
    protected final HashMap<String, Client> clients;
    protected final ReadWriteLock channels_lock, clients_lock;

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

        // Pool instantiation using what is available in BCM4Java (also shuts down automatically)
        this.createNewExecutorService(RECEPTION_POOL_URI, RECEPTION_POOL_SIZE, false);
        this.createNewExecutorService(PROPAGATION_POOL_URI, PROPAGATION_POOL_SIZE, false);
        this.createNewExecutorService(DELIVERY_POOL_URI, DELIVERY_POOL_SIZE, false);

        // Default channels
        this.channels.put(DEFAULT_PUBLIC_CHANNEL, new Channel(null, ""));
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
        this.runTask(RECEPTION_POOL_URI, (FComponentTask) (owner) -> this.acceptPublication(channel, messages));
    }

    protected void submitPublication(String channel, List<MessageI> messages, String notificationInboundPortURI) {
        this.runTask(RECEPTION_POOL_URI, (FComponentTask) (owner) -> {
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
        this.runTask(PROPAGATION_POOL_URI, (FComponentTask) (owner) -> {
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
            this.runTask(PROPAGATION_POOL_URI, (FComponentTask) (owner) -> {
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
            if (entry.filter.match(message))
                this.runTask(DELIVERY_POOL_URI, (FComponentTask) (owner) -> this.deliverMessage(channel, message, entry.client));

    }

    protected void deliverMessage(String channel, MessageI message, Client client) {
        try {
            client.port.receive(channel, message);
        } catch (Exception e) {
            this.traceMessage("Delivery error to "
                    + client.receiving_uri + ": " + e.getMessage() + "\n");
        }
    }

    public void publish(String receptionPortURI, String channel, ArrayList<MessageI> messages) throws Exception {
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
                this.channels.get(channel).subscribers.remove(receptionPortURI);

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
        if (!this.channels.get(channel).isAuthorized(receptionPortURI))
            throw new UnauthorisedClientException();

        this.channels_lock.writeLock().lock();
        this.clients_lock.readLock().lock();
        try {
            this.channels.get(channel).subscribers.put(receptionPortURI, new Subscription(this.clients.get(receptionPortURI), filter));
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
            this.channels.get(channel).subscribers.remove(receptionPortURI);
            client.subscriptions.remove(channel);
        } finally {
            this.channels_lock.writeLock().unlock();
            this.clients_lock.readLock().unlock();
        }
    }

    public void modifyFilter(String receptionPortURI, String channel, MessageFilterI filter) throws Exception {
        if (filter == null)
            throw new IllegalArgumentException();
        if (!subscribed(receptionPortURI, channel))
            throw new NotSubscribedChannelException();

        this.channels_lock.writeLock().lock();
        this.clients_lock.readLock().lock();
        try {
            Client client = this.clients.get(receptionPortURI);
            Subscription sub = this.channels.get(channel).subscribers.get(receptionPortURI);
            if (sub == null)
                throw new RuntimeException("Client subscribed but Subscription not found");
            sub.filter = filter;
        } finally {
            this.channels_lock.writeLock().unlock();
            this.clients_lock.readLock().unlock();
        }
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
        return this.clients.get(receptionPortURI).rc == RegistrationCI.RegistrationClass.PREMIUM;
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
        return false; // TODO : R2.9
    }

    public void createChannel(String receptionPortURI, String channel, String regex) throws Exception {
        if (regex == null)
            throw new IllegalArgumentException();
        if (!isPremium(receptionPortURI))
            throw new UnauthorisedClientException();
        if (channelExist(channel))
            throw new AlreadyExistingChannelException();
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

    public void destroyChannelNow(String receptionPortURI, String channel) throws Exception {
        if (!hasCreatedChannel(receptionPortURI, channel))
            throw new NotOwnerException();

        this.channels_lock.writeLock().lock();
        try {
            Channel chan = this.channels.remove(channel);
            for (Subscription sub : chan.subscribers.values())
                sub.client.subscriptions.remove(channel);
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
}
