package fr.sorbonne_u.cps.pubsub.message;

import fr.sorbonne_u.cps.gossip.interfaces.GossipMessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class GossipMessage implements GossipMessageI {

    public enum MessageType {
        PUBLICATION, REGISTRATION, CHANNEL_CREATION, CHANNEL_DESTRUCTION, CHANNEL_REGEX_UPDATE, CLIENT_RC_UPDATE
    }

    private final String uri;
    private final Instant timestamp;
    private final String emitterURI;
    private final MessageType type;

    // PUBLICATION
    private final List<MessageI> messages;
    private final String channel;

    // REGISTRATION
    private final String clientURI;
    private final RegistrationCI.RegistrationClass clientRC;

    // CHANNEL_CREATION / CHANNEL_DESTRUCTION
    private final String channelName;
    private final String channelRegex;
    private final String channelOwner;

    private GossipMessage(
            String uri, String emitterURI, MessageType type,
            List<MessageI> messages, String channel,
            String clientURI, RegistrationCI.RegistrationClass clientRC,
            String channelName, String channelRegex, String channelOwner
    ) {
        this.uri = uri;
        this.timestamp = Instant.now();
        this.emitterURI = emitterURI;
        this.type = type;
        this.messages = messages;
        this.channel = channel;
        this.clientURI = clientURI;
        this.clientRC = clientRC;
        this.channelName = channelName;
        this.channelRegex = channelRegex;
        this.channelOwner = channelOwner;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // GossipMessage Factories (for each type of message)
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    public static GossipMessage publication(String emitterURI, String channel, List<MessageI> messages) {
        return new GossipMessage(
                UUID.randomUUID().toString(), emitterURI,
                MessageType.PUBLICATION,
                Collections.unmodifiableList(messages), channel,
                null, null, null, null, null
        );
    }

    public static GossipMessage registration(String emitterURI, String clientURI,
                                             RegistrationCI.RegistrationClass rc) {
        return new GossipMessage(
                UUID.randomUUID().toString(), emitterURI,
                MessageType.REGISTRATION,
                null, null,
                clientURI, rc,
                null, null, null
        );
    }

    public static GossipMessage channelCreation(String emitterURI, String channelName,
                                                String channelOwner, String regex) {
        return new GossipMessage(
                UUID.randomUUID().toString(), emitterURI,
                MessageType.CHANNEL_CREATION,
                null, null,
                null, null,
                channelName, regex, channelOwner
        );
    }

    public static GossipMessage channelDestruction(String emitterURI, String channelName) {
        return new GossipMessage(
                UUID.randomUUID().toString(), emitterURI,
                MessageType.CHANNEL_DESTRUCTION,
                null, null,
                null, null,
                channelName, null, null
        );
    }

    public static GossipMessage channelRegexUpdate(String emitterURI,
                                                   String channelName, String newRegex) {
        return new GossipMessage(
                UUID.randomUUID().toString(), emitterURI,
                MessageType.CHANNEL_REGEX_UPDATE,
                null, null, null, null,
                channelName, newRegex, null
        );
    }

    public static GossipMessage clientRcUpdate(String emitterURI,
                                               String clientURI,
                                               RegistrationCI.RegistrationClass rc) {
        return new GossipMessage(
                UUID.randomUUID().toString(), emitterURI,
                MessageType.CLIENT_RC_UPDATE,
                null, null,
                clientURI, rc,
                null, null, null
        );
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // Overriden methods from GossipMessageI
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @Override
    public String gossipMessageURI() {
        return this.uri;
    }

    @Override
    public Instant timestamp() {
        return this.timestamp;
    }

    @Override
    public GossipMessage copyWithNewEmitterURI(String newEmitterURI) {
        return new GossipMessage(
                this.uri, newEmitterURI, this.type,
                this.messages, this.channel,
                this.clientURI, this.clientRC,
                this.channelName, this.channelRegex, this.channelOwner
        );
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // Getters (we make sure that only the right message type is allowed to access some of the fields) #invariants
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  - - - - - - - - - - - - - - - - - -

    public MessageType getType() { return type; }

    public String getEmitterURI() { return emitterURI; }

    // Only for PUBLICATION
    public List<MessageI> getPubSubMessages() {
        assertType(MessageType.PUBLICATION);
        return messages;
    }

    // Only for PUBLICATION
    public String getChannel() {
        assertType(MessageType.PUBLICATION);
        return channel;
    }

    // Only for REGISTRATION
    public String getClientURI() {
        if (type != MessageType.REGISTRATION && type != MessageType.CLIENT_RC_UPDATE)
            throw new IllegalStateException("Not a client message: " + type);
        return clientURI;
    }

    public RegistrationCI.RegistrationClass getClientRC() {
        if (type != MessageType.REGISTRATION && type != MessageType.CLIENT_RC_UPDATE)
            throw new IllegalStateException("Not a client message: " + type);
        return clientRC;
    }

    public String getChannelName() {
        if (type != MessageType.CHANNEL_CREATION && type != MessageType.CHANNEL_DESTRUCTION
                && type != MessageType.CHANNEL_REGEX_UPDATE)
            throw new IllegalStateException("Not a channel message: " + type);
        return channelName;
    }

    public String getChannelRegex() {
        if (type != MessageType.CHANNEL_CREATION && type != MessageType.CHANNEL_REGEX_UPDATE)
            throw new IllegalStateException("Not a channel message: " + type);
        return channelRegex;
    }

    // Only for CHANNEL_CREATION
    public String getChannelOwner() {
        assertType(MessageType.CHANNEL_CREATION);
        return channelOwner;
    }

    private void assertType(MessageType expected) {
        if (this.type != expected)
            throw new IllegalStateException("Expected " + expected + " but got " + type);
    }

}
