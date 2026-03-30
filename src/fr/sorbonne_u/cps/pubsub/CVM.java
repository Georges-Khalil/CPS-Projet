package fr.sorbonne_u.cps.pubsub;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.cps.pubsub.composants.Broker;
import fr.sorbonne_u.cps.pubsub.scenario.AsyncAuditScenario;
import fr.sorbonne_u.cps.pubsub.scenario.AsyncErrorScenario;
import fr.sorbonne_u.cps.pubsub.scenario.AbstractScenario;
import fr.sorbonne_u.cps.pubsub.scenario.ChannelManagementScenario;
import fr.sorbonne_u.cps.pubsub.scenario.ComplexInteractionScenario;
import fr.sorbonne_u.cps.pubsub.scenario.FilterModificationScenario;
import fr.sorbonne_u.cps.pubsub.scenario.FullOperationScenario;
import fr.sorbonne_u.cps.pubsub.scenario.LargeScaleScenario;
import fr.sorbonne_u.cps.pubsub.scenario.MultipleSubscriptionScenario;
import fr.sorbonne_u.cps.pubsub.scenario.SecurityTestScenario;
import fr.sorbonne_u.cps.pubsub.scenario.ServiceClassUpgradeScenario;
import fr.sorbonne_u.cps.pubsub.scenario.UnregisterRedeployScenario;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

import java.util.concurrent.TimeUnit;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
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

        // ----- Choose the test scenario -----
        // AbstractScenario scenario = new SimpleScenario(this);
         AbstractScenario scenario = new AsyncAuditScenario(this);
        //AbstractScenario scenario = new AsyncErrorScenario(this);
        // AbstractScenario scenario = new FullOperationScenario(this);
        // AbstractScenario scenario = new ComplexInteractionScenario(this);
        // AbstractScenario scenario = new SecurityTestScenario(this);
        // AbstractScenario scenario = new LargeScaleScenario(this);
        // AbstractScenario scenario = new ChannelManagementScenario(this);
        // AbstractScenario scenario = new FilterModificationScenario(this);
        // AbstractScenario scenario = new MultipleSubscriptionScenario(this);
        // AbstractScenario scenario = new UnregisterRedeployScenario(this);
        //AbstractScenario scenario = new ServiceClassUpgradeScenario(this);

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
        CVM cvm = new CVM();
        cvm.startStandardLifeCycle(300000L);
        System.exit(0);
    }

}
