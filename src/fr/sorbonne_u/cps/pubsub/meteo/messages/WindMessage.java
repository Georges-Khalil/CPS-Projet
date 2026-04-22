package fr.sorbonne_u.cps.pubsub.meteo.messages;

import fr.sorbonne_u.cps.pubsub.components.Station;
import fr.sorbonne_u.cps.pubsub.message.Message;
import fr.sorbonne_u.cps.pubsub.meteo.WindData;

public class WindMessage extends Message {

    public WindMessage(Station station, double windX, double windY) {
        super(new WindData(station.getPosition(), windX, windY));
        if (station == null)
            throw new IllegalArgumentException();
        putProperty("Type", "wind");
        putProperty("Id", station.getUid());
    }

}
