/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2022 Agorapulse.
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

import com.agorapulse.slack.install.enumerate.InstallationEnumerationService;
import com.agorapulse.slack.oauth.DistributedAppMethodsClientFactory;
import com.slack.api.bolt.model.Bot;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.model.Conversation;
import io.micronaut.runtime.event.ApplicationStartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.slack.api.model.block.Blocks.actions;
import static com.slack.api.model.block.Blocks.header;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.button;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

@Singleton
public class MessageSender {

    private final DistributedAppMethodsClientFactory factory;                           // <1>
    private final InstallationEnumerationService enumerationService;                    // <2>

    public MessageSender(
        DistributedAppMethodsClientFactory factory,
        InstallationEnumerationService enumerationService
    ) {
        this.factory = factory;
        this.enumerationService = enumerationService;
    }

    @EventListener
    public void sendMessage(ApplicationStartupEvent event) throws SlackApiException, IOException {
        List<Bot> allBots = enumerationService.findAllBots().collect(toList());         // <3>
        for (Bot bot : allBots) {
            MethodsClient methods = factory
                .createClient(bot)                                                      // <4>
                .orElseThrow(() -> new IllegalStateException("Should not happen"));

            ConversationsListResponse channels = methods.conversationsList(b -> b);     // <5>
            Optional<Conversation> generalChannel = channels
                .getChannels()
                .stream()
                .filter(Conversation::isGeneral)
                .findAny();

            if (generalChannel.isPresent()) {
                methods.chatPostMessage(m -> m                                          // <6>
                    .channel(generalChannel.get().getId())
                    .blocks(asList(
                        header(b -> b.text(plainText("Micronaut is awesome, WDYT?"))),  // <7>
                        actions(asList(
                            button(b -> b.actionId("#mn-yes").text(plainText("Yes"))),  // <8>
                            button(b -> b.actionId("#mn-no").text(plainText("No")))
                        ))
                    ))
                );
            }
        }
    }

}
