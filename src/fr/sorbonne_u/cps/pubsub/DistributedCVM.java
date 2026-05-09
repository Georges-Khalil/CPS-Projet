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

    protected static final String turbineURI = "turbine_GA";
    protected static final String stationURI = "station_GB";
    protected static final String bureauURI = "bureau_GC";
    protected static final String clockURI = "clock-uri";

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

        TestScenario testScenario = getScenario(turbineURI, stationURI, bureauURI);
        switch (getThisJVMURI()) {
            case GROUP_A_JVM_URI:
                AbstractComponent.createComponent(
                        ClocksServer.class.getCanonicalName(),
                        new Object[] {
                                clockURI,
                                TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis() + 2000),
                                Instant.now().plus(24, ChronoUnit.HOURS),
                                1.0
                        });

                AbstractComponent.createComponent(WindTurbine.class.getCanonicalName(), new Object[]{turbineURI, new Position(0, 0), testScenario});
                toggleTracing(turbineURI);
                break;
            case GROUP_B_JVM_URI:
                AbstractComponent.createComponent(Station.class.getCanonicalName(), new Object[]{stationURI, new Position(0, 0), testScenario});
                toggleTracing(stationURI);
                break;
            case GROUP_C_JVM_URI:
                AbstractComponent.createComponent(Bureau.class.getCanonicalName(), new Object[]{bureauURI, testScenario});
                toggleTracing(bureauURI);
                break;
            default:
                throw new Exception("URI unknow : " + getThisJVMURI());
        }

        super.instantiateAndPublish();
    }

    private TestScenario getScenario(String t1, String s1, String b1) {
        Instant start = Instant.now().plus(24, ChronoUnit.HOURS);
        return new TestScenario(clockURI, start, start.plus(1, ChronoUnit.HOURS), new TestStepI[]{
                new TestStep(clockURI, t1, start.plusSeconds(2), owner -> {
                    try {
                        ((WindTurbine)owner).getRegistrationPlugin().register(RegistrationCI.RegistrationClass.STANDARD);
                    } catch (AlreadyRegisteredException e) {
                        throw new RuntimeException(e);
                    }
                    owner.traceMessage("Hello from ??? Turbine");
                }),
                new TestStep(clockURI, s1, start.plusSeconds(2), owner -> {
                    try {
                        ((Station)owner).getRegistrationPlugin().register(RegistrationCI.RegistrationClass.STANDARD);
                    } catch (AlreadyRegisteredException e) {
                        throw new RuntimeException(e);
                    }
                    owner.traceMessage("Hello from ??? Station");
                }),
                new TestStep(clockURI, b1, start.plusSeconds(2), owner -> {
                    try {
                        ((Bureau)owner).getRegistrationPlugin().register(RegistrationCI.RegistrationClass.STANDARD);
                    } catch (AlreadyRegisteredException e) {
                        throw new RuntimeException(e);
                    }
                    owner.traceMessage("Hello from ??? Bureau");
                })
        });
    }

    @Override
    public void interconnect() throws Exception {
        switch (getThisJVMURI()) {
            case GROUP_A_JVM_URI: // Add neighbours
                connectBrokerToNeighbour( "Broker-" + getThisJVMURI(),  "gossip-receiver-" + "Broker-" + GROUP_B_JVM_URI);
                connectBrokerToNeighbour( "Broker-" + getThisJVMURI(),  "gossip-receiver-" + "Broker-" + GROUP_C_JVM_URI);
                break;
            case GROUP_B_JVM_URI:
                connectBrokerToNeighbour( "Broker-" + getThisJVMURI(),  "gossip-receiver-" + "Broker-" + GROUP_A_JVM_URI);
                connectBrokerToNeighbour( "Broker-" + getThisJVMURI(),  "gossip-receiver-" + "Broker-" + GROUP_C_JVM_URI);
                break;
            case GROUP_C_JVM_URI:
                connectBrokerToNeighbour( "Broker-" + getThisJVMURI(),  "gossip-receiver-" + "Broker-" + GROUP_A_JVM_URI);
                connectBrokerToNeighbour( "Broker-" + getThisJVMURI(),  "gossip-receiver-" + "Broker-" + GROUP_B_JVM_URI);
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
