package fr.sorbonne_u.cps.pubsub;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.examples.basic_cs.connections.URIServiceConnector;
import fr.sorbonne_u.cps.pubsub.composants.Broker;
import fr.sorbonne_u.cps.pubsub.composants.Eolienne;
import fr.sorbonne_u.cps.pubsub.composants.Station;
import fr.sorbonne_u.cps.pubsub.connectors.PublishingConnector;
import fr.sorbonne_u.cps.pubsub.connectors.ReceivingConnector;
import fr.sorbonne_u.cps.pubsub.connectors.RegistrationConnector;

public class CVM
        extends AbstractCVM {

    public CVM() throws Exception {
        super();
    }

    /** URI inbound receive d'une éolienne.						*/
    public final static String	EOLIENNE_RECEIVE_1 = "éolienne_receive_1" ;
    /** URI outbound registration d'une éolienne.						*/
    public final static String	EOLIENNE_REGISTRATION_1 = "éolienne_register_1" ;

    /** URI outbound publish d'une station.						*/
    public final static String	STATION_PUBLISH_1 = "station_publish_1" ;
    /** URI outbound registration d'une station.						*/
    public final static String	STATION_REGISTRATION_1 = "station_register_1" ;


    @Override
    public void			deploy() throws Exception {

        // ---------------------------------------------------------------------
        // Creation phase
        // ---------------------------------------------------------------------

        // Composant courtier :
        String Courtier =
                AbstractComponent.createComponent(
                        Broker.class.getCanonicalName(), // nom de la classe
                        new Object[]{}  // pas de paramètres pour ce constructeur
                );
        this.toggleTracing(Courtier);

        // Un composant eolienne :
        String Eolienne_1 =
                AbstractComponent.createComponent(
                        Eolienne.class.getCanonicalName(),
                        new Object[]{EOLIENNE_RECEIVE_1, EOLIENNE_REGISTRATION_1}
                );
        this.toggleTracing(Eolienne_1);
        // Un composant station météo :
        String Station_1 =
                AbstractComponent.createComponent(
                        Station.class.getCanonicalName(),
                        new Object[]{STATION_PUBLISH_1, STATION_REGISTRATION_1}
                );
        this.toggleTracing(Station_1);


        // ---------------------------------------------------------------------
        // Connection phase
        // ---------------------------------------------------------------------

        // Publish :
        this.doPortConnection(
                Station_1,
                STATION_PUBLISH_1,
                Broker.BROKER_PUBLISH_URI,
                PublishingConnector.class.getCanonicalName());
        // Receive :
        this.doPortConnection(
                Courtier,
                Broker.BROKER_RECEIVE_URI.get(0), // TODO: Obtenir l'uri de connection vers eolienne_1.
                EOLIENNE_RECEIVE_1,
                ReceivingConnector.class.getCanonicalName()
        );
        // Registration Eolienne :
        this.doPortConnection(
                Eolienne_1,
                EOLIENNE_REGISTRATION_1,
                Broker.BROKER_REGISTRATION_URI,
                RegistrationConnector.class.getCanonicalName()
        );
        // Registration Station :
        this.doPortConnection(
                Station_1,
                STATION_REGISTRATION_1,
                Broker.BROKER_REGISTRATION_URI,
                RegistrationConnector.class.getCanonicalName()
        );

        // --------------------------------------------------------------------
        // Deployment done
        // --------------------------------------------------------------------
        super.deploy();
    }

    public static void		main(String[] args) {
        try {
            CVM cvm = new CVM();
            cvm.startStandardLifeCycle(60000L) ;
            Thread.sleep(5000L) ;
            System.exit(0) ;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
