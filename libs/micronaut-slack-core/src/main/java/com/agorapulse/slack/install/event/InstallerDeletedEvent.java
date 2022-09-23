package com.agorapulse.slack.install.event;

public class InstallerDeletedEvent extends InstallationEvent {

    public InstallerDeletedEvent(String enterpriseId, String teamId, String userId) {
        super(enterpriseId, teamId, userId);
    }

}
