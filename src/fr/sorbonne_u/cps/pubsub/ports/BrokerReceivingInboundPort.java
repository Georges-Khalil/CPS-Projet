package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.PublishingCI;
import fr.sorbonne_u.cps.pubsub.interfaces.ReceivingCI;

import java.util.ArrayList;


public class BrokerReceivingInboundPort implements ReceivingCI {

    @Override
    public void receive(String channel, MessageI message) throws Exception {

    }

    @Override
    public void receive(String channel, MessageI[] messages) throws Exception {

    }
}
