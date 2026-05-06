package fr.sorbonne_u.cps.pubsub;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;
import fr.sorbonne_u.cps.pubsub.components.Broker;
import fr.sorbonne_u.cps.pubsub.scenario.AbstractScenario;
import fr.sorbonne_u.cps.pubsub.scenario.FullOperationScenario;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

import java.util.concurrent.TimeUnit;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class DistributedCVM extends AbstractDistributedCVM {

    protected static final String BROKER_JVM_URI = "BROKER_JVM";
    protected static final String CLIENTS_JVM_URI = "CLIENTS_JVM";

    public DistributedCVM(String[] args) throws Exception {
        super(args);
    }

    @Override
    public void instantiateAndPublish() throws Exception {
        switch (getThisJVMURI()) {
            case BROKER_JVM_URI:
                break;
            case CLIENTS_JVM_URI:
                break;
            default:
                throw new Exception("URI unknow : " + getThisJVMURI());
        }
    }

    @Override
    public void deploy() throws Exception {

        // ----- Creation of the components -----
        String broker = AbstractComponent.createComponent(
                Broker.class.getCanonicalName(),
                new Object[]{}
        );

        // ----- Choose the test scenario -----
        AbstractScenario scenario = new FullOperationScenario(this);

        // create the clock server and the clock used to synchronise the
        // components actions in the test scenario
        String clock = AbstractComponent.createComponent(
                ClocksServer.class.getCanonicalName(),
                new Object[] {
                        scenario.clockURI, // must use the same in the test scenario
                        TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis() + scenario.startDelay),
                        scenario.startInstant, // idem
                        scenario.accelerationFactor
                });

        // ----- Enable tracing -----
        this.toggleTracing(broker);
        this.toggleTracing(clock);

        // The connexions are dynamically created

        super.deploy();
    }

    public static void main(String[] args) throws Exception {
        DistributedCVM cvm = new DistributedCVM(args);
        cvm.startStandardLifeCycle(100000L);
        System.exit(0);
    }

}
