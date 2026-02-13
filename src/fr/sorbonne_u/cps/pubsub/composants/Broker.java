package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.PublishingCI;
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
public class Broker extends AbstractComponent {

    public static final String BROKER_PUBLISH_URI = "broker-publish";
    protected BrokerPublishingInboundPort bpip;
    public static final String BROKER_REGISTRATION_URI = "broker-registration";
    protected BrokerRegistrationInboundPort brip;

    protected Broker() throws Exception {
        super(1,0); // Pour l'instant
        this.bpip = new BrokerPublishingInboundPort(BROKER_PUBLISH_URI, this);
        this.brip = new BrokerRegistrationInboundPort(BROKER_REGISTRATION_URI, this);
    }

    public void publish(String receptionPortURI, String channel, MessageI message) throws Exception {
        //todo
    }

    public void publish(String receptionPortURI, String channel, ArrayList<MessageI> messages) throws Exception {
        //todo peut-être, on peut sûrement faire sans
    }

    // todo: registration
}
