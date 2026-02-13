package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.PublishingCI;
import fr.sorbonne_u.cps.pubsub.interfaces.ReceivingCI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.ports.BrokerPublishingInboundPort;
import fr.sorbonne_u.cps.pubsub.ports.BrokerRegistrationInboundPort;

import java.util.ArrayList;

/**
 * C'est le courtier!
 * Offre: Publishing et Registration
 * Requiert: Receive
 */
@OfferedInterfaces(offered = {PublishingCI.class, RegistrationCI.class})
@RequiredInterfaces(required = {ReceivingCI.class})
public class Broker extends AbstractComponent {

    // On peut créer des ports uniques car le broker est unique,
    // mais ce n'est pas très maintainable si on décidait qu'il y aurait plusieurs instance de courtier
    public static final String BROKER_PUBLISH_URI = "broker-publish";
    protected BrokerPublishingInboundPort bpip;
    public static final String BROKER_REGISTRATION_URI = "broker-registration";
    protected BrokerRegistrationInboundPort brip;

    // Mais il y a beaucoup de clients qui offrent qqch à recevoir, donc il faut une structure de donnée pour leur ports
    // todo

    protected Broker() throws Exception {
        super(1,0); // Pour l'instant
        this.bpip = new BrokerPublishingInboundPort(BROKER_PUBLISH_URI, this);
        this.brip = new BrokerRegistrationInboundPort(BROKER_REGISTRATION_URI, this);
        this.bpip.publishPort();
        this.brip.publishPort();
    }

    @Override
    public void execute() throws Exception {
        //todo
    }

    @Override
    public synchronized void finalise() throws Exception {
        // todo: this.doPortDisconnection( les outboundports receive )
        super.finalise();
    }
    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.bpip.unpublishPort();
            this.brip.unpublishPort();
            // todo: unpublish les outboundport receive
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    public void publish(String receptionPortURI, String channel, MessageI message) throws Exception {
        //todo
    }

    public void publish(String receptionPortURI, String channel, ArrayList<MessageI> messages) throws Exception {
        //todo peut-être, on peut sûrement faire sans
    }


    public boolean registered(String receptionPortURI) throws Exception {
        return false //todo
    }

    // todo: registration
}
