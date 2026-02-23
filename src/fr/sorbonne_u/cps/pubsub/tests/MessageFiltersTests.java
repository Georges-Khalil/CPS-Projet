package fr.sorbonne_u.cps.pubsub.tests;

import fr.sorbonne_u.cps.pubsub.filters.*;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.*;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.message.Message;
import fr.sorbonne_u.cps.pubsub.message.Property;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class MessageFiltersTests {

  @Test
  void testArguments() {
    assertThrows(IllegalArgumentException.class, () -> new ComparableValueFilter(null, null));
    assertThrows(IllegalArgumentException.class, () -> new ComparableValueFilter(null, ComparableValueFilter.Operator.EQ));
    assertThrows(IllegalArgumentException.class, () -> new ComparableValueFilter(new Serializable() {}, ComparableValueFilter.Operator.LT));
    assertThrows(IllegalArgumentException.class, () -> new ComparableValueFilter("a", null));

    assertThrows(IllegalArgumentException.class, () -> TimeFilter.acceptBefore(null));
    assertThrows(IllegalArgumentException.class, () -> TimeFilter.acceptAfter(null));
    assertThrows(IllegalArgumentException.class, () -> TimeFilter.acceptBetween(Instant.ofEpochSecond(10), Instant.ofEpochSecond(9)));

    assertThrows(IllegalArgumentException.class, () -> new PropertyFilter(null, null));
    assertThrows(IllegalArgumentException.class, () -> new PropertyFilter(null, new JokerValueFilter()));
    assertThrows(IllegalArgumentException.class, () -> new PropertyFilter("propName", null));
    assertThrows(IllegalArgumentException.class, () -> new PropertyFilter("", new JokerValueFilter()));

    assertThrows(IllegalArgumentException.class, () -> new MultiValuesFilter<>(null, null));
    assertThrows(IllegalArgumentException.class, () -> new MultiValuesFilter<>(new String[0], args -> false));
    assertThrows(IllegalArgumentException.class, () -> new MultiValuesFilter<>(new String[] { null }, args -> false));
    assertThrows(IllegalArgumentException.class, () -> new MultiValuesFilter<>(new String[] { "" }, args -> false));
    assertThrows(IllegalArgumentException.class, () -> new MultiValuesFilter<>(new String[] { "a" }, null));

    assertThrows(IllegalArgumentException.class, () -> new PropertiesFilter(null));

    // assertThrows(IllegalArgumentException.class, () -> new MessageFilter(null, new PropertiesFilterI[0], TimeFilter.acceptAny()));
    // assertThrows(IllegalArgumentException.class, () -> new MessageFilter(new PropertyFilterI[0], null, TimeFilter.acceptAny()));
    // assertThrows(IllegalArgumentException.class, () -> new MessageFilter(new PropertyFilterI[0], new PropertiesFilterI[0], null));

    ValueFilterI vf = new ComparableValueFilter("a", ComparableValueFilter.Operator.GT);
    TimeFilterI tf = TimeFilter.acceptAny();
    PropertyFilterI pf = new PropertyFilter("prop", vf);
    MultiValuesFilterI mvf = new MultiValuesFilter<>(new String[] {"prop"}, args -> false);
    PropertiesFilterI mpf = new PropertiesFilter(mvf);

    assertThrows(IllegalArgumentException.class, () -> vf.match(null));
    assertThrows(ClassCastException.class, () -> vf.match(new Serializable() {}));
    assertThrows(IllegalArgumentException.class, () -> tf.match(null));
    assertThrows(IllegalArgumentException.class, () -> pf.match(null));
    assertThrows(IllegalArgumentException.class, () -> mvf.match(null));
    assertThrows(IllegalArgumentException.class, () -> mpf.match(null));

    assertThrows(ClassCastException.class, () -> new ComparableValueFilter(1, ComparableValueFilter.Operator.GE).match("1"));
  }

  @Test
  void testPropertyFilter() {
    ValueFilterI vf = new JokerValueFilter();
    PropertyFilterI pf = new PropertyFilter("prop", vf);

    assertTrue(vf.match(""));
    assertTrue(pf.match(new Property("prop", "")));
    assertFalse(pf.match(new Property("propB", "")));
    assertFalse(pf.match(new Property("Prop", "")));

    assertTrue(new JokerValueFilter().match(0));
    assertTrue(new JokerValueFilter().match(1));
    assertTrue(new JokerValueFilter().match(2));
    assertTrue(new ComparableValueFilter(1, ComparableValueFilter.Operator.LT).match(0));
    assertTrue(new ComparableValueFilter(1, ComparableValueFilter.Operator.GT).match(2));
    assertTrue(new ComparableValueFilter(1, ComparableValueFilter.Operator.EQ).match(1));
    assertTrue(new ComparableValueFilter(1, ComparableValueFilter.Operator.NE).match(0));
    assertTrue(new ComparableValueFilter(1, ComparableValueFilter.Operator.NE).match(2));
    assertTrue(new ComparableValueFilter(1, ComparableValueFilter.Operator.NE).match("1"));
    assertTrue(new ComparableValueFilter(1, ComparableValueFilter.Operator.LE).match(0));
    assertTrue(new ComparableValueFilter(1, ComparableValueFilter.Operator.LE).match(1));
    assertTrue(new ComparableValueFilter(1, ComparableValueFilter.Operator.GE).match(2));
    assertTrue(new ComparableValueFilter(1, ComparableValueFilter.Operator.GE).match(1));

    assertFalse(new ComparableValueFilter(1, ComparableValueFilter.Operator.LT).match(1));
    assertFalse(new ComparableValueFilter(1, ComparableValueFilter.Operator.LT).match(2));
    assertFalse(new ComparableValueFilter(1, ComparableValueFilter.Operator.GT).match(1));
    assertFalse(new ComparableValueFilter(1, ComparableValueFilter.Operator.GT).match(0));
    assertFalse(new ComparableValueFilter(1, ComparableValueFilter.Operator.EQ).match(0));
    assertFalse(new ComparableValueFilter(1, ComparableValueFilter.Operator.EQ).match(2));
    assertFalse(new ComparableValueFilter(1, ComparableValueFilter.Operator.EQ).match("1"));
    assertFalse(new ComparableValueFilter(1, ComparableValueFilter.Operator.NE).match(1));
    assertFalse(new ComparableValueFilter(1, ComparableValueFilter.Operator.LE).match(2));
    assertFalse(new ComparableValueFilter(1, ComparableValueFilter.Operator.GE).match(0));
  }

  @Test
  void testTimeFilter() {
    TimeFilterI tf = TimeFilter.acceptAny();
    assertTrue(tf.match(Instant.ofEpochSecond(0)));
    assertTrue(tf.match(Instant.ofEpochSecond(1)));
    assertTrue(tf.match(Instant.ofEpochSecond(2)));

    tf = TimeFilter.acceptAfter(Instant.ofEpochSecond(1));
    assertFalse(tf.match(Instant.ofEpochSecond(0)));
    assertTrue(tf.match(Instant.ofEpochSecond(1)));
    assertTrue(tf.match(Instant.ofEpochSecond(2)));

    tf = TimeFilter.acceptBefore(Instant.ofEpochSecond(1));
    assertTrue(tf.match(Instant.ofEpochSecond(0)));
    assertTrue(tf.match(Instant.ofEpochSecond(1)));
    assertFalse(tf.match(Instant.ofEpochSecond(2)));

    tf = TimeFilter.acceptBetween(Instant.ofEpochSecond(1), Instant.ofEpochSecond(3));
    assertFalse(tf.match(Instant.ofEpochSecond(0)));
    assertTrue(tf.match(Instant.ofEpochSecond(1)));
    assertTrue(tf.match(Instant.ofEpochSecond(2)));
    assertTrue(tf.match(Instant.ofEpochSecond(3)));
    assertFalse(tf.match(Instant.ofEpochSecond(4)));

    tf = TimeFilter.acceptBetween(Instant.ofEpochSecond(1), Instant.ofEpochSecond(1));
    assertFalse(tf.match(Instant.ofEpochSecond(0)));
    assertTrue(tf.match(Instant.ofEpochSecond(1)));
    assertFalse(tf.match(Instant.ofEpochSecond(2)));
  }

  @Test
  void testPropertiesFilter() {
    MultiValuesFilterI sub_a_b_eq_c_filter = new MultiValuesFilter<Integer>(new String[] {"a", "b", "c"},
            values -> values[0] - values[1] == values[2]);

    PropertiesFilterI mpf = new PropertiesFilter(sub_a_b_eq_c_filter);

    assertTrue(mpf.match(new Property("a", 15), new Property("b", 10), new Property("c", 5)));
    assertTrue(mpf.match(new Property("b", 10), new Property("a", 15), new Property("c", 5)));
    assertTrue(mpf.match(new Property("a", 15), new Property("c", 5), new Property("b", 10)));
    assertTrue(mpf.match(new Property("c", 5), new Property("a", 15), new Property("b", 10)));
    assertTrue(mpf.match(new Property("c", 5), new Property("b", 10), new Property("a", 15)));
    assertTrue(mpf.match(new Property("a", 15), new Property("b", 10), new Property("c", 5), new Property("d", 0)));

    assertFalse(mpf.match(new Property("a", 15), new Property("b", 10), new Property("c", 4)));
    assertFalse(mpf.match(new Property("a", 15), new Property("b", 10), new Property("C", 5)));
    assertFalse(mpf.match(new Property("a", 15), new Property("b", 10)));
    assertFalse(mpf.match(new Property("a", 15), new Property("b", 10), new Property("c", 5.0)));
  }

  @Test
  void testMessageFilter() {
    PropertiesFilterI strong_wind = new PropertiesFilter(new MultiValuesFilter<Double>(new String[] { "X", "Y" },
            values -> Math.sqrt(values[0] * values[0] + values[1] * values[1]) >= 40.0));

    PropertyFilterI is_wind_data = new PropertyFilter("Type", new ComparableValueFilter("WindData"));
    PropertyFilterI acceptable_version = new PropertyFilter("Version", new ComparableValueFilter(3.14, ComparableValueFilter.Operator.GE));

    TimeFilterI after_1000 = TimeFilter.acceptAfter(Instant.ofEpochSecond(1000));

    MessageFilterI strong_filter = new MessageFilter(new PropertyFilterI[] { is_wind_data, acceptable_version }, new PropertiesFilterI[] { strong_wind }, after_1000);


    MessageI message = new Message("Message content", Instant.ofEpochSecond(1001));
    message.putProperty("Type", "WindData");
    message.putProperty("Version", 3.15);
    message.putProperty("X", 25.0);
    message.putProperty("Y", 38.0);

    assertTrue(strong_filter.match(message));

    // TODO: test with wrong values
  }

}