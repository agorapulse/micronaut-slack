package com.agorapulse.slack.install.event;

public abstract class InstallationEvent {

    private final String enterpriseId;
    private final String teamId;

    private final String userId;

    protected InstallationEvent(String enterpriseId, String teamId, String userId) {
        this.enterpriseId = enterpriseId;
        this.teamId = teamId;
        this.userId = userId;
    }

    public String getEnterpriseId() {
        return enterpriseId;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getUserId() {
        return userId;
    }
}
