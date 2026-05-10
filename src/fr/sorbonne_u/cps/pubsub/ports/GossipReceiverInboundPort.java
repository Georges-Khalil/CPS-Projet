package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.gossip.interfaces.GossipImplementationI;
import fr.sorbonne_u.cps.gossip.interfaces.GossipMessageI;
import fr.sorbonne_u.cps.gossip.interfaces.GossipReceiverCI;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class GossipReceiverInboundPort extends AbstractInboundPort implements GossipReceiverCI {
    public GossipReceiverInboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, GossipReceiverCI.class, owner);
    }

    @Override
    public void receive(GossipMessageI[] gossipMessages) throws Exception {
        this.getOwner().handleRequest(
                c -> {
                    ((GossipImplementationI) c).receive(gossipMessages);
                    return null;
                });
    }
}
