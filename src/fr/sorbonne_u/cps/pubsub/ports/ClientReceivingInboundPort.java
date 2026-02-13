package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.PublishingCI;
import fr.sorbonne_u.cps.pubsub.interfaces.ReceivingCI;

import java.util.ArrayList;


public class ClientReceivingInboundPort extends AbstractInboundPort implements ReceivingCI {

    public ClientReceivingInboundPort(ComponentI owner) throws Exception {
        super(ReceivingCI.class, owner);
    }

    public ClientReceivingInboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, ReceivingCI.class, owner);
    }

    @Override
    public void receive(String channel, MessageI message) throws Exception {

    }

    @Override
    public void receive(String channel, MessageI[] messages) throws Exception {

    }
}
