package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.examples.ddeployment_cs.components.DynamicURIConsumer;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

import fr.sorbonne_u.cps.pubsub.composants.Broker;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.PublishingCI;

import java.util.ArrayList;

/**
 *  Ce port implémente PublishingCI en tant que RequiredCI
 */
public class BrokerPublishingInboundPort extends AbstractInboundPort implements PublishingCI {

    // private static final long serialVersionUID = 1L;

    public BrokerPublishingInboundPort(ComponentI owner) throws Exception {
        super(PublishingCI.class, owner);
        assert owner instanceof Broker;
    }

    // Facultatif, on peut imposer une URI
    public BrokerPublishingInboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, PublishingCI.class, owner);
        assert owner instanceof Broker;
    }

    @Override
    public void publish(String receptionPortURI, String channel, MessageI message) {
        /*
        // Dans la vidéo c'est implémenté comme ça :
        this.getOwner().handleRequest(
            c -> ((Broker) c).publish(this.getPortURI(), channel, message)
        );
         */

        // Mais les exemples proposent ça :
        this.owner.runTask(
                new AbstractComponent.AbstractTask() {
                    @Override
                    public void run() {
                        try {
                            ((Broker)this.getTaskOwner()).
                                    publish(this.getTaskOwner().getReflectionInboundPortURI(),  channel, message);
                        } catch (Throwable e) {
                            e.printStackTrace(); ;
                        }
                    }
                }) ;
    }

    @Override
    public void publish(String receptionPortURI, String channel, ArrayList<MessageI> messages) {
        this.owner.runTask(
                new AbstractComponent.AbstractTask() {
                    @Override
                    public void run() {
                        try {
                            ((Broker)this.getTaskOwner()).
                                    publish(this.getTaskOwner().getReflectionInboundPortURI(),  channel, messages); ;
                        } catch (Throwable e) {
                            e.printStackTrace(); ;
                        }
                    }
                }) ;
    }
}
