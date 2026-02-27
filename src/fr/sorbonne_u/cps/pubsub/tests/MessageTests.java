package fr.sorbonne_u.cps.pubsub.tests;

import fr.sorbonne_u.cps.pubsub.exceptions.UnknownPropertyException;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.message.Property;
import fr.sorbonne_u.cps.pubsub.message.Message;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class MessageTests {

    @Test
    public void testProperty() {
        MessageI.PropertyI p = new Property("It's my name", "Value");

        assertThrows(IllegalArgumentException.class, () -> new Property("a", null));
        assertThrows(IllegalArgumentException.class, () -> new Property(null, ""));
        assertThrows(IllegalArgumentException.class, () -> new Property("", ""));

        assertEquals("It's my name", p.getName());
        assertEquals("Value", p.getValue());

        assertEquals(new Property("Azertyuiop", 1), new Property("Azertyuiop", 1));
        assertNotEquals(new Property("Azertyuiop", 2), new Property("Azertyuiom", 2));
        assertNotEquals(new Property("Azertyuiop", 1), new Property("Azertyuiop", 2));
    }

    @Test
    public void testMessage() throws Exception {
        MessageI m = new Message("Payload..");

        assertThrows(IllegalArgumentException.class, () -> new Message("Payload", null));
        assertThrows(IllegalArgumentException.class, () -> new Message(null, Instant.now()));

        assertEquals("Payload..", m.getPayload());
        assertFalse(Instant.now().isBefore(m.getTimeStamp()));
        assertEquals(0, m.getProperties().length);

        m.putProperty("PropA", "valA");
        m.putProperty("PropB", "valB");
        m.putProperty("PropC", "valC");

        assertThrows(IllegalArgumentException.class, () -> m.putProperty(null, ""));
        assertThrows(IllegalArgumentException.class, () -> m.putProperty("prop", null));
        assertThrows(RuntimeException.class, () -> m.putProperty("PropA", "valA bis"));

        assertThrows(IllegalArgumentException.class, () -> m.setPayload(null));

        assertThrows(IllegalArgumentException.class, () -> m.removeProperty(null));
        assertThrows(UnknownPropertyException.class, () -> m.removeProperty("noProp"));

        assertThrows(IllegalArgumentException.class, () -> m.getPropertyValue(null));
        assertThrows(UnknownPropertyException.class, () -> m.getPropertyValue("Prop1000"));
        assertTrue(m.propertyExists("PropA"));
        assertTrue(m.propertyExists("PropB"));
        assertFalse(m.propertyExists("Prop1000"));

        assertEquals(3, m.getProperties().length);

        MessageI m_cp = m.copy();
        assertNotSame(m, m_cp);
        assertNotSame(m.getProperties(), m_cp.getProperties());
        assertEquals(3, m_cp.getProperties().length);
        assertArrayEquals(m.getProperties(), m_cp.getProperties());
        assertEquals(m.getPayload(), m_cp.getPayload());
        assertEquals(m.getTimeStamp(), m_cp.getTimeStamp());
        assertSame(m.getTimeStamp(), m_cp.getTimeStamp());

        m.removeProperty("PropB");
        assertFalse(m.propertyExists("PropB"));

        assertTrue(m_cp.propertyExists("PropB"));
        assertEquals("valB", m_cp.getPropertyValue("PropB"));

        m.putProperty("Test", 123);
        assertFalse(m.propertyExists("test"));
    }

}
