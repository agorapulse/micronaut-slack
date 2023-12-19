/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2022-2023 Agorapulse.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agorapulse.slack.example.sender.interactive;

import com.agorapulse.slack.handlers.MicronautBoltEventHandler;
import com.agorapulse.slack.oauth.DistributedAppMethodsClientFactory;
import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.event.ReactionAddedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.Optional;

@Singleton
public class ReactionHandler implements MicronautBoltEventHandler<ReactionAddedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactionHandler.class);

    private final DistributedAppMethodsClientFactory clientFactory;

    public ReactionHandler(DistributedAppMethodsClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public Class<ReactionAddedEvent> getEventType() {
        return ReactionAddedEvent.class;
    }

    @Override
    public Response apply(EventsApiPayload<ReactionAddedEvent> event, EventContext context) throws IOException, SlackApiException {
        String channel = event.getEvent().getItem().getChannel();
        Optional<MethodsClient> maybeClient = clientFactory.createClient(event.getEnterpriseId(), event.getTeamId());
        if (maybeClient.isPresent()) {
            ChatPostMessageResponse response = maybeClient.get().chatPostMessage(m -> m
                    .channel(channel)
                    .text("Thank you, <@" + event.getEvent().getUser() + "> for your :" + event.getEvent().getReaction())
            );
            if (!response.isOk()) {
                LOGGER.error("Error sending a message: " + response);
            }
        } else {
            LOGGER.warn("No installation found for team {}", event.getTeamId());
        }

        return context.ack();
    }

}
