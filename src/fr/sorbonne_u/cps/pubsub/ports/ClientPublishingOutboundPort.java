package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.PublishingCI;

import java.util.ArrayList;

/**
 *  Ce port implémente PublishingCI en tant que OfferedCI
 */
public class ClientPublishingOutboundPort extends AbstractOutboundPort implements PublishingCI {
    @Override
    public void publish(String receptionPortURI, String channel, MessageI message) throws Exception {
        //todo
    }

    @Override
    public void publish(String receptionPortURI, String channel, ArrayList<MessageI> messages) throws Exception {
        //todo
    }
}
