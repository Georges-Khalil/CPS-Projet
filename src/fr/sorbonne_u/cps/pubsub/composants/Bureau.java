package fr.sorbonne_u.cps.pubsub.composants;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.PublishingCI;
import fr.sorbonne_u.cps.pubsub.interfaces.ReceivingCI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;

@OfferedInterfaces(offered = {ReceivingCI.class})
@RequiredInterfaces(required = {PublishingCI.class, RegistrationCI.class})
public class Bureau extends AbstractComponent implements ClientI {

    protected Bureau() {
        super(1,0); // Pour l'instant
    }

    public void receive(String channel, MessageI message) {
        //todo
    }

    @Override
    public synchronized void execute() throws Exception {
        super.execute();
        // TODO !!!
    }
}
