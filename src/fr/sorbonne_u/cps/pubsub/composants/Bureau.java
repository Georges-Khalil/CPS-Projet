package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.meteo.interfaces.RegionI;
import fr.sorbonne_u.cps.meteo.interfaces.WindDataI;
import fr.sorbonne_u.cps.pubsub.connectors.PrivilegedClientConnector;
import fr.sorbonne_u.cps.pubsub.connectors.PublishingConnector;
import fr.sorbonne_u.cps.pubsub.connectors.RegistrationConnector;
import fr.sorbonne_u.cps.pubsub.exceptions.UnknownPropertyException;
import fr.sorbonne_u.cps.pubsub.filters.MessageFilter;
import fr.sorbonne_u.cps.pubsub.interfaces.*;
import fr.sorbonne_u.cps.pubsub.message.Message;
import fr.sorbonne_u.cps.pubsub.meteo.MeteoAlert;
import fr.sorbonne_u.cps.pubsub.meteo.MeteoAlert.Level;
import fr.sorbonne_u.cps.pubsub.meteo.Position;
import fr.sorbonne_u.cps.pubsub.meteo.Region;
import fr.sorbonne_u.cps.meteo.interfaces.MeteoAlertI;
import fr.sorbonne_u.cps.pubsub.ports.PrivilegedClientOutboundPort;
import fr.sorbonne_u.cps.pubsub.ports.PublishingOutboundPort;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

import fr.sorbonne_u.cps.pubsub.ports.ReceivingInboundPort;
import fr.sorbonne_u.cps.pubsub.ports.RegistrationOutboundPort;
import fr.sorbonne_u.cps.pubsub.utils.URIGenerator;

/**
 * The Bureau publishes weather alerts on the pub/sub system.
 * 
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
@OfferedInterfaces(offered = {ReceivingCI.class})
@RequiredInterfaces(required = {PrivilegedClientCI.class, RegistrationCI.class})
public class Bureau extends AbstractComponent implements ClientI {

    public static String WEATHER_ALERTS_CHANNEL = "weather_alerts_channel";

    protected final String REGISTRATION_PORT_URI;
    protected final String RECEIVE_PORT_URI;
    protected final String PUBLISH_PORT_URI;
    protected final RegistrationOutboundPort registration_port;
    protected final ReceivingInboundPort receive_port;
    protected final PrivilegedClientOutboundPort publish_port;

    protected final HashMap<Integer, RegionI> stations;

    protected Bureau() throws Exception {
        super(1, 0);
        this.REGISTRATION_PORT_URI = URIGenerator.getNew(this);
        this.RECEIVE_PORT_URI = URIGenerator.getNew(this);
        this.PUBLISH_PORT_URI = URIGenerator.getNew(this);

        this.registration_port = new RegistrationOutboundPort(this.REGISTRATION_PORT_URI, this);
        this.registration_port.publishPort();
        this.receive_port = new ReceivingInboundPort(this.RECEIVE_PORT_URI, this);
        this.receive_port.publishPort();
        this.publish_port = new PrivilegedClientOutboundPort(this.PUBLISH_PORT_URI, this);
        this.publish_port.publishPort();

        this.doPortConnection(this.REGISTRATION_PORT_URI, Broker.BROKER_REGISTRATION_URI, RegistrationConnector.class.getCanonicalName());

        this.stations = new HashMap<>();
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        String publisher_uri = this.registration_port.register(RECEIVE_PORT_URI, RegistrationCI.RegistrationClass.PREMIUM);
        this.doPortConnection(this.PUBLISH_PORT_URI, publisher_uri, PrivilegedClientConnector.class.getCanonicalName());
        this.traceMessage("Bureau : Registered\n");

        this.publish_port.createChannel(this.RECEIVE_PORT_URI, WEATHER_ALERTS_CHANNEL, "*WindTurbine*");

        this.registration_port.subscribe(this.RECEIVE_PORT_URI, Broker.WIND_CHANNEL, new MessageFilter());
    }
    @Override
    public synchronized void finalise() throws Exception {
        System.out.println("B F");
        this.doPortDisconnection(PUBLISH_PORT_URI);
        System.out.println("B2 F");
        this.doPortDisconnection(REGISTRATION_PORT_URI);
        System.out.println("B3 F");
        super.finalise();
        System.out.println("B4 F");
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        System.out.println("B S");
        try {
            this.receive_port.unpublishPort();
            this.publish_port.unpublishPort();
            this.registration_port.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    protected void receiveFromStationsInfo(MessageI message) throws Exception {
        this.stations.put((Integer) message.getPropertyValue("ID"), (RegionI) message.getPayload());
    }

    protected void receiveWindData(MessageI message) throws Exception {
        WindDataI windData = (WindDataI) message.getPayload();
        double force = windData.force();

        MeteoAlert alert1 = new MeteoAlert(
                ((Position)windData.getPosition()).x < 0 ? MeteoAlertI.AlterType.STORM : MeteoAlertI.AlterType.ICY_STORM, // Can be cold in the north
                force < 20 ? Level.GREEN : force < 60 ? Level.YELLOW : force < 100 ? Level.ORANGE : force < 150 ? Level.RED : Level.SCARLET,
                new RegionI[] { this.stations.get((Integer) message.getPropertyValue("ID")) },
                Instant.now(),
                Duration.ofHours(((Position)windData.getPosition()).x % 7) // Why not
        );
        Message msg1 = new Message(alert1);
        msg1.putProperty("type", "alert");

        this.publish_port.publish(RECEIVE_PORT_URI, "channel1", msg1);
        this.traceMessage("Alert published on 'channel1' - " + alert1 + "\n");
    }

    @Override
    public void receiveOne(String channel, MessageI message) throws Exception {
        this.traceMessage("Message received on '" + channel +
                "' | payload=" + message.getPayload() + "\n");

        if (channel.equals(WEATHER_ALERTS_CHANNEL))
            this.receiveFromStationsInfo(message);
        else
            this.receiveWindData(message);
    }

    @Override
    public void receiveMultiple(String channel, MessageI[] messages) throws Exception {
        for (MessageI m : messages)
            this.receiveOne(channel, m);
    }
}