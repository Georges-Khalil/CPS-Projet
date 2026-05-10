package fr.sorbonne_u.cps.pubsub.meteo.messages;

import fr.sorbonne_u.cps.meteo.interfaces.MeteoAlertI;
import fr.sorbonne_u.cps.pubsub.message.Message;
import fr.sorbonne_u.cps.pubsub.meteo.MeteoAlert;

public class AlertMessage extends Message {

    public AlertMessage(MeteoAlertI alert) {
        super(alert);
        putProperty("Type", "Alert");
        putProperty("AlertType", alert.getAlertType().toString());
        putProperty("AlertLevel", (MeteoAlertI.Level)alert.getLevel());
        putProperty("AlertDuration", alert.getDuration());
        putProperty("AlertStart", alert.getStartTime());
    }

}
