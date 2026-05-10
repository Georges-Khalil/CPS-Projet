package fr.sorbonne_u.cps.pubsub.meteo.messages;

import fr.sorbonne_u.cps.pubsub.components.Station;
import fr.sorbonne_u.cps.pubsub.message.Message;
import fr.sorbonne_u.cps.pubsub.meteo.Position;
import fr.sorbonne_u.cps.pubsub.meteo.WindData;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class WindMessage extends Message {

    public WindMessage(Station station, double windX, double windY) {
        super(new WindData(station.getPosition(), windX, windY));
        if (station == null)
            throw new IllegalArgumentException();
        putProperty("Type", "Wind");
        putProperty("Id", station.getUid());
        putProperty("Region", Position.toRegion((Position)station.getPosition()));
        putProperty("Force", Math.hypot(windX, windY));
    }

}
