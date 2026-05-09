package fr.sorbonne_u.cps.pubsub;

import fr.sorbonne_u.components.AbstractComponent;
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

    protected static final String GROUP_A_JVM_URI = "GROUP_A_JVM";
    protected static final String GROUP_B_JVM_URI = "GROUP_B_JVM";
    protected static final String GROUP_C_JVM_URI = "GROUP_C_JVM";

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

        switch (getThisJVMURI()) {
            case GROUP_A_JVM_URI:
                break;
            case GROUP_B_JVM_URI:
                break;
            case GROUP_C_JVM_URI:
                break;
            default:
                throw new Exception("URI unknow : " + getThisJVMURI());
        }
        super.instantiateAndPublish();
    }

    @Override
    public void interconnect() throws Exception {
        switch (getThisJVMURI()) {
            case GROUP_A_JVM_URI: // Add neighbours
                break;
            case GROUP_B_JVM_URI:
                break;
            case GROUP_C_JVM_URI:
                break;
            default:
                throw new Exception("URI unknow : " + getThisJVMURI());
        }

        super.interconnect();
    }

    public static void main(String[] args) throws Exception {
        DistributedCVM cvm = new DistributedCVM(args);
        cvm.startStandardLifeCycle(100000L);
        System.exit(0);
    }

}
