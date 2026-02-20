package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;

import fr.sorbonne_u.cps.pubsub.composants.Broker;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.PublishingCI;

import java.util.ArrayList;

/**
 *  Ce port implémente PublishingCI en tant que RequiredCI
 */
public class PublishingInboundPort extends AbstractInboundPort implements PublishingCI {

    // private static final long serialVersionUID = 1L;

    public PublishingInboundPort(ComponentI owner) throws Exception {
        super(PublishingCI.class, owner);
        assert owner instanceof Broker;
    }

    // Constructeur utilisé par PrivilegedClientInboundPort
    protected PublishingInboundPort(
            Class<? extends OfferedCI> implementedInterface,
            ComponentI owner
    ) throws Exception {
        super(implementedInterface, owner);
        assert owner instanceof Broker;
    }

    // Facultatif, on peut imposer une URI
    public PublishingInboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, PublishingCI.class, owner);
        assert owner instanceof Broker;
    }
    // Constructeur utilisé par PrivilegedClientInboundPort
    protected PublishingInboundPort(
            String uri,
            Class<? extends OfferedCI> implementedInterface,
            ComponentI owner
    ) throws Exception {
        super(uri, implementedInterface, owner);
        assert owner instanceof Broker;
    }

    @Override
    public void publish(String receptionPortURI, String channel, MessageI message) throws Exception {
        this.getOwner().handleRequest(
                c -> {
                    ((Broker) c).publish(receptionPortURI, channel, message);
                    return null;
                });
        /*
        // Les exemples proposent cette implémentation :
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
        */
    }

    @Override
    public void publish(String receptionPortURI, String channel, ArrayList<MessageI> messages) throws Exception {
        this.getOwner().handleRequest(
                c -> {
                    ((Broker) c).publish(receptionPortURI, channel, messages);
                    return null;
                });

        /*
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

         */
    }
}
