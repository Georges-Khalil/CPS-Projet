package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.pubsub.composants.ClientI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.ReceivingCI;


public class ReceivingInboundPort extends AbstractInboundPort implements ReceivingCI {

    public ReceivingInboundPort(ComponentI owner) throws Exception {
        super(ReceivingCI.class, owner);
        assert owner instanceof ClientI;
    }

    public ReceivingInboundPort(String uri, ComponentI owner) throws Exception {
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
