package fr.sorbonne_u.cps.pubsub.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;

public class RegistrationConnector
        extends AbstractConnector
        implements RegistrationCI
{

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
