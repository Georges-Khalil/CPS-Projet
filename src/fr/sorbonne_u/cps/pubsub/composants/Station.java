package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.pubsub.interfaces.PublishingCI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.ports.ClientPublishingOutboundPort;

/**
 * La station peut publier des messages en utilisant le publish du Broker
 */
@RequiredInterfaces(required = {PublishingCI.class, RegistrationCI.class})
public class Station extends AbstractComponent implements ClientI {

    // Il n'y a qu'un seul port ici, pour publish
    protected ClientPublishingOutboundPort cpop;
    private final String CPOP_URI;

    protected Station() throws Exception {
        super(1,0); // Pour l'instant
        this.cpop = new ClientPublishingOutboundPort(this); // URI CRÉÉE AUTOMATIQUEMENT ICI
        this.CPOP_URI = this.cpop.getPortURI();
        this.cpop.publishPort();
    }

    @Override
    public synchronized void finalise() throws Exception {
        this.doPortDisconnection(CPOP_URI);
        super.finalise();
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.cpop.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    public String get_uri() {
        return CPOP_URI;
    } // Ca peut être utile.

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
