package fr.sorbonne_u.cps.pubsub.message;

import fr.sorbonne_u.cps.gossip.interfaces.GossipMessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;

import java.time.Instant;

public class GossipMessage implements GossipMessageI {

    public enum MessageType {
        PUBLICATION, REGISTRATION, CHANNEL_CREATION, CHANNEL_DESTRUCTION
    }

    private final String uri;
    public final Instant timestamp;
    public final String emitterURI;

    public final MessageType type;

    public final MessageI pubSubMessage;
    public final String channel;

    public final String clientURI;
    public final RegistrationCI.RegistrationClass clientRC;

    public final String channelName;
    public final String channelRegex; // null = destruction
    public final String channelOwner;

    public GossipMessage(String uri, Instant timestamp, String emitterURI, MessageType type, MessageI pubSubMessage, String channel, String clientURI, RegistrationCI.RegistrationClass clientRC, String channelName, String channelRegex, String channelOwner) {
        this.uri = uri;
        this.timestamp = timestamp;
        this.emitterURI = emitterURI;
        this.type = type;
        this.pubSubMessage = pubSubMessage;
        this.channel = channel;
        this.clientURI = clientURI;
        this.clientRC = clientRC;
        this.channelName = channelName;
        this.channelRegex = channelRegex;
        this.channelOwner = channelOwner;
    }

    @Override
    public String gossipMessageURI() {
        return this.uri;
    }

    @Override
    public Instant timestamp() {
        return this.timestamp;
    }

    public GossipMessage copyWithNewEmitterURI(String newURI) {
        return new GossipMessage(this.uri, this.timestamp, newURI, this.type, this.pubSubMessage, this.channel, this.clientURI, this.clientRC, this.channelName, this.channelRegex, this.channelOwner);
    }
}
