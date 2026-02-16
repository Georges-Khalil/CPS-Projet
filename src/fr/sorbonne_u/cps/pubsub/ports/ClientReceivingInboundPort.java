package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.pubsub.composants.Broker;
import fr.sorbonne_u.cps.pubsub.composants.ClientI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.PublishingCI;
import fr.sorbonne_u.cps.pubsub.interfaces.ReceivingCI;

import java.util.ArrayList;


public class ClientReceivingInboundPort extends AbstractInboundPort implements ReceivingCI {

    public ClientReceivingInboundPort(ComponentI owner) throws Exception {
        super(ReceivingCI.class, owner);
        assert owner instanceof ClientI;
    }

    public ClientReceivingInboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, ReceivingCI.class, owner);
        assert owner instanceof ClientI;
    }

    @Override
    public void receive(String channel, MessageI message) throws Exception {
        this.getOwner().handleRequest(
                c -> {
                    ((ClientI) c).receive_one(channel, message);
                    return null;
                });

    }

    @Override
    public void receive(String channel, MessageI[] messages) throws Exception {
        this.getOwner().handleRequest(
                c -> {
                    ((ClientI) c).receive_multiple(channel, messages);
                    return null;
                });

    }
}
