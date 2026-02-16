package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.PublishingCI;
import fr.sorbonne_u.cps.pubsub.interfaces.ReceivingCI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.message.Message;
import fr.sorbonne_u.cps.pubsub.ports.ClientPublishingOutboundPort;
import fr.sorbonne_u.cps.pubsub.ports.ClientReceivingInboundPort;
import fr.sorbonne_u.cps.pubsub.ports.ClientRegistrationOutboundPort;

/**
 * La station météorologique publie des données de vent sur le système pub/sub.
 */
@OfferedInterfaces(offered = {ReceivingCI.class})
@RequiredInterfaces(required = {PublishingCI.class, RegistrationCI.class})
public class Station extends AbstractComponent implements ClientI {

    protected ClientReceivingInboundPort receive_port;
    private final String RECEIVE_PORT_URI;
    protected ClientPublishingOutboundPort publish_port;
    private final String PUBLISH_PORT_URI;
    protected ClientRegistrationOutboundPort registration_port;
    private final String REGISTRATION_PORT_URI;

    protected Station(String receive_port_uri, String publish_port_uri, String registration_port_uri) throws Exception {
        super(1, 0);
        // Receive:
        this.receive_port = new ClientReceivingInboundPort(receive_port_uri, this);
        this.RECEIVE_PORT_URI = receive_port_uri;
        this.receive_port.publishPort();
        // Publish:
        this.publish_port = new ClientPublishingOutboundPort(publish_port_uri, this);
        this.PUBLISH_PORT_URI = publish_port_uri;
        this.publish_port.publishPort();
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
        this.traceMessage("Station " + RECEIVE_PORT_URI + ": enregistree\n");

        // Attendre que les abonnes soient prets
        Thread.sleep(3000);

        // Publier un message simple de donnees de vent
        Message msg = new Message();
        msg.putProperty("type", "wind");
        msg.putProperty("windSpeedX", 10.0);
        msg.putProperty("windSpeedY", 5.0);
        msg.putProperty("station", RECEIVE_PORT_URI);
        msg.setPayload("Donnees de vent de " + RECEIVE_PORT_URI);

        this.publish_port.publish(RECEIVE_PORT_URI, "channel0", msg);
        this.traceMessage("Station " + RECEIVE_PORT_URI + ": message vent publie sur channel0\n");
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
            this.receive_port.unpublishPort();
            this.publish_port.unpublishPort();
            this.registration_port.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    @Override
    public void receive_one(String channel, MessageI message) {
        // La station est un publieur, pas un souscripteur
    }

    @Override
    public void receive_multiple(String channel, MessageI[] messages) {
        // La station est un publieur, pas un souscripteur
    }
}
