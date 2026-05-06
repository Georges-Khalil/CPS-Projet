package fr.sorbonne_u.cps.pubsub.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.gossip.interfaces.GossipMessageI;
import fr.sorbonne_u.cps.gossip.interfaces.GossipReceiverCI;
import fr.sorbonne_u.cps.gossip.interfaces.GossipSenderCI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;

public class GossipConnector extends AbstractConnector implements GossipSenderCI {
    @Override
    public void send(GossipMessageI[] gossipMessages) throws Exception {
        ((GossipReceiverCI) this.offering).receive(gossipMessages);
    }
}
