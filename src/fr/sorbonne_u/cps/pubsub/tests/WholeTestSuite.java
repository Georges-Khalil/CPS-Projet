package fr.sorbonne_u.cps.pubsub.tests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    MessageTests.class,
    MessageFiltersTests.class,
})
public class WholeTestSuite {}
