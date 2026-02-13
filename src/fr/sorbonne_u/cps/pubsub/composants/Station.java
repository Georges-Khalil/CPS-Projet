package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.components.AbstractComponent;

public class Station extends AbstractComponent implements ClientI {
    protected Station() {
        super(1,0); // Pour l'instant
    }
}
