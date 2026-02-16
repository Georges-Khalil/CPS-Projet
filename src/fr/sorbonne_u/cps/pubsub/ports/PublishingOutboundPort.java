package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.PublishingCI;

import java.util.ArrayList;

/**
 *  Ce port implémente PublishingCI en tant que OfferedCI
 */
public class PublishingOutboundPort extends AbstractOutboundPort implements PublishingCI {
    public PublishingOutboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, PublishingCI.class, owner);
    }

    public PublishingOutboundPort(ComponentI owner) throws Exception {
        super(PublishingCI.class, owner);
    }

    @Override
    public void publish(String receptionPortURI, String channel, MessageI message) throws Exception {
        ((PublishingCI) this.getConnector()).publish(receptionPortURI, channel, message);
    }

    @Override
    public void publish(String receptionPortURI, String channel, ArrayList<MessageI> messages) throws Exception {
        ((PublishingCI) this.getConnector()).publish(receptionPortURI, channel, messages);
    }
}
