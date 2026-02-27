package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.pubsub.connectors.RegistrationConnector;
import fr.sorbonne_u.cps.pubsub.filters.ComparableValueFilter;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.ReceivingCI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.filters.MessageFilter;
import fr.sorbonne_u.cps.pubsub.filters.PropertyFilter;
import fr.sorbonne_u.cps.pubsub.ports.ReceivingInboundPort;
import fr.sorbonne_u.cps.pubsub.ports.RegistrationOutboundPort;
import fr.sorbonne_u.cps.pubsub.utils.URIGenerator;

/**
 * The Wind Turbine subscribes & receives messages (data, alerts).
 *
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
@OfferedInterfaces(offered = {ReceivingCI.class})
@RequiredInterfaces(required = {RegistrationCI.class})
public class WindTurbine extends AbstractComponent implements ClientI {

    protected final ReceivingInboundPort receive_port;
    protected final String RECEIVE_PORT_URI;
    protected final RegistrationOutboundPort registration_port;
    protected final String REGISTRATION_PORT_URI;

    protected WindTurbine() throws Exception {
        super(1, 0);
        this.REGISTRATION_PORT_URI = URIGenerator.getNew(this);
        this.RECEIVE_PORT_URI = URIGenerator.getNew(this);

        this.registration_port = new RegistrationOutboundPort(this.REGISTRATION_PORT_URI, this);
        this.registration_port.publishPort();
        this.receive_port = new ReceivingInboundPort(this.RECEIVE_PORT_URI, this);
        this.receive_port.publishPort();

        this.doPortConnection(this.REGISTRATION_PORT_URI, Broker.BROKER_REGISTRATION_URI, RegistrationConnector.class.getCanonicalName());
    }

    @Override
    public synchronized void execute() throws Exception {
        super.execute();

        this.registration_port.register(this.RECEIVE_PORT_URI, RegistrationCI.RegistrationClass.FREE);
        this.traceMessage("WindTurbine : Registered\n");

        // Subscribe with filters
        this.registration_port.subscribe(this.RECEIVE_PORT_URI, "channel0", new MessageFilter());
        this.traceMessage("Subscribed to channel0\n");

        MessageFilter filter = new MessageFilter(
            new PropertyFilter("type", new ComparableValueFilter("alert"))
        );
        this.registration_port.subscribe(this.RECEIVE_PORT_URI, "channel1", filter);
        this.traceMessage("Subscribed to channel1 with filter: type=alert\n");
    }

    @Override
    public synchronized void finalise() throws Exception {
        this.doPortDisconnection(this.REGISTRATION_PORT_URI);
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
    public void receiveOne(String channel, MessageI message) {
        this.traceMessage("Message received on '" + channel +
                "' | payload=" + message.getPayload() + " | timestamp=" + message.getTimeStamp() + "\n");
    }

    @Override
    public void receiveMultiple(String channel, MessageI[] messages) {
        for (MessageI m : messages)
            this.receiveOne(channel, m);
    }
}