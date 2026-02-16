package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.ReceivingCI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.ports.ClientPublishingOutboundPort;
import fr.sorbonne_u.cps.pubsub.ports.ClientReceivingInboundPort;
import fr.sorbonne_u.cps.pubsub.ports.ClientRegistrationOutboundPort;

@OfferedInterfaces(offered = {ReceivingCI.class})
@RequiredInterfaces(required = {RegistrationCI.class})
public class Eolienne extends AbstractComponent implements ClientI {
    // Deux ports, un pour publish l'autre pour register
    protected ClientReceivingInboundPort receive_port;
    private final String RECEIVE_PORT_URI;
    protected ClientRegistrationOutboundPort registration_port;
    private final String REGISTRATION_PORT_URI;

    protected Eolienne(String receive_port_uri, String registration_port_uri) throws Exception {

        super(1,0); // Pour l'instant
        // Publish:
        this.receive_port = new ClientReceivingInboundPort(receive_port_uri, this);
        this.RECEIVE_PORT_URI = receive_port_uri;
        this.receive_port.publishPort();
        // Registration:
        this.registration_port = new ClientRegistrationOutboundPort(registration_port_uri, this);
        this.REGISTRATION_PORT_URI = registration_port_uri;
        this.registration_port.publishPort();
    }

    @Override
    public synchronized void execute() throws Exception {
        super.execute();
        // TODO !!!
        // J'imagine qu'on se register et qu'on loop sur des receive bloquants?
    }

    @Override
    public synchronized void finalise() throws Exception {
        // On déconnecte les OUTBOUND_PORT donc pas le RECEIVE_PORT car il est INBOUND
        this.doPortDisconnection(REGISTRATION_PORT_URI);
        super.finalise();
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.registration_port.unpublishPort();
            this.registration_port.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }


    @Override
    public void receive_one(String channel, MessageI message) {
        // Todo!
    }

    @Override
    public void receive_multiple(String channel, MessageI[] messages) {
        // Todo
    }

}
