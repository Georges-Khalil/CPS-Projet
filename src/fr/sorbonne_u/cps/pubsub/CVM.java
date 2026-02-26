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

    public CVM() throws Exception {
        super();
    }

    @Override
    public void deploy() throws Exception {

        // ----- Creation of the components -----

        String broker = AbstractComponent.createComponent(
                Broker.class.getCanonicalName(),
                new Object[]{}
        );

        String windTurbine1 = AbstractComponent.createComponent(
                WindTurbine.class.getCanonicalName(),
                new Object[]{}
        );

        String station1 = AbstractComponent.createComponent(
                Station.class.getCanonicalName(),
                new Object[]{}
        );

        String station2 = AbstractComponent.createComponent(
                Station.class.getCanonicalName(),
                new Object[]{}
        );

        String bureau1 = AbstractComponent.createComponent(
                Bureau.class.getCanonicalName(),
                new Object[]{}
        );

        // Enable tracing
        this.toggleTracing(broker);
        this.toggleTracing(windTurbine1);
        this.toggleTracing(station1);
        this.toggleTracing(station2);
        this.toggleTracing(bureau1);

        // The connexions are dynamically created

        super.deploy();
    }

    public static void main(String[] args) throws Exception {
        CVM cvm = new CVM();
        cvm.startStandardLifeCycle(10000L);
        System.exit(0);
    }
}
