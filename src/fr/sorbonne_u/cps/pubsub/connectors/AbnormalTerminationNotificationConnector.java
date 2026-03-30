package fr.sorbonne_u.cps.pubsub.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.pubsub.interfaces.AbnormalTerminationNotificationCI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;

public class AbnormalTerminationNotificationConnector 
extends AbstractConnector 
implements AbnormalTerminationNotificationCI {
    @Override
    public void notifyAbnormalTermination(String channel, MessageI message, Throwable cause) throws Exception {
        ((AbnormalTerminationNotificationCI) this.offering).notifyAbnormalTermination(channel, message, cause);
    }

    @Override
    public void notifyAbnormalTermination(String channel, MessageI[] messages, Throwable cause) throws Exception {
        ((AbnormalTerminationNotificationCI) this.offering).notifyAbnormalTermination(channel, messages, cause);
    }
}
