package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;

/**
 * Implemented by components that offer ReceivingCI
 *
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public interface ClientI {

    void receiveOne(String channel, MessageI message) throws Exception;
    void receiveMultiple(String channel, MessageI[] messages) throws Exception;
}
