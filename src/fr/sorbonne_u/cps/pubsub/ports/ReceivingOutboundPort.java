package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.pubsub.connectors.ReceivingConnector;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.ReceivingCI;

public class ReceivingOutboundPort extends AbstractOutboundPort implements ReceivingCI {

    public ReceivingOutboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, ReceivingCI.class, owner);
    }

    public ReceivingOutboundPort(ComponentI owner) throws Exception {
        super(ReceivingCI.class, owner);
    }

    @Override
    public void receive(String channel, MessageI message) throws Exception {
        ((ReceivingConnector) this.getConnector()).receive(channel, message);
    }

    @Override
    public void receive(String channel, MessageI[] messages) throws Exception {
        ((ReceivingConnector) this.getConnector()).receive(channel, messages);
    }
}
