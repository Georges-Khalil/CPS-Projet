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
import fr.sorbonne_u.cps.pubsub.meteo.MeteoAlert;
import fr.sorbonne_u.cps.pubsub.meteo.MeteoAlert.Level;
import fr.sorbonne_u.cps.pubsub.meteo.Region;
import fr.sorbonne_u.cps.meteo.interfaces.MeteoAlertI;
import fr.sorbonne_u.cps.pubsub.ports.PublishingOutboundPort;

import java.time.Duration;
import java.time.Instant;
import fr.sorbonne_u.cps.pubsub.ports.ReceivingInboundPort;
import fr.sorbonne_u.cps.pubsub.ports.RegistrationOutboundPort;
import fr.sorbonne_u.cps.pubsub.utils.URIGenerator;

/**
 * The Bureau publishes weather alerts on the pub/sub system.
 */
@OfferedInterfaces(offered = {ReceivingCI.class})
@RequiredInterfaces(required = {PublishingCI.class, RegistrationCI.class})
public class Bureau extends AbstractComponent implements ClientI {

    protected final ReceivingInboundPort receive_port;
    protected final String RECEIVE_PORT_URI;
    protected final PublishingOutboundPort publish_port;
    protected final String PUBLISH_PORT_URI;
    protected final RegistrationOutboundPort registration_port;
    protected final String REGISTRATION_PORT_URI;

    protected Bureau() throws Exception {
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
        this.traceMessage("Bureau : Registered\n");

        // Wait for subscribers get ready
        Thread.sleep(1000);

        // Publish a first alert message that should be filtered (accepted)
        Region[] regions1 = {new Region(0, 0, 100, 100)};
        MeteoAlert alert1 = new MeteoAlert(
                MeteoAlertI.AlterType.STORM, 
                Level.ORANGE, 
                regions1, 
                Instant.now(), 
                Duration.ofHours(6)
        );
        Message msg1 = new Message(alert1);
        msg1.putProperty("type", "alert");

        this.publish_port.publish(RECEIVE_PORT_URI, "channel1", msg1);
        this.traceMessage("Alert published on 'channel1' - " + alert1 + "\n");

        // Publish a second message that should be filtered (refused)
        Region[] regions2 = {new Region(50, 50, 75, 75)};
        MeteoAlert alert2 = new MeteoAlert(
                MeteoAlertI.AlterType.FLOODING, 
                Level.RED, 
                regions2, 
                Instant.now(), 
                Duration.ofHours(3)
        );
        Message msg2 = new Message(alert2);
        msg2.putProperty("type", "warning");

        this.publish_port.publish(RECEIVE_PORT_URI, "channel1", msg2);
        this.traceMessage("Published a warning message on 'channel1' - " + alert2 + "\n");
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
        this.traceMessage("Message received on '" + channel +
                "' | payload=" + message.getPayload() + "\n");
    }

    @Override
    public void receiveMultiple(String channel, MessageI[] messages) {
        for (MessageI m : messages)
            this.receiveOne(channel, m);
    }
}