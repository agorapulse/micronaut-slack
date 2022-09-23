package com.agorapulse.slack.install.event;

public class BotSavedEvent extends InstallationEvent {

    public BotSavedEvent(String enterpriseId, String teamId, String userId) {
        super(enterpriseId, teamId, userId);
    }

}
