package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.pubsub.composants.Broker;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;

/**
 * Used by components to register to the broker
 *
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class RegistrationInboundPort extends AbstractInboundPort implements RegistrationCI {

    public RegistrationInboundPort(ComponentI owner) throws Exception {
        super(RegistrationCI.class, owner);
        assert owner instanceof Broker;
    }

    public RegistrationInboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, RegistrationCI.class, owner);
        assert owner instanceof Broker;
    }

    @Override
    public boolean registered(String receptionPortURI) throws Exception {
        return this.getOwner().handleRequest(
                c -> ((Broker) c).registered(receptionPortURI));

//        // Si on suit les exemples, on peut avoir cette forme la:
//        return this.getOwner().handleRequest(
//                new AbstractComponent.AbstractService<Boolean>() {
//                    @SuppressWarnings("unchecked")
//                    @Override
//                    public Boolean call() throws Exception {
//                        return ((Broker) this.getServiceOwner()).registered(receptionPortURI) ;
//                    }
//                }) ;
    }

    @Override
    public boolean registered(String receptionPortURI, RegistrationClass rc) throws Exception {
        return this.getOwner().handleRequest(
                c -> ((Broker) c).registered(receptionPortURI, rc));

    }

    @Override
    public String register(String receptionPortURI, RegistrationClass rc) throws Exception {
        return this.getOwner().handleRequest(
                c -> ((Broker) c).register(receptionPortURI, rc));

    }

    @Override
    public String modifyServiceClass(String receptionPortURI, RegistrationClass rc) throws Exception {
        return this.getOwner().handleRequest(
                c -> ((Broker) c).modifyServiceClass(receptionPortURI, rc));

    }

    @Override
    public void unregister(String receptionPortURI) throws Exception {
        this.getOwner().handleRequest(
                c -> {
                    ((Broker) c).unregister(receptionPortURI);
                    return null;
                } );
    }

  @Override
  public boolean channelAuthorised(String receptionPortURI, String channel) throws Exception {
    return this.getOwner().handleRequest(
            c -> ((Broker) c).channelAuthorised(receptionPortURI, channel));
  }

  @Override
    public boolean channelExist(String channel) throws Exception {
        return this.getOwner().handleRequest(
                c -> ((Broker) c).channelExist(channel));
    }

    @Override
    public boolean subscribed(String receptionPortURI, String channel) throws Exception {
        return this.getOwner().handleRequest(
                c -> ((Broker) c).subscribed(receptionPortURI, channel));
    }

    @Override
    public void subscribe(String receptionPortURI, String channel, MessageFilterI filter) throws Exception {
        this.getOwner().handleRequest(
                c -> {
                    ((Broker) c).subscribe(receptionPortURI, channel, filter);
                    return null;
                } );
    }

    @Override
    public void unsubscribe(String receptionPortURI, String channel) throws Exception {
        this.getOwner().handleRequest(
                c -> {
                    ((Broker) c).unsubscribe(receptionPortURI, channel);
                    return null;
                } );
    }

    @Override
    public boolean modifyFilter(String receptionPortURI, String channel, MessageFilterI filter) throws Exception {
        return this.getOwner().handleRequest(
                c -> ((Broker) c).modifyFilter(receptionPortURI, channel, filter));
    }
}
