package fr.sorbonne_u.cps.pubsub;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;
import fr.sorbonne_u.components.reflection.connectors.ReflectionConnector;
import fr.sorbonne_u.components.reflection.ports.ReflectionOutboundPort;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestStep;
import fr.sorbonne_u.components.utils.tests.TestStepI;
import fr.sorbonne_u.cps.pubsub.components.Broker;
import fr.sorbonne_u.cps.pubsub.components.Bureau;
import fr.sorbonne_u.cps.pubsub.components.Station;
import fr.sorbonne_u.cps.pubsub.components.WindTurbine;
import fr.sorbonne_u.cps.pubsub.exceptions.AlreadyRegisteredException;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.meteo.Position;
import fr.sorbonne_u.cps.pubsub.scenario.AbstractScenario;
import fr.sorbonne_u.cps.pubsub.scenario.DistributedScenario;
import fr.sorbonne_u.cps.pubsub.scenario.FullOperationScenario;
import fr.sorbonne_u.cps.pubsub.utils.URIGenerator;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class DistributedCVM extends AbstractDistributedCVM {

    protected static final String GROUP_A_JVM_URI = "GROUP_A_JVM";
    protected static final String GROUP_B_JVM_URI = "GROUP_B_JVM";
    protected static final String GROUP_C_JVM_URI = "GROUP_C_JVM";
    protected static final String[] groups = new String[] { GROUP_A_JVM_URI, GROUP_B_JVM_URI, GROUP_C_JVM_URI };

    public DistributedCVM(String[] args) throws Exception {
        super(args);
    }

    @Override
    public void instantiateAndPublish() throws Exception {
        System.out.println("Create broker : " + "Broker-" + getThisJVMURI());
        String broker = AbstractComponent.createComponent(
                Broker.class.getCanonicalName(),
                new Object[]{ "Broker-" + getThisJVMURI() }
        );
        this.toggleTracing(broker);

        AbstractScenario scenario = new DistributedScenario(this, this.thisJVMURI, groups);

        if (this.thisJVMURI.equals(groups[0])) {
            AbstractComponent.createComponent(
                    ClocksServer.class.getCanonicalName(),
                    new Object[]{
                            AbstractScenario.clockURI,
                            TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis() + scenario.startDelay),
                            scenario.startInstant,
                            scenario.accelerationFactor
                    });
        }

        super.instantiateAndPublish();
    }

    @Override
    public void interconnect() throws Exception {
        switch (getThisJVMURI()) {
            case GROUP_A_JVM_URI: // Add neighbours
                connectBrokerToNeighbour( "Broker-" + getThisJVMURI(),  "gossip-receiver-Broker-" + GROUP_B_JVM_URI);
                connectBrokerToNeighbour( "Broker-" + getThisJVMURI(),  "gossip-receiver-Broker-" + GROUP_C_JVM_URI);
                break;
            case GROUP_B_JVM_URI:
                connectBrokerToNeighbour( "Broker-" + getThisJVMURI(),  "gossip-receiver-Broker-" + GROUP_A_JVM_URI);
                connectBrokerToNeighbour( "Broker-" + getThisJVMURI(),  "gossip-receiver-Broker-" + GROUP_C_JVM_URI);
                break;
            case GROUP_C_JVM_URI:
                connectBrokerToNeighbour( "Broker-" + getThisJVMURI(),  "gossip-receiver-Broker-" + GROUP_A_JVM_URI);
                connectBrokerToNeighbour( "Broker-" + getThisJVMURI(),  "gossip-receiver-Broker-" + GROUP_B_JVM_URI);
                break;
            default:
                throw new Exception("URI unknow : " + getThisJVMURI());
        }

        super.interconnect();
    }

    private void connectBrokerToNeighbour(String localBrokerURI, String remoteGripURI) throws Exception {
        ((Broker)this.uri2component.get(localBrokerURI)).connectToNeighbour(remoteGripURI);
    }

    public static void main(String[] args) throws Exception {
        DistributedCVM cvm = new DistributedCVM(args);
        cvm.startStandardLifeCycle(100000L);
        System.exit(0);
    }

}
