package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.PrivilegedClientCI;
import fr.sorbonne_u.cps.pubsub.interfaces.PublishingCI;

import java.util.ArrayList;

/**
 *  Ce port implémente PublishingCI en tant que OfferedCI
 */
public class PrivilegedClientOutboundPort extends PublishingOutboundPort implements PrivilegedClientCI {
    // TODO: Vérifier, peut-on extend PublishingOutboundPort? N'y a-t-il pas des problèmes dans le calcul d'instances
    public PrivilegedClientOutboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, PrivilegedClientCI.class, owner);
    }

    public PrivilegedClientOutboundPort(ComponentI owner) throws Exception {
        super(PrivilegedClientCI.class, owner);
    }

    // Todo: remplir les fns:
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
