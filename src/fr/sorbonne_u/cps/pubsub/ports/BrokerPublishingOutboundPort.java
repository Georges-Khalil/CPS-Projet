package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.RequiredCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.PublishingCI;

import java.util.ArrayList;

/**
 *  Ce port implémente PublishingCI en tant que RequiredCI
 */
public class BrokerPublishingOutboundPort extends AbstractOutboundPort implements PublishingCI {
    public BrokerPublishingOutboundPort(String uri, Class<? extends RequiredCI> implementedInterface, ComponentI owner) throws Exception {
        super(uri, implementedInterface, owner);
    }

    public BrokerPublishingOutboundPort(Class<? extends RequiredCI> implementedInterface, ComponentI owner) throws Exception {
        super(implementedInterface, owner);
    }

    @Override
    public void publish(String receptionPortURI, String channel, MessageI message) throws Exception {
        //todo
    }

    @Override
    public void publish(String receptionPortURI, String channel, ArrayList<MessageI> messages) throws Exception {
        //todo
    }
}
