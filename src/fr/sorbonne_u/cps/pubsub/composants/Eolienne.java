package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.ReceivingCI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.filters.MessageFilter;
import fr.sorbonne_u.cps.pubsub.ports.ClientReceivingInboundPort;
import fr.sorbonne_u.cps.pubsub.ports.ClientRegistrationOutboundPort;

/**
 * L'éolienne s'abonne aux canaux et reçoit les messages (vent, alertes).
 */
@OfferedInterfaces(offered = {ReceivingCI.class})
@RequiredInterfaces(required = {RegistrationCI.class})
public class Eolienne extends AbstractComponent implements ClientI {

    protected ClientReceivingInboundPort receive_port;
    private final String RECEIVE_PORT_URI;
    protected ClientRegistrationOutboundPort registration_port;
    private final String REGISTRATION_PORT_URI;

    protected Eolienne(String receive_port_uri, String registration_port_uri) throws Exception {
        super(1, 0);
        // Receive:
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

        // S'enregistrer auprès du courtier
        this.registration_port.register(RECEIVE_PORT_URI, RegistrationCI.RegistrationClass.FREE);
        this.traceMessage("Eolienne " + RECEIVE_PORT_URI + ": enregistree\n");

        // S'abonner au channel0 avec un filtre qui accepte tout
        this.registration_port.subscribe(RECEIVE_PORT_URI, "channel0", new MessageFilter());
        this.traceMessage("Eolienne " + RECEIVE_PORT_URI + ": abonnee a channel0\n");

        this.registration_port.subscribe(RECEIVE_PORT_URI, "channel1", new MessageFilter());
        this.traceMessage("Eolienne " + RECEIVE_PORT_URI + ": abonnee a channel1\n");
    }

    @Override
    public synchronized void finalise() throws Exception {
        this.doPortDisconnection(REGISTRATION_PORT_URI);
        super.finalise();
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.receive_port.unpublishPort();
            this.registration_port.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    @Override
    public void receive_one(String channel, MessageI message) {
        this.traceMessage("Eolienne " + RECEIVE_PORT_URI + ": message recu sur " + channel +
                " | payload=" + message.getPayload() + "\n");
    }

    @Override
    public void receive_multiple(String channel, MessageI[] messages) {
        for (MessageI m : messages) {
            receive_one(channel, m);
        }
    }
}
