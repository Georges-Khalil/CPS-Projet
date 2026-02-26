package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.pubsub.connectors.PublishingConnector;
import fr.sorbonne_u.cps.pubsub.connectors.RegistrationConnector;
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
import fr.sorbonne_u.cps.pubsub.utils.URIGenerator;

/**
 * The station sends data on the pub/sub system.
 */
@OfferedInterfaces(offered = {ReceivingCI.class})
@RequiredInterfaces(required = {PublishingCI.class, RegistrationCI.class})
public class Station extends AbstractComponent implements ClientI {

    protected final ReceivingInboundPort receive_port;
    protected final String RECEIVE_PORT_URI;
    protected final PublishingOutboundPort publish_port;
    protected final String PUBLISH_PORT_URI;
    protected final RegistrationOutboundPort registration_port;
    protected final String REGISTRATION_PORT_URI;

    protected Station() throws Exception {
        super(1, 0);
        this.REGISTRATION_PORT_URI = URIGenerator.getNew(this);
        this.RECEIVE_PORT_URI = URIGenerator.getNew(this);
        this.PUBLISH_PORT_URI = URIGenerator.getNew(this);

        this.registration_port = new RegistrationOutboundPort(this.REGISTRATION_PORT_URI, this);
        this.registration_port.publishPort();
        this.receive_port = new ReceivingInboundPort(this.RECEIVE_PORT_URI, this);
        this.receive_port.publishPort();
        this.publish_port = new PublishingOutboundPort(this.PUBLISH_PORT_URI, this);
        this.publish_port.publishPort();

        this.doPortConnection(this.REGISTRATION_PORT_URI, Broker.BROKER_REGISTRATION_URI, RegistrationConnector.class.getCanonicalName());
    }

    @Override
    public synchronized void execute() throws Exception {
        super.execute();

        String publisher_uri = this.registration_port.register(RECEIVE_PORT_URI, RegistrationCI.RegistrationClass.FREE);
        this.doPortConnection(this.PUBLISH_PORT_URI, publisher_uri, PublishingConnector.class.getCanonicalName());
        this.traceMessage("Station : Registered\n");

        // Wait for clients
        Thread.sleep(1000);

        // Publish a test message with WindData as payload
        Position position = new Position(10, 20);
        WindData windData = new WindData(position, 10.0, 5.0);
        Message msg = new Message(windData);
        msg.putProperty("type", "wind");;

        this.publish_port.publish(RECEIVE_PORT_URI, "channel0", msg);
        this.traceMessage("Publish a message on channel0 - " + windData + "\n");
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

    @Override // Not a receiver
    public void receiveOne(String channel, MessageI message) { }

    @Override
    public void receiveMultiple(String channel, MessageI[] messages) {}
}
