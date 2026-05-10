package fr.sorbonne_u.cps.pubsub.filters;

import fr.sorbonne_u.cps.meteo.interfaces.MeteoAlertI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.Serializable;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class AlertLevelFilter implements MessageFilterI.ValueFilterI {
    private final MeteoAlertI.Level level;

    public AlertLevelFilter(MeteoAlertI.Level level) {
        this.level = level;
    }

    @Override
    public boolean match(Serializable value) {
        if (value == null) throw new IllegalArgumentException();
        if (!(value instanceof MeteoAlertI.Level)) return false;

        MeteoAlertI.Level level = (MeteoAlertI.Level) value;

        switch (this.level) { // No-break switch for multiple value match (e.g: if this level = RED then return true if value level is RED or SCARLET otherwise return false)
            case GREEN:
                if (level == MeteoAlertI.Level.GREEN) return true;
            case YELLOW:
                if (level == MeteoAlertI.Level.YELLOW) return true;
            case ORANGE:
                if (level == MeteoAlertI.Level.ORANGE) return true;
            case RED:
                if (level == MeteoAlertI.Level.RED) return true;
            case SCARLET:
                return level == MeteoAlertI.Level.SCARLET;
            default:
                throw new NotImplementedException();
        }
    }
}
