package fr.sorbonne_u.cps.pubsub.connectors;

import java.util.ArrayList;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.PublishingCI;

import java.util.ArrayList;

public class PublishingConnector
        extends AbstractConnector
        implements PublishingCI
{

    @Override
    public void publish(String receptionPortURI, String channel, MessageI message) throws Exception {
        ((PublishingCI) this.offering).publish(receptionPortURI, channel, message);

    }

    @Override
    public void publish(String receptionPortURI, String channel, ArrayList<MessageI> messages) throws Exception {
        ((PublishingCI) this.offering).publish(receptionPortURI, channel, messages);
    }
}
