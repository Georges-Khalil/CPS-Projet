package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.pubsub.interfaces.AbnormalTerminationNotificationCI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.plugins.ClientPublicationPlugin;

public class AbnormalTerminationNotificationInboundPort 
extends AbstractInboundPort 
implements AbnormalTerminationNotificationCI {
    public AbnormalTerminationNotificationInboundPort(ComponentI owner) throws Exception {
        super(AbnormalTerminationNotificationCI.class, owner);
    }

    public AbnormalTerminationNotificationInboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, AbnormalTerminationNotificationCI.class, owner);
    }

    @Override
    public void notifyAbnormalTermination(String channel, MessageI message, Throwable cause) throws Exception {
        this.getOwner().runTask(
                new AbstractComponent.AbstractTask() {
                    @Override
                    public void run() {
                        try {
                            ((ClientPublicationPlugin) AbnormalTerminationNotificationInboundPort.this
                                    .getOwnerPlugin(ClientPublicationPlugin.PLUGIN_URI))
                                    .notifyAbnormalTermination(channel, message, cause);
                        } catch (Exception e) {
                            throw new RuntimeException(e); // TODO: FAUT GERER CA ICI ! OU ALORS DANS LE PLUGIN
                        }
                    }
                });
    }

    @Override
    public void notifyAbnormalTermination(String channel, MessageI[] messages, Throwable cause) throws Exception {
        this.getOwner().runTask(
                new AbstractComponent.AbstractTask() {
                    @Override
                    public void run() {
                        try {
                            ((ClientPublicationPlugin) AbnormalTerminationNotificationInboundPort.this
                                    .getOwnerPlugin(ClientPublicationPlugin.PLUGIN_URI))
                                    .notifyAbnormalTermination(channel, messages, cause);
                        } catch (Exception e) {
                            throw new RuntimeException(e); //TODO: FAUT GERER CA ICI ! OU ALORS DANS LE PLUGIN
                        }
                    }
                });
    }
}
