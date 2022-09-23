package com.agorapulse.slack.oauth;

import com.slack.api.bolt.model.Bot;
import com.slack.api.methods.MethodsClient;

import java.util.Optional;

public interface DistributedAppMethodsClientFactory {

    Optional<MethodsClient> createClient(String enterpriseId, String teamId);

    default Optional<MethodsClient> createClient(Bot bot) {
        return createClient(bot.getEnterpriseId(), bot.getTeamId());
    }

}
