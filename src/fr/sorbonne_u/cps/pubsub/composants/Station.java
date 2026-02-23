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
import fr.sorbonne_u.cps.pubsub.meteo.Position;
import fr.sorbonne_u.cps.pubsub.meteo.WindData;
import fr.sorbonne_u.cps.pubsub.ports.PublishingOutboundPort;
import fr.sorbonne_u.cps.pubsub.ports.ReceivingInboundPort;
import fr.sorbonne_u.cps.pubsub.ports.RegistrationOutboundPort;

/**
 * La station météorologique publie des données de vent sur le système pub/sub.
 */
@OfferedInterfaces(offered = {ReceivingCI.class})
@RequiredInterfaces(required = {PublishingCI.class, RegistrationCI.class})
public class Station extends AbstractComponent implements ClientI {

    protected ReceivingInboundPort receive_port;
    private final String RECEIVE_PORT_URI;
    protected PublishingOutboundPort publish_port;
    private final String PUBLISH_PORT_URI;
    protected RegistrationOutboundPort registration_port;
    private final String REGISTRATION_PORT_URI;

    protected Station(String receive_port_uri, String publish_port_uri, String registration_port_uri) throws Exception {
        super(1, 0);
        // Receive:
        this.receive_port = new ReceivingInboundPort(receive_port_uri, this);
        this.RECEIVE_PORT_URI = receive_port_uri;
        this.receive_port.publishPort();
        // Publish:
        this.publish_port = new PublishingOutboundPort(publish_port_uri, this);
        this.PUBLISH_PORT_URI = publish_port_uri;
        this.publish_port.publishPort();
        // Registration:
        this.registration_port = new RegistrationOutboundPort(registration_port_uri, this);
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

        // Publier un message avec WindData comme payload
        Position position = new Position(10, 20);
        WindData windData = new WindData(position, 10.0, 5.0);
        Message msg = new Message(windData);
        msg.putProperty("type", "wind");
        msg.putProperty("station", RECEIVE_PORT_URI);

        this.publish_port.publish(RECEIVE_PORT_URI, "channel0", msg);
        this.traceMessage("Station " + RECEIVE_PORT_URI + ": message vent publie sur channel0 - " + windData + "\n");
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
    public void receiveOne(String channel, MessageI message) {
        // La station est un publieur, pas un souscripteur
    }

    @Override
    public void receiveMultiple(String channel, MessageI[] messages) {
        // La station est un publieur, pas un souscripteur
    }
}
