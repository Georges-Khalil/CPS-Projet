package fr.sorbonne_u.cps.pubsub.tests;

import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI.*;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class MessageFiltersTests {

//  @Test
//  void testArguments() {
//    assertThrows(IllegalArgumentException.class, () -> new ValueFilter(null, null));
//    assertThrows(IllegalArgumentException.class, () -> new ValueFilter(null, ValueFilter.FilterOperation.ANY));
//    assertThrows(IllegalArgumentException.class, () -> new ValueFilter(new Serializable() {}, ValueFilter.FilterOperation.LOWER));
//    assertThrows(IllegalArgumentException.class, () -> new ValueFilter("a", null));
//
//    assertThrows(IllegalArgumentException.class, () -> new TimeFilter(Instant.ofEpochSecond(10), Instant.ofEpochSecond(9)));
//
//    assertThrows(IllegalArgumentException.class, () -> new PropertyFilter(null, null));
//    assertThrows(IllegalArgumentException.class, () -> new PropertyFilter(null, new ValueFilter("a", ValueFilter.FilterOperation.ANY)));
//    assertThrows(IllegalArgumentException.class, () -> new PropertyFilter("propName", null));
//    assertThrows(IllegalArgumentException.class, () -> new PropertyFilter("", new ValueFilter("a", ValueFilter.FilterOperation.ANY)));
//
//    assertThrows(IllegalArgumentException.class, () -> new MultiValuesFilter(null) {});
//    assertThrows(IllegalArgumentException.class, () -> new MultiValuesFilter(new String[0]) {});
//    assertThrows(IllegalArgumentException.class, () -> new MultiValuesFilter(new String[] { null }) {});
//
//    assertThrows(IllegalArgumentException.class, () -> new PropertiesFilter(null));
//
//    assertThrows(IllegalArgumentException.class, () -> new MessageFilter(null, new PropertiesFilterI[0], new TimeFilter()));
//    assertThrows(IllegalArgumentException.class, () -> new MessageFilter(new PropertyFilterI[0], null, new TimeFilter()));
//    assertThrows(IllegalArgumentException.class, () -> new MessageFilter(new PropertyFilterI[0], new PropertiesFilterI[0], null));
//
//    ValueFilterI vf = new ValueFilter("a", ValueFilter.FilterOperation.GRATER);
//    TimeFilterI tf = new TimeFilter();
//    PropertyFilterI pf = new PropertyFilter("prop", vf);
//    MultiValuesFilterI mvf = new MultiValuesFilter(new String[] {"prop"}) {};
//    PropertiesFilterI mpf = new PropertiesFilter(mvf);
//
//    assertThrows(IllegalArgumentException.class, () -> vf.match(null));
//    assertThrows(IllegalArgumentException.class, () -> vf.match(new Serializable() {}));
//    assertThrows(IllegalArgumentException.class, () -> tf.match(null));
//    assertThrows(IllegalArgumentException.class, () -> pf.match(null));
//    assertThrows(IllegalArgumentException.class, () -> mvf.match(null));
//    assertThrows(IllegalArgumentException.class, () -> mpf.match(null));
//    assertThrows(IllegalArgumentException.class, () -> mvf.match(new Serializable[0]));
//    assertThrows(IllegalArgumentException.class, () -> mpf.match(new Message.Property[0]));
//  }
//
//  @Test
//  void testPropertyFilter() {
//    ValueFilterI vf = new ValueFilter("", ValueFilter.FilterOperation.ANY);
//    PropertyFilterI pf = new PropertyFilter("prop", vf);
//
//    assertTrue(vf.match(""));
//    assertTrue(pf.match(new Message.Property("prop", "")));
//    assertFalse(pf.match(new Message.Property("propB", "")));
//    assertFalse(pf.match(new Message.Property("Prop", "")));
//
//    assertTrue(new ValueFilter(1, ValueFilter.FilterOperation.ANY).match(0));
//    assertTrue(new ValueFilter(1, ValueFilter.FilterOperation.ANY).match(1));
//    assertTrue(new ValueFilter(1, ValueFilter.FilterOperation.ANY).match(2));
//    assertTrue(new ValueFilter(1, ValueFilter.FilterOperation.LOWER).match(0));
//    assertTrue(new ValueFilter(1, ValueFilter.FilterOperation.GRATER).match(2));
//    assertTrue(new ValueFilter(1, ValueFilter.FilterOperation.EQUALS).match(1));
//    assertTrue(new ValueFilter(1, ValueFilter.FilterOperation.NOT_EQUALS).match(0));
//    assertTrue(new ValueFilter(1, ValueFilter.FilterOperation.NOT_EQUALS).match(2));
//    assertTrue(new ValueFilter(1, ValueFilter.FilterOperation.NOT_EQUALS).match("1"));
//    assertTrue(new ValueFilter(1, ValueFilter.FilterOperation.LOWER_EQUALS).match(0));
//    assertTrue(new ValueFilter(1, ValueFilter.FilterOperation.LOWER_EQUALS).match(1));
//    assertTrue(new ValueFilter(1, ValueFilter.FilterOperation.GREATER_EQUALS).match(2));
//    assertTrue(new ValueFilter(1, ValueFilter.FilterOperation.GREATER_EQUALS).match(1));
//
//    assertFalse(new ValueFilter(1, ValueFilter.FilterOperation.LOWER).match(1));
//    assertFalse(new ValueFilter(1, ValueFilter.FilterOperation.LOWER).match(2));
//    assertFalse(new ValueFilter(1, ValueFilter.FilterOperation.GRATER).match(1));
//    assertFalse(new ValueFilter(1, ValueFilter.FilterOperation.GRATER).match(0));
//    assertFalse(new ValueFilter(1, ValueFilter.FilterOperation.EQUALS).match(0));
//    assertFalse(new ValueFilter(1, ValueFilter.FilterOperation.EQUALS).match(2));
//    assertFalse(new ValueFilter(1, ValueFilter.FilterOperation.EQUALS).match("1"));
//    assertFalse(new ValueFilter(1, ValueFilter.FilterOperation.NOT_EQUALS).match(1));
//    assertFalse(new ValueFilter(1, ValueFilter.FilterOperation.LOWER_EQUALS).match(2));
//    assertFalse(new ValueFilter(1, ValueFilter.FilterOperation.GREATER_EQUALS).match(0));
//    assertFalse(new ValueFilter(1, ValueFilter.FilterOperation.GREATER_EQUALS).match("1"));
//  }
//
//  @Test
//  void testTimeFilter() {
//    TimeFilterI tf = new TimeFilter();
//    assertTrue(tf.match(Instant.ofEpochSecond(0)));
//    assertTrue(tf.match(Instant.ofEpochSecond(1)));
//    assertTrue(tf.match(Instant.ofEpochSecond(2)));
//
//    tf = new TimeFilter(Instant.ofEpochSecond(1), null);
//    assertFalse(tf.match(Instant.ofEpochSecond(0)));
//    assertTrue(tf.match(Instant.ofEpochSecond(1)));
//    assertTrue(tf.match(Instant.ofEpochSecond(2)));
//
//    tf = new TimeFilter(null, Instant.ofEpochSecond(1));
//    assertTrue(tf.match(Instant.ofEpochSecond(0)));
//    assertTrue(tf.match(Instant.ofEpochSecond(1)));
//    assertFalse(tf.match(Instant.ofEpochSecond(2)));
//
//    tf = new TimeFilter(Instant.ofEpochSecond(1), Instant.ofEpochSecond(3));
//    assertFalse(tf.match(Instant.ofEpochSecond(0)));
//    assertTrue(tf.match(Instant.ofEpochSecond(1)));
//    assertTrue(tf.match(Instant.ofEpochSecond(2)));
//    assertTrue(tf.match(Instant.ofEpochSecond(3)));
//    assertFalse(tf.match(Instant.ofEpochSecond(4)));
//
//    tf = new TimeFilter(Instant.ofEpochSecond(1), Instant.ofEpochSecond(1));
//    assertFalse(tf.match(Instant.ofEpochSecond(0)));
//    assertTrue(tf.match(Instant.ofEpochSecond(1)));
//    assertFalse(tf.match(Instant.ofEpochSecond(2)));
//  }
//
//  @Test
//  void testPropertiesFilter() {
//    MultiValuesFilterI sub_a_b_eq_c_filter = new MultiValuesFilter(new String[] {"a", "b", "c"}) {
//      @Override
//      public boolean match(Serializable... values) {
//        if (values == null || values.length != 3 || !(values[0] instanceof Integer) || !(values[1] instanceof Integer) || !(values[2] instanceof Integer))
//          return false;
//        return ((Integer)values[0] - (Integer)values[1]) == (Integer)values[2];
//      }
//    };
//
//    PropertiesFilterI mpf = new PropertiesFilter(sub_a_b_eq_c_filter);
//
//    assertTrue(mpf.match(new Message.Property("a", 15), new Message.Property("b", 10), new Message.Property("c", 5)));
//    assertTrue(mpf.match(new Message.Property("b", 10), new Message.Property("a", 15), new Message.Property("c", 5)));
//    assertTrue(mpf.match(new Message.Property("a", 15), new Message.Property("c", 5), new Message.Property("b", 10)));
//    assertTrue(mpf.match(new Message.Property("c", 5), new Message.Property("a", 15), new Message.Property("b", 10)));
//    assertTrue(mpf.match(new Message.Property("c", 5), new Message.Property("b", 10), new Message.Property("a", 15)));
//    assertTrue(mpf.match(new Message.Property("a", 15), new Message.Property("b", 10), new Message.Property("c", 5), new Message.Property("d", 0)));
//
//    assertFalse(mpf.match(new Message.Property("a", 15), new Message.Property("b", 10), new Message.Property("c", 4)));
//    assertFalse(mpf.match(new Message.Property("a", 15), new Message.Property("b", 10), new Message.Property("C", 5)));
//    assertFalse(mpf.match(new Message.Property("a", 15), new Message.Property("b", 10)));
//    assertFalse(mpf.match(new Message.Property("a", 15), new Message.Property("b", 10), new Message.Property("c", 5.0)));
//  }
//
//  @Test
//  void testMessageFilter() {
//    PropertiesFilterI strong_wind = new PropertiesFilter(new MultiValuesFilter(new String[] { "X", "Y" }) {
//      @Override
//      public boolean match(Serializable... values) {
//        if (values.length != 2 || !(values[0] instanceof Double) || !(values[1] instanceof Double))
//          return false;
//        return Math.sqrt((Double)values[0] * (Double)values[0] + (Double)values[1] * (Double)values[1]) >= 40.0;
//      }
//    });
//
//    PropertyFilterI is_wind_data = new PropertyFilter("Type", new ValueFilter("WindData"));
//    PropertyFilterI acceptable_version = new PropertyFilter("Version", new ValueFilter(3.14, ValueFilter.FilterOperation.GREATER_EQUALS));
//
//    TimeFilterI after_1000 = new TimeFilter(Instant.ofEpochSecond(1000), null);
//
//    MessageFilterI strong_filter = new MessageFilter(new PropertyFilterI[] { is_wind_data, acceptable_version }, new PropertiesFilterI[] { strong_wind }, after_1000);
//
//
//    MessageI message = new Message("Message content", Instant.ofEpochSecond(1001));
//    message.putProperty("Type", "WindData");
//    message.putProperty("Version", 3.15);
//    message.putProperty("X", 25.0);
//    message.putProperty("Y", 38.0);
//
//    assertTrue(strong_filter.match(message));
//
//    // TODO: test with wrong values
//  }

}