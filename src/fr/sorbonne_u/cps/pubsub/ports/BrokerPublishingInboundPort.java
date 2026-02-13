package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

import fr.sorbonne_u.cps.pubsub.composants.Broker;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.PublishingCI;

import java.util.ArrayList;

/**
 *  Ce port implémente PublishingCI en tant que RequiredCI
 */
public class BrokerPublishingInboundPort extends AbstractInboundPort implements PublishingCI {

    private static final long serialVersionUID = 1L;

    public BrokerPublishingInboundPort(ComponentI owner) throws Exception {
        super(PublishingCI.class, owner);
        assert owner instanceof Broker;
    }

    // Facultatif, on peut imposer une URI
    public BrokerPublishingInboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, PublishingCI.class, owner);
    }

    @Override
    public void publish(String receptionPortURI, String channel, MessageI message) throws Exception {
        this.getOwner().handleRequest(
                c -> ((Broker) c).publish(this.getPortURI(), channel, message)
        );
    }

    @Override
    public void publish(String receptionPortURI, String channel, ArrayList<MessageI> messages) throws Exception {
        this.getOwner().handleRequest(
                c -> ((Broker) c).publish(this.getPortURI(), channel, messages)
        );
    }
}
