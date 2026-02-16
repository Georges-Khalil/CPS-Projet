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
 * Le bureau météorologique publie des alertes météo sur le système pub/sub.
 */
@OfferedInterfaces(offered = {ReceivingCI.class})
@RequiredInterfaces(required = {PublishingCI.class, RegistrationCI.class})
public class Bureau extends AbstractComponent implements ClientI {

    protected ClientReceivingInboundPort receive_port;
    private final String RECEIVE_PORT_URI;
    protected ClientPublishingOutboundPort publish_port;
    private final String PUBLISH_PORT_URI;
    protected ClientRegistrationOutboundPort registration_port;
    private final String REGISTRATION_PORT_URI;

    protected Bureau(String receive_port_uri, String publish_port_uri, String registration_port_uri) throws Exception {
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
        this.traceMessage("Bureau " + RECEIVE_PORT_URI + ": enregistre\n");

        // Attendre que les abonnes soient prets
        Thread.sleep(3000);

        // Publier un message simple d'alerte meteo
        Message msg = new Message();
        msg.putProperty("type", "alert");
        msg.putProperty("alertType", "STORM");
        msg.putProperty("level", "ORANGE");
        msg.setPayload("Alerte tempete emise par " + RECEIVE_PORT_URI);

        this.publish_port.publish(RECEIVE_PORT_URI, "channel1", msg);
        this.traceMessage("Bureau " + RECEIVE_PORT_URI + ": alerte publiee sur channel1\n");
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
        // Le bureau est un publieur, pas un souscripteur
    }

    @Override
    public void receive_multiple(String channel, MessageI[] messages) {
        // Le bureau est un publieur, pas un souscripteur
    }
}
