package fr.sorbonne_u.cps.pubsub.tests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
@Suite
@SelectClasses({
    MessageTests.class,
    MessageFiltersTests.class,
})
public class WholeTestSuite {}
