package fr.sorbonne_u.cps.pubsub;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.cps.pubsub.composants.Broker;
import fr.sorbonne_u.cps.pubsub.composants.Bureau;
import fr.sorbonne_u.cps.pubsub.composants.WindTurbine;
import fr.sorbonne_u.cps.pubsub.composants.Station;
import fr.sorbonne_u.cps.pubsub.connectors.PublishingConnector;
import fr.sorbonne_u.cps.pubsub.connectors.RegistrationConnector;

public class CVM extends AbstractCVM {

    // Éolienne 1
    public static final String EOLIENNE_RECEIVE_1 = "eolienne-receive-1";
    public static final String EOLIENNE_REGISTRATION_1 = "eolienne-register-1";

    // Station 1
    public static final String STATION_RECEIVE_1 = "station-receive-1";
    public static final String STATION_PUBLISH_1 = "station-publish-1";
    public static final String STATION_REGISTRATION_1 = "station-register-1";

    // Station 2
    public static final String STATION_RECEIVE_2 = "station-receive-2";
    public static final String STATION_PUBLISH_2 = "station-publish-2";
    public static final String STATION_REGISTRATION_2 = "station-register-2";

    // Bureau 1
    public static final String BUREAU_RECEIVE_1 = "bureau-receive-1";
    public static final String BUREAU_PUBLISH_1 = "bureau-publish-1";
    public static final String BUREAU_REGISTRATION_1 = "bureau-register-1";

    public CVM() throws Exception {
        super();
    }

    @Override
    public void deploy() throws Exception {

        // ----- Création des composants -----

        String courtier = AbstractComponent.createComponent(
                Broker.class.getCanonicalName(),
                new Object[]{}
        );
        this.toggleTracing(courtier);

        String eolienne1 = AbstractComponent.createComponent(
                WindTurbine.class.getCanonicalName(),
                new Object[]{EOLIENNE_RECEIVE_1, EOLIENNE_REGISTRATION_1}
        );
        this.toggleTracing(eolienne1);

        String station1 = AbstractComponent.createComponent(
                Station.class.getCanonicalName(),
                new Object[]{STATION_RECEIVE_1, STATION_PUBLISH_1, STATION_REGISTRATION_1}
        );
        this.toggleTracing(station1);

        String station2 = AbstractComponent.createComponent(
                Station.class.getCanonicalName(),
                new Object[]{STATION_RECEIVE_2, STATION_PUBLISH_2, STATION_REGISTRATION_2}
        );
        this.toggleTracing(station2);

        String bureau1 = AbstractComponent.createComponent(
                Bureau.class.getCanonicalName(),
                new Object[]{BUREAU_RECEIVE_1, BUREAU_PUBLISH_1, BUREAU_REGISTRATION_1}
        );
        this.toggleTracing(bureau1);

        // ----- Connexions Registration (client -> courtier) -----

        this.doPortConnection(eolienne1, EOLIENNE_REGISTRATION_1,
                Broker.BROKER_REGISTRATION_URI,
                RegistrationConnector.class.getCanonicalName());

        this.doPortConnection(station1, STATION_REGISTRATION_1,
                Broker.BROKER_REGISTRATION_URI,
                RegistrationConnector.class.getCanonicalName());

        this.doPortConnection(station2, STATION_REGISTRATION_2,
                Broker.BROKER_REGISTRATION_URI,
                RegistrationConnector.class.getCanonicalName());

        this.doPortConnection(bureau1, BUREAU_REGISTRATION_1,
                Broker.BROKER_REGISTRATION_URI,
                RegistrationConnector.class.getCanonicalName());

        // ----- Connexions Publishing (station/bureau -> courtier) -----

        this.doPortConnection(station1, STATION_PUBLISH_1,
                Broker.BROKER_PUBLISH_URI,
                PublishingConnector.class.getCanonicalName());

        this.doPortConnection(station2, STATION_PUBLISH_2,
                Broker.BROKER_PUBLISH_URI,
                PublishingConnector.class.getCanonicalName());

        this.doPortConnection(bureau1, BUREAU_PUBLISH_1,
                Broker.BROKER_PUBLISH_URI,
                PublishingConnector.class.getCanonicalName());

        // Les connexions Receive (courtier -> clients) sont créées dynamiquement
        // par le courtier lors de register()

        super.deploy();
    }

    public static void main(String[] args) {
        try {
            CVM cvm = new CVM();
            cvm.startStandardLifeCycle(120000L);
            Thread.sleep(10000L);
            System.exit(0);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
