package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.ReceivingCI;

public class BrokerReceivingOutboundPort extends AbstractOutboundPort implements ReceivingCI {

    @Override
    public void receive(String channel, MessageI message) throws Exception {

    }

    @Override
    public void receive(String channel, MessageI[] messages) throws Exception {

    }
}
