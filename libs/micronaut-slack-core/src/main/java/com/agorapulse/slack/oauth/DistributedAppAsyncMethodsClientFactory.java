package com.agorapulse.slack.oauth;

import com.slack.api.bolt.model.Bot;
import com.slack.api.methods.AsyncMethodsClient;
import com.slack.api.methods.MethodsClient;

import java.util.Optional;

public interface DistributedAppAsyncMethodsClientFactory {

    Optional<AsyncMethodsClient> createClient(String enterpriseId, String teamId);

    default Optional<AsyncMethodsClient> createClient(Bot bot) {
        return createClient(bot.getEnterpriseId(), bot.getTeamId());
    }

}
