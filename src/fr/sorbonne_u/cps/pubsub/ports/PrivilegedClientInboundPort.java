package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.cps.pubsub.composants.Broker;
import fr.sorbonne_u.cps.pubsub.interfaces.PrivilegedClientCI;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class PrivilegedClientInboundPort extends PublishingInboundPort implements PrivilegedClientCI {

    // private static final long serialVersionUID = 1L;

    public PrivilegedClientInboundPort(ComponentI owner) throws Exception {
        super(PrivilegedClientCI.class, owner);
    }

    public PrivilegedClientInboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, PrivilegedClientCI.class, owner);
    }

    @Override
    public boolean hasCreatedChannel(String receptionPortURI, String channel) throws Exception {
        return this.getOwner().handleRequest(
                c -> ((Broker) c).hasCreatedChannel(receptionPortURI, channel));
    }

    @Override
    public boolean channelQuotaReached(String receptionPortURI) throws Exception {
        return this.getOwner().handleRequest(
                c -> ((Broker) c).channelQuotaReached(receptionPortURI));
    }

    @Override
    public void createChannel(String receptionPortURI, String channel, String authorisedUsers) throws Exception {
        this.getOwner().handleRequest(
                c -> {
                    ((Broker) c).createChannel(receptionPortURI, channel, authorisedUsers);
                    return null;
                });
    }

    @Override
    public boolean isAuthorisedUser(String channel, String uri) throws Exception {
        return this.getOwner().handleRequest(
                c -> ((Broker) c).isAuthorisedUser(channel, uri));
    }

    @Override
    public void modifyAuthorisedUsers(String receptionPortURI, String channel, String authorisedUsers) throws Exception {
        this.getOwner().handleRequest(
                c -> {
                    ((Broker) c).modifyAuthorisedUsers(receptionPortURI, channel, authorisedUsers);
                    return null;
                });
    }

    @Override
    public void removeAuthorisedUsers(String receptionPortURI, String channel, String regularExpression) throws Exception {
        this.getOwner().handleRequest(
                c -> {
                    ((Broker) c).removeAuthorisedUsers(receptionPortURI, channel, regularExpression);
                    return null;
                });
    }

    @Override
    public void destroyChannel(String receptionPortURI, String channel) throws Exception {
        this.getOwner().handleRequest(
                c -> {
                    ((Broker) c).destroyChannel(receptionPortURI, channel);
                    return null;
                });
    }

    @Override
    public void destroyChannelNow(String receptionPortURI, String channel) throws Exception {
        this.getOwner().handleRequest(
                c -> {
                    ((Broker) c).destroyChannelNow(receptionPortURI, channel);
                    return null;
                });
    }
}
