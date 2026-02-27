package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.cps.pubsub.interfaces.PrivilegedClientCI;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class PrivilegedClientOutboundPort extends PublishingOutboundPort implements PrivilegedClientCI {
    // TODO: Vérifier, peut-on extend PublishingOutboundPort? N'y a-t-il pas des problèmes dans le calcul d'instances
    public PrivilegedClientOutboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, PrivilegedClientCI.class, owner);
    }

    public PrivilegedClientOutboundPort(ComponentI owner) throws Exception {
        super(PrivilegedClientCI.class, owner);
    }

    @Override
    public boolean hasCreatedChannel(String receptionPortURI, String channel) throws Exception {
        return ((PrivilegedClientCI) this.getConnector()).hasCreatedChannel(receptionPortURI, channel);
    }

    @Override
    public boolean channelQuotaReached(String receptionPortURI) throws Exception {
        return ((PrivilegedClientCI) this.getConnector()).channelQuotaReached(receptionPortURI);
    }

    @Override
    public void createChannel(String receptionPortURI, String channel, String authorisedUsers) throws Exception {
        ((PrivilegedClientCI) this.getConnector()).createChannel(receptionPortURI, channel, authorisedUsers);
    }

    @Override
    public boolean isAuthorisedUser(String channel, String uri) throws Exception {
        return ((PrivilegedClientCI) this.getConnector()).isAuthorisedUser(channel, uri);
    }

    @Override
    public void modifyAuthorisedUsers(String receptionPortURI, String channel, String authorisedUsers) throws Exception {
        ((PrivilegedClientCI) this.getConnector()).modifyAuthorisedUsers(receptionPortURI, channel, authorisedUsers);
    }

    @Override
    public void removeAuthorisedUsers(String receptionPortURI, String channel, String regularExpression) throws Exception {
        ((PrivilegedClientCI) this.getConnector()).removeAuthorisedUsers(receptionPortURI, channel, regularExpression);
    }

    @Override
    public void destroyChannel(String receptionPortURI, String channel) throws Exception {
        ((PrivilegedClientCI) this.getConnector()).destroyChannel(receptionPortURI, channel);
    }

    @Override
    public void destroyChannelNow(String receptionPortURI, String channel) throws Exception {

    }
}
