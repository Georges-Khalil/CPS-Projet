package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.pubsub.interfaces.PublishingCI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.ports.ClientPublishingOutboundPort;
import fr.sorbonne_u.cps.pubsub.ports.ClientRegistrationOutboundPort;

/**
 * La station peut publier des messages en utilisant le publish du Broker
 */
@RequiredInterfaces(required = {PublishingCI.class, RegistrationCI.class})
public class Station extends AbstractComponent /*implements ClientI*/ {

    // Deux ports, un pour publish l'autre pour register
    protected ClientPublishingOutboundPort publish_port;
    private final String PUBLISH_PORT_URI;
    protected ClientRegistrationOutboundPort registration_port;
    private final String REGISTRATION_PORT_URI;

    protected Station() throws Exception {
        super(1,0); // Pour l'instant
        // Publish:
        this.publish_port = new ClientPublishingOutboundPort(this); // URI CRÉÉE AUTOMATIQUEMENT ICI
        this.PUBLISH_PORT_URI = this.publish_port.getPortURI();
        this.publish_port.publishPort();
        // Registration:
        this.registration_port = new ClientRegistrationOutboundPort(this);
        this.REGISTRATION_PORT_URI = this.registration_port.getPortURI();
        this.registration_port.publishPort();
    }

    @Override
    public synchronized void finalise() throws Exception {
        this.doPortDisconnection(PUBLISH_PORT_URI);
        this.doPortDisconnection(REGISTRATION_PORT_URI);
        super.finalise();
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.publish_port.unpublishPort();
            this.registration_port.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    public String getPUBLISH_PORT_URI() {
        return PUBLISH_PORT_URI;
    }
    public String getREGISTRATION_PORT_URI() {
        return REGISTRATION_PORT_URI;
    }

    @Override
    public synchronized void execute() throws Exception {
        super.execute();
        // TODO !!!
    }

    public void publish() {
        // TODO !
        // On va publier des messages avec ça (j'imagine), il faut ici appeler le broker avec notre port afin de publier.
    }
}
