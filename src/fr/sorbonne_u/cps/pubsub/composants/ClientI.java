package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;

/**
 * Les clients peuvent own les ports clients.
  */
public interface ClientI {

    public void receive_one(String channel, MessageI message);
    public void receive_multiple(String channel, MessageI[] messages);
}
