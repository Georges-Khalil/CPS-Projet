package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;

/**
 * Implemented by components that offer ReceivingCI
  */
public interface ClientI {

    public void receiveOne(String channel, MessageI message);
    public void receiveMultiple(String channel, MessageI[] messages);
}
