package com.agorapulse.slack.install.event;

public class BotDeletedEvent extends InstallationEvent {

    public BotDeletedEvent(String enterpriseId, String teamId, String userId) {
        super(enterpriseId, teamId, userId);
    }

}
