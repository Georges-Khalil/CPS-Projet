package fr.sorbonne_u.cps.pubsub.connectors;

import java.util.ArrayList;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.PublishingCI;

public class PublishingConnector
        extends AbstractConnector
        implements PublishingCI
{

	@Override
	public void publish(String receptionPortURI, String channel, MessageI message) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void publish(String receptionPortURI, String channel, ArrayList<MessageI> messages) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
