package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;

/**
 * Utilisé par tous les composants sauf le courtier, pour s'enregistrer auprès du courtier.
 */
public class BrokerRegistrationInboundPort extends AbstractInboundPort implements RegistrationCI {


    public BrokerRegistrationInboundPort(ComponentI owner) throws Exception {
        super(RegistrationCI.class, owner);
    }

    public BrokerRegistrationInboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, RegistrationCI.class, owner);
    }

    @Override
    public boolean registered(String receptionPortURI) throws Exception {
        return false;
    }

    @Override
    public boolean registered(String receptionPortURI, RegistrationClass rc) throws Exception {
        return false;
    }

    @Override
    public String register(String receptionPortURI, RegistrationClass rc) throws Exception {
        return "";
    }

    @Override
    public String modifyServiceClass(String receptionPortURI, RegistrationClass rc) throws Exception {
        return "";
    }

    @Override
    public void unregister(String receptionPortURI) throws Exception {

    }

    @Override
    public boolean channelExists(String channel) throws Exception {
        return false;
    }

    @Override
    public boolean subscribed(String receptionPortURI, String channel) throws Exception {
        return false;
    }

    @Override
    public void subscribe(String receptionPortURI, String channel, MessageFilterI filter) throws Exception {

    }

    @Override
    public void unsubscribe(String receptionPortURI, String channel) throws Exception {

    }

    @Override
    public boolean modifyFilter(String receptionPortURI, String channel, MessageFilterI filter) throws Exception {
        return false;
    }
}
