package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.gossip.interfaces.GossipMessageI;
import fr.sorbonne_u.cps.gossip.interfaces.GossipSenderCI;
import fr.sorbonne_u.cps.pubsub.interfaces.PublishingCI;

public class GossipSenderOutboundPort extends AbstractOutboundPort implements GossipSenderCI {
    public GossipSenderOutboundPort(ComponentI owner) throws Exception {
        super(GossipSenderCI.class, owner);
    }

    @Override
    public void send(GossipMessageI[] gossipMessages) throws Exception {
        ((GossipSenderCI) this.getConnector()).send(gossipMessages);
    }
}
