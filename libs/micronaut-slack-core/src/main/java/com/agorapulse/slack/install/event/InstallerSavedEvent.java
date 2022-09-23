package com.agorapulse.slack.install.event;

public class InstallerSavedEvent extends InstallationEvent {

    public InstallerSavedEvent(String enterpriseId, String teamId, String userId) {
        super(enterpriseId, teamId, userId);
    }

}
