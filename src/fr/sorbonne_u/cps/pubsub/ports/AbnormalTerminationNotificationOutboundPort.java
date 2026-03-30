package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.pubsub.interfaces.AbnormalTerminationNotificationCI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;

public class AbnormalTerminationNotificationOutboundPort 
extends AbstractOutboundPort 
implements AbnormalTerminationNotificationCI {
    public AbnormalTerminationNotificationOutboundPort(ComponentI owner) throws Exception {
        super(AbnormalTerminationNotificationCI.class, owner);
    }

    public AbnormalTerminationNotificationOutboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, AbnormalTerminationNotificationCI.class, owner);
    }

    @Override
    public void notifyAbnormalTermination(String channel, MessageI message, Throwable cause) throws Exception {
        ((AbnormalTerminationNotificationCI) this.getConnector()).notifyAbnormalTermination(channel, message, cause);
    }

    @Override
    public void notifyAbnormalTermination(String channel, MessageI[] messages, Throwable cause) throws Exception {
        ((AbnormalTerminationNotificationCI) this.getConnector()).notifyAbnormalTermination(channel, messages, cause);
    }
}
