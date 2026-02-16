package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.pubsub.composants.Broker;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.PrivilegedClientCI;
import fr.sorbonne_u.cps.pubsub.interfaces.PublishingCI;

import java.util.ArrayList;

/**
 *  Ce port implémente PublishingCI en tant que RequiredCI
 */
public class PrivilegedClientInboundPort extends PublishingInboundPort implements PrivilegedClientCI {

    // private static final long serialVersionUID = 1L;

    public PrivilegedClientInboundPort(ComponentI owner) throws Exception {
        super(owner);
    }

    // Facultatif, on peut imposer une URI
    public PrivilegedClientInboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, owner);
    }

    // todo: Remplir les fn en bas:
    @Override
    public boolean hasCreatedChannel(String receptionPortURI, String channel) throws Exception {
        return false;
    }

    @Override
    public boolean channelQuotaReached(String receptionPortURI) throws Exception {
        return false;
    }

    @Override
    public void createChannel(String receptionPortURI, String channel, String autorisedUsers) throws Exception {

    }

    @Override
    public boolean isAuthorisedUser(String channel, String uri) throws Exception {
        return false;
    }

    @Override
    public void modifyAuthorisedUsers(String receptionPortURI, String channel, String autorisedUsers) throws Exception {

    }

    @Override
    public void removeAuthorisedUsers(String receptionPortURI, String channel, String regularExpression) throws Exception {

    }

    @Override
    public void destroyChannel(String receptionPortURI, String channel) throws Exception {

    }

    @Override
    public void destroyChannelNow(String receptionPortURI, String channel) throws Exception {

    }
}
