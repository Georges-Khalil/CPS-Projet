package fr.sorbonne_u.cps.pubsub.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

/**
 * The component interface <code>AbnormalTerminationNotificationCI</code> is 
 * used to notify a client component that an asynchronous publication has
 * failed.
 */
public interface AbnormalTerminationNotificationCI 
extends OfferedCI, RequiredCI {
    /**
     * notify the client component that an asynchronous publication has
     * failed.
     * 
     * @param channel   name of the channel on which the publication was attempted.
     * @param message   message that failed to be published.
     * @param cause     cause of the failure.
     * @throws Exception <i>to do</i>.
     */
    public void notifyAbnormalTermination(
        String channel, 
        MessageI message, 
        Throwable cause
    ) throws Exception;

    /**
     * notify the client component that an asynchronous publication has
     * failed.
     * 
     * @param channel   name of the channel on which the publication was attempted.
     * @param messages  messages that failed to be published.
     * @param cause     cause of the failure.
     * @throws Exception <i>to do</i>.
     */
    public void notifyAbnormalTermination(
        String channel, 
        MessageI[] messages, 
        Throwable cause
    ) throws Exception;
}
