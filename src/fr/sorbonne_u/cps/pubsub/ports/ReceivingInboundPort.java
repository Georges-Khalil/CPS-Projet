package fr.sorbonne_u.cps.pubsub.ports;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.pubsub.components.ClientI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.ReceivingCI;
import fr.sorbonne_u.cps.pubsub.plugins.ClientSubscriptionPlugin;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class ReceivingInboundPort extends AbstractInboundPort implements ReceivingCI {

    public ReceivingInboundPort(ComponentI owner) throws Exception {
        super(ReceivingCI.class, owner);
        assert owner instanceof ClientI;
    }

    public ReceivingInboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, ReceivingCI.class, owner);
        assert owner instanceof ClientI;
    }

    @Override
    public void receive(String channel, MessageI message) throws Exception {
        this.getOwner().runTask(
                ClientSubscriptionPlugin.RECEIVING_TASKS_URI,
                new AbstractComponent.AbstractTask() {
                    @Override
                    public void run() {
                        ((ClientSubscriptionPlugin) ReceivingInboundPort.this
                                .getOwnerPlugin(ClientSubscriptionPlugin.PLUGIN_URI))
                                .receive(channel, message);
                    }
                });

    }

    @Override
    public void receive(String channel, MessageI[] messages) throws Exception {
        this.getOwner().runTask(
                ClientSubscriptionPlugin.RECEIVING_TASKS_URI,
                new AbstractComponent.AbstractTask() {
                    @Override
                    public void run() {
                        ((ClientSubscriptionPlugin) ReceivingInboundPort.this
                                .getOwnerPlugin(ClientSubscriptionPlugin.PLUGIN_URI))
                                .receive(channel, messages);
                    }
                });

    }
}
