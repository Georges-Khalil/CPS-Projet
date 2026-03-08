package fr.sorbonne_u.cps.pubsub;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.cps.pubsub.composants.Broker;
import fr.sorbonne_u.cps.pubsub.composants.Bureau;
import fr.sorbonne_u.cps.pubsub.composants.WindTurbine;
import fr.sorbonne_u.cps.pubsub.composants.Station;
import fr.sorbonne_u.cps.pubsub.meteo.Position;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

import java.time.Instant;
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
        // ----- Component URIs and TestScenario -----
        // Warning: each component URI must be unique
        // Warning: all created components need to be present in the test scenario they're given (otherwise assertion error)
        Scenario scenario = new Scenario();
        String windTurbine1_URI = "windTurbine1";
        String station1_URI = "station1";
        String bureau1_URI = "bureau1";
        String station2_URI = "station2";
        TestScenario testScenario = scenario.fullOperationTest(windTurbine1_URI, station1_URI, bureau1_URI, station2_URI);

        // ----- Creation of the components -----

        String broker = AbstractComponent.createComponent(
                Broker.class.getCanonicalName(),
                new Object[]{}
        );

        AbstractComponent.createComponent(
                WindTurbine.class.getCanonicalName(),
                new Object[]{windTurbine1_URI, new Position(20, 30), testScenario}
        );

        AbstractComponent.createComponent(
                Station.class.getCanonicalName(),
                new Object[]{station1_URI, new Position(0, 0), testScenario}
        );

        AbstractComponent.createComponent(
                Station.class.getCanonicalName(),
                new Object[]{station2_URI,new Position(50, 30), testScenario}
        );

        AbstractComponent.createComponent(
                Bureau.class.getCanonicalName(),
                new Object[]{bureau1_URI, testScenario}
        );

        // ----- Creation of the clock -----

        // the start time of the scenario is defined relative to the current time
        long current = System.currentTimeMillis();

        // start time of the components in Unix epoch time in nanoseconds
        long unixEpochStartTimeInNanos = TimeUnit.MILLISECONDS.toNanos(current + scenario.DELAY_TO_START);

        // start instant used for time-triggered synchronisation in the test scenario
        Instant startInstant = Instant.parse(scenario.START_INSTANT);

        // create the clock server and the clock used to synchronise the
        // components actions in the test scenario
        String clock = AbstractComponent.createComponent(
                ClocksServer.class.getCanonicalName(),
                new Object[] {
                        scenario.CLOCK_URI, // must use the same in the test scenario
                        unixEpochStartTimeInNanos,
                        startInstant, // idem
                        scenario.ACCELERATION_FACTOR
                });

        // ----- Enable tracing -----
        this.toggleTracing(broker);
        this.toggleTracing(windTurbine1_URI);
        this.toggleTracing(station1_URI);
        this.toggleTracing(station2_URI);
        this.toggleTracing(bureau1_URI);
        this.toggleTracing(clock);

        // The connexions are dynamically created

        super.deploy();
    }

    public static void main(String[] args) throws Exception {
        CVM cvm = new CVM();
        cvm.startStandardLifeCycle(30000L);
        System.exit(0);
    }

}
