package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
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

    protected Eolienne() throws Exception {

        super(1,0); // Pour l'instant
        // Publish:
        this.receive_port = new ClientReceivingInboundPort(this); // URI CRÉÉE AUTOMATIQUEMENT ICI
        this.RECEIVE_PORT_URI = this.receive_port.getPortURI();
        this.receive_port.publishPort();
        // Registration:
        this.registration_port = new ClientRegistrationOutboundPort(this);
        this.REGISTRATION_PORT_URI = this.registration_port.getPortURI();
        this.registration_port.publishPort();
    }

    @Override
    public synchronized void execute() throws Exception {
        super.execute();
        // TODO !!!
        // J'imagine qu'on se register et qu'on loop sur des receive bloquants?
    }

    @Override
    public void receive_one(String channel, MessageI message) {
        // Todo!
    }

    @Override
    public void receive_multiple(String channel, MessageI[] messages) {
        // Todo
    }

    public String getRECEIVE_PORT_URI() {
        return RECEIVE_PORT_URI;
    }
    public String getREGISTRATION_PORT_URI() {
        return REGISTRATION_PORT_URI;
    }
}
